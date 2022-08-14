package com.adun.cloudalibabaproviderpayment9003;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class SentinelPaymentMain9003 {

    public static void main(String[] args) {
        SpringApplication.run(SentinelPaymentMain9003.class, args);
    }

}
