package com.adun.springcloud;

import com.adun.myrule.MySelfRule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.cloud.netflix.ribbon.RibbonClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * @author Zhu Dunfeng
 * @date 2022/2/26 23:20
 */
@SpringBootApplication
@EnableEurekaClient
//@EnableAspectJAutoProxy
//自定义ribbon负载均衡算法
//分别指定多个服务调用负载均衡算法
//@RibbonClients(value = {
//        @RibbonClient(name = "CLOUD-PAYMENT-SERVICE",configuration= MySelfRule.class),
//        @RibbonClient(name = "CLOUD-PAYMENT-SERVICE",configuration= MySelfRule.class)})
//指定单个服务调用负载均衡算法
//@RibbonClient(name = "CLOUD-PAYMENT-SERVICE",configuration= MySelfRule.class)
public class OrderMain80 {
    public static void main(String[] args) {
        SpringApplication.run(OrderMain80.class, args);
    }
}
