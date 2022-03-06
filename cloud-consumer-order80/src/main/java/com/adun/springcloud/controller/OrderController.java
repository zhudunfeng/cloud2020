package com.adun.springcloud.controller;

import com.adun.springcloud.aop.annotation.LoadBalanced;
import com.adun.springcloud.entities.CommonResult;
import com.adun.springcloud.entities.Payment;
import com.adun.springcloud.lb.LoadBalancer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Zhu Dunfeng
 * @date 2022/2/26 23:21
 */
@RestController
@Slf4j
public class OrderController {

//    public static final String PAYMENT_URL="http://localhost:8001";
    public static final String PAYMENT_URL="http://CLOUD-PAYMENT-SERVICE";

    @Resource
    private RestTemplate restTemplate;

    @Resource
    private DiscoveryClient discoveryClient;

    @Resource
    private LoadBalancer loadBalancer;

    @GetMapping(value = "/consumer/payment/create")
    public CommonResult<Payment> create(Payment payment){
        log.info("消费端create：{}",payment);
        return restTemplate.postForObject(PAYMENT_URL + "/payment/create", payment, CommonResult.class);
    }

    @GetMapping(value = "/consumer/payment/get/{id}")
    public CommonResult<Payment> getPaymentById(@PathVariable("id") Long id){
        log.info("消费端getPaymentById：{}",id);
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        return restTemplate.getForObject(PAYMENT_URL + "/payment/get/{id}", CommonResult.class,map);
    }

//    @GetMapping(value = "/consumer/payment/lb")
//    public String getPaymentLB(){
//        List<ServiceInstance> instances = discoveryClient.getInstances("CLOUD-PAYMENT-SERVICE");
//        if(instances == null || instances.size() <= 0) {
//            return null;
//        }
//        ServiceInstance serviceInstance = loadBalancer.instances(instances);
//        URI uri = serviceInstance.getUri();
//        return restTemplate.getForObject(uri+"/payment/lb",String.class);
//    }

    @GetMapping(value = "/consumer/payment/lb")
    @LoadBalanced(path = "/payment/lb",instance = "CLOUD-PAYMENT-SERVICE")
    public String getPaymentLB(){
        System.out.println("调用成功");
        return "";
    }

}
