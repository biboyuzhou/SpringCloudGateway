package com.drcnet.platform.gateway;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @version: v1.0
 * @date: 2020/1/9
 * @author: lianrf
 */
@EnableDiscoveryClient
@SpringBootApplication
@MapperScan("com.drcnet.platform.gateway.mapper")
public class GatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

}
