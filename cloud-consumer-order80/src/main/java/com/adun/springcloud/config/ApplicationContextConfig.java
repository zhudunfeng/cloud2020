package com.adun.springcloud.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * @author Zhu Dunfeng
 * @date 2022/2/26 23:34
 */
@Configuration
public class ApplicationContextConfig {

    @Bean
    //restTemplate启用ribbon的负载均衡
//    @LoadBalanced
    public RestTemplate getRestTemplate(){
        return new RestTemplate();
    }
}
