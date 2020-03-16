package com.drcnet.platform.gateway.config;

import com.drcnet.platform.gateway.filter.ElapsedFilter;
import com.drcnet.platform.gateway.filter.HostAddrKeyResolver;
import com.drcnet.platform.gateway.filter.MemberVerifyFilter;
import com.drcnet.platform.gateway.filter.TokenFilter;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.Serializable;

/**
 * @Author jack
 * @Date: 2020/2/18 14:38
 * @Desc:
 **/
@Configuration
public class GatewayConfig {

    @Resource
    private RedisTemplate<Serializable,Object> redisTemplate;

    @Bean
    public HostAddrKeyResolver hostAddrKeyResolver() {
        return new HostAddrKeyResolver();
    }


    @Bean
    public RouteLocator customerRouteLocator(RouteLocatorBuilder builder) {
        // @formatter:off
        return builder.routes()
                .route(r -> r.path("/fluent/customer/**")
                        .filters(f -> f.stripPrefix(2)
                                .filter(new ElapsedFilter())
                                .addResponseHeader("X-Response-Default-Foo", "Default-Bar"))
                        .uri("lb://CONSUMER")
                        .order(0)
                        .id("fluent_customer_service")
                )
                .build();
        // @formatter:on

    }

    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route(r -> r.path("/test/**")
                        .uri("lb://demo-service"))
                .route(r -> r.path("/forward/**")
                        .uri("lb://hello-service"))
                .build();

    }


    @Bean
    public TokenFilter tokenFilter(){
        return new TokenFilter();
    }

    @Bean
    public MemberVerifyFilter memberVerifyFilter(){
        return new MemberVerifyFilter();
    }

    @PostConstruct
    public void serializeRedisTemplate() {

        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        om.registerModule(new JavaTimeModule());
        //设置序列化
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        jackson2JsonRedisSerializer.setObjectMapper(om);

        RedisSerializer stringSerializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(stringSerializer);//key序列化
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);//value序列化
        redisTemplate.setHashKeySerializer(stringSerializer);//Hash key序列化
        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);//Hash value序列化
        redisTemplate.setEnableTransactionSupport(false);
        redisTemplate.afterPropertiesSet();
    }



}
