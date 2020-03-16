package com.drcnet.platform.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @Author jack
 * @Date: 2020/2/18 14:09
 * @Desc: 以log日志的形式记录每次请求耗费的时间
 **/
@Slf4j
public class ElapsedFilter implements GatewayFilter, Ordered {

    private static final String REQUEST_TIME_BEGIN = "requestTimeBegin";

    /**
     * chain.filter(exchange)之前的就是 “pre” 部分，之后的也就是then里边的是 “post” 部分
     * @param exchange
     * @param chain
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        exchange.getAttributes().put(REQUEST_TIME_BEGIN, System.currentTimeMillis());
        return chain.filter(exchange).then(
                Mono.fromRunnable(() -> {
                    Long startTime = exchange.getAttribute(REQUEST_TIME_BEGIN);
                    if (startTime != null) {
                        Long cost = System.currentTimeMillis() - startTime;
                        log.info("请求路径：{} 小耗时间：{}ms", exchange.getRequest().getURI().getRawPath(), cost);
                    }
                })
        );
    }

    @Override
    public int getOrder() {
        /**
         * filter 优先级，值越大优先级越低
         */
        return Ordered.LOWEST_PRECEDENCE;
    }
}
