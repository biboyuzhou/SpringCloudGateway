package com.drcnet.platform.gateway.filter;

import com.alibaba.fastjson.JSON;
import com.drcnet.platform.gateway.constant.RedisCacheKey;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author jack
 * @Date: 2020/2/18 14:35
 * @Desc: token filter  通过验证token的正确性验证权限
 **/
public class TokenFilter implements GlobalFilter, Ordered {



    @Resource
    private RedisTemplate<Serializable, Object> redisTemplate;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String token = exchange.getRequest().getQueryParams().getFirst("token");
        ServerHttpRequest req = exchange.getRequest();
        String path = req.getURI().getPath();
        ServerHttpResponse serverHttpResponse = exchange.getResponse();
        /*if (path.contains(TRANSFER)) {
            if (StringUtils.isEmpty(token) || !redisTemplate.hasKey(RedisCacheKey.TOKEN_REDIS_KEY + token)) {
                Map map = new HashMap<>(2);
                map.put("code", HttpStatus.UNAUTHORIZED.value());
                map.put("message", "签名验证失败");
                String resp = JSON.toJSONString(map);
                DataBuffer bodyDataBuffer = serverHttpResponse.bufferFactory().wrap(resp.getBytes());
                serverHttpResponse.getHeaders().add("Content-Type", "text/plain;charset=UTF-8");
                return serverHttpResponse.writeWith(Mono.just(bodyDataBuffer));
            }
        }*/
        exchange.getAttributes().put("path", path);
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
