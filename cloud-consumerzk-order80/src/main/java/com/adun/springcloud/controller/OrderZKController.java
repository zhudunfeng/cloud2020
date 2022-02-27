package com.adun.springcloud.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;

/**
 * @author Zhu Dunfeng
 * @date 2022/2/27 18:36
 */
@RestController
public class OrderZKController {

    public static final String INVOKE_URL="http://cloud-provider-payment";

    @Resource
    private RestTemplate restTemplate;

    @GetMapping(value = "/consumer/payment/zk")
    public String getPayment(){
        return restTemplate.getForObject(INVOKE_URL+"/payment/zk", String.class );
    }


}
