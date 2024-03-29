package com.adun.cloudalibabasentinelservice8401;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class SentinelMain8401 {

    public static void main(String[] args) {
        SpringApplication.run(SentinelMain8401.class, args);
    }

}
