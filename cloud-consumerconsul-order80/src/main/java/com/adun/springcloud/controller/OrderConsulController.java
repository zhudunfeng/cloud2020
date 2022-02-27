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
public class OrderConsulController {

    public static final String INVOKE_URL="http://consul-provider-payment";

    @Resource
    private RestTemplate restTemplate;

    @GetMapping(value = "/consumer/payment/consul")
    public String getPayment(){
        return restTemplate.getForObject(INVOKE_URL+"/payment/consul", String.class );
    }


}
