package com.drcnet.platform.gateway.filter;

import com.alibaba.fastjson.JSON;
import com.drcnet.platform.common.constant.RedisKey;
import com.drcnet.platform.common.entity.DatasetSource;
import com.drcnet.platform.gateway.constant.RedisCacheKey;
import com.drcnet.platform.gateway.service.QueryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author jack
 * @Date: 2020/2/27 14:36
 * @Desc: 会员类型及会员权限认证
 **/
@Slf4j
public class MemberVerifyFilter implements GlobalFilter, Ordered {
    private static final String SECRET_KEY_CACHE = "";
    private static final String TRANSFER = "transfer";

    @Resource
    private QueryService queryService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpResponse serverHttpResponse = exchange.getResponse();
        ServerHttpRequest req = exchange.getRequest();
        log.info("-----------------path: " + req.getURI());
        String path = (String) exchange.getAttributes().get("path");
        if (!path.contains(TRANSFER)) {
            return chain.filter(exchange);
        }
        int dataType = 2;
        String[] pathArray = path.split("/");
        if ("api".equals(pathArray[3])) {
            dataType = 1;
        } else if ("dataset".equals(pathArray[3])) {
            dataType = 2 ;
        }
        String token = exchange.getRequest().getQueryParams().getFirst("token");
        String userId = req.getQueryParams().getFirst("userId");
        if (StringUtils.isEmpty(userId)) {
            String secretKey = req.getQueryParams().getFirst("secretKey");
            userId = getUserId(secretKey);
            if (StringUtils.isEmpty(userId)) {
                DataBuffer bodyDataBuffer = getDataBuffer(serverHttpResponse, HttpStatus.UNAUTHORIZED.value(), "无效的用户信息！");
                return serverHttpResponse.writeWith(Mono.just(bodyDataBuffer));
            }
        }
        String dataId = req.getQueryParams().getFirst("dataId");
        if (dataId == null) {
            DataBuffer bodyDataBuffer = getDataBuffer(serverHttpResponse, HttpStatus.UNAUTHORIZED.value(), "无效的资源！");
            return serverHttpResponse.writeWith(Mono.just(bodyDataBuffer));
        }
        Long userid = Long.parseLong(userId);
        Long dataid = Long.parseLong(dataId);
        DatasetSource datasetSource = queryService.queryDataSource(userid, dataid, dataType);

        if (datasetSource == null) {
            DataBuffer bodyDataBuffer = getDataBuffer(serverHttpResponse, HttpStatus.UNAUTHORIZED.value(), "下载权限不在有效期！");
            return serverHttpResponse.writeWith(Mono.just(bodyDataBuffer));
        }

        if (datasetSource.getIsTime()) {
            LocalDateTime now = LocalDateTime.now();
            if (now.isAfter(datasetSource.getEndTime()) || now.isBefore(datasetSource.getCreateTime())) {
                DataBuffer bodyDataBuffer = getDataBuffer(serverHttpResponse, HttpStatus.UNAUTHORIZED.value(), "下载权限不在有效期！");
                return serverHttpResponse.writeWith(Mono.just(bodyDataBuffer));
            }
        }
        if (datasetSource.getIsNum() && datasetSource.getResidueNum() < 1) {
            DataBuffer bodyDataBuffer = getDataBuffer(serverHttpResponse, HttpStatus.UNAUTHORIZED.value(), "剩余下载次数为0！");
            return serverHttpResponse.writeWith(Mono.just(bodyDataBuffer));
        }
        //验证每分钟并发次数
        if (datasetSource.getIsConcurrent()) {
            //todo
        }

        if (dataType == 2) {
            //验证文件id和数据块的关系是否正确
            Long datasetId = datasetSource.getDatasetId();
            if (!queryService.isRightRelation(dataId, datasetId)) {
                DataBuffer bodyDataBuffer = getDataBuffer(serverHttpResponse, HttpStatus.UNAUTHORIZED.value(), "无当前文件的下载权限，该数据块不包含该文件！");
                return serverHttpResponse.writeWith(Mono.just(bodyDataBuffer));
            }
        }

        ServerHttpRequest request = addNewParam(exchange, userid);
        return chain.filter(exchange.mutate().request(request).build());
       // return chain.filter(exchange);
    }

    private ServerHttpRequest addNewParam(ServerWebExchange exchange, Long userid) {
        URI uri = exchange.getRequest().getURI();
        StringBuilder query = new StringBuilder();
        String originalQuery = uri.getRawQuery();

        if (org.springframework.util.StringUtils.hasText(originalQuery)) {
            query.append(originalQuery);
            if (originalQuery.charAt(originalQuery.length() - 1) != '&') {
                query.append('&');
            }
        }

        query.append("userId");
        query.append('=');
        query.append(userid);

        URI newUri = UriComponentsBuilder.fromUri(uri)
                .replaceQuery(query.toString())
                .build(true)
                .toUri();
        return exchange.getRequest().mutate().uri(newUri).build();
    }

    private DataBuffer getDataBuffer(ServerHttpResponse serverHttpResponse, int responseCode, String responseMessage) {
        Map map = new HashMap<>(2);
        map.put("code", responseCode);
        map.put("message", responseMessage);
        String resp = JSON.toJSONString(map);
    DataBuffer bodyDataBuffer = serverHttpResponse.bufferFactory().wrap(resp.getBytes());
        serverHttpResponse.getHeaders().add("Content-Type", "text/plain;charset=UTF-8");
        return bodyDataBuffer;
}

    private String getUserId(String secretKey) {
        Object userId = redisTemplate.opsForValue().get(RedisCacheKey.SecretKey_cache_key + secretKey);
        if (userId != null) {
            return String.valueOf(userId);
        }
        Long dUserId = queryService.getUserIdBySecretKey(secretKey);
        return String.valueOf(dUserId);
    }

    @Override
    public int getOrder() {
        return 2;
    }
}
