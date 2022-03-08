# SpringCloud
SpringCloud2020年的技术变更

## 初期项目结构
> 此时，项目未引入SpringCloud的服务注册与发现组件，服务之间的调用使用restTemplate进行相互调用



![image](https://user-images.githubusercontent.com/48040850/155866502-5e2966be-eae3-40c0-bbf6-b9aa3939d798.png)



![image-20220227111214723](https://gitee.com/zhudunfeng/cloudimage/raw/master/image/202202271112780.png)



## SpringCloudNetflix组件大部分进入维护状态

SpringCloudNetflix组件大部分进入维护状态，从而Spring官方推荐进行一些相应组件的升级与替换

维护前的主要cloud组件

<img src="https://gitee.com/zhudunfeng/cloudimage/raw/master/image/202203081638521.png" alt="image-20220308163814442" style="zoom: 67%;" />

推荐替换的相应组件

![image-20220308163942294](https://gitee.com/zhudunfeng/cloudimage/raw/master/image/202203081639345.png)

## 微服务注册中心

### 引入Eureka服务发现与注册

<img src="https://gitee.com/zhudunfeng/cloudimage/raw/master/image/202202271342914.png" alt="image-20220227134225873" style="zoom:80%;" />

eureka server集群

<img src="https://gitee.com/zhudunfeng/cloudimage/raw/master/image/202202271725855.png" alt="image-20220227172534797" style="zoom:67%;" />

![image-20220227141957114](https://gitee.com/zhudunfeng/cloudimage/raw/master/image/202202271419150.png)

### zookeeper相关项目

<img src="https://gitee.com/zhudunfeng/cloudimage/raw/master/image/202202272257070.png" alt="image-20220227225729931" style="zoom:80%;" />

### consul相关项目

<img src="https://gitee.com/zhudunfeng/cloudimage/raw/master/image/202202272259322.png" alt="image-20220227225915255" style="zoom:80%;" />

### 三个注册中心的对比

<img src="https://gitee.com/zhudunfeng/cloudimage/raw/master/image/202202272308504.png" alt="image-20220227230802462" style="zoom:67%;" />

<img src="https://gitee.com/zhudunfeng/cloudimage/raw/master/image/202202272309193.png" alt="image-20220227230944151" style="zoom:67%;" />







## 微服务调用

### restTemplate

进行简单的服务调用

<img src="https://gitee.com/zhudunfeng/cloudimage/raw/master/image/202203081426858.png" alt="image-20220308142650801" style="zoom: 80%;" />

<img src="https://gitee.com/zhudunfeng/cloudimage/raw/master/image/202203081427195.png" alt="image-20220308142726146" style="zoom:80%;" />

### ribbon+restTemplate

使用restTemplate进行服务调用

使用ribbon进行负载均衡加强

```java
package com.adun.springcloud.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ApplicationContextConfig {

    @Bean
    //restTemplate启用ribbon的负载均衡
    @LoadBalanced
    public RestTemplate getRestTemplate(){
        return new RestTemplate();
    }
}

```

相关项目结构

<img src="https://gitee.com/zhudunfeng/cloudimage/raw/master/image/202203081626925.png" alt="image-20220308162636836" style="zoom:67%;" />



### openFeign

底层封装ribbon,使用ribbon来进行负载均衡

主启动类

```java
package com.adun.springcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
//激活feign
@EnableFeignClients
public class OrderFeignMain80 {
    public static void main(String[] args) {
        SpringApplication.run(OrderFeignMain80.class, args);
    }
}

```

能力接口

```java
package com.adun.springcloud.service;

import com.adun.springcloud.entities.CommonResult;
import com.adun.springcloud.entities.Payment;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
@Component
@FeignClient(value = "CLOUD-PAYMENT-SERVICE")
public interface PaymentFeignService {

    @GetMapping(value = "/payment/get/{id}")
    public CommonResult<Payment> getPaymentById(@PathVariable("id") Long id);

    @GetMapping(value = "/payment/feign/timeout")
    public String paymentFeignTimeOut();

}

```

相关项目结构

<img src="https://gitee.com/zhudunfeng/cloudimage/raw/master/image/202203081627823.png" alt="image-20220308162730725" style="zoom:67%;" />

## 服务熔断降级与限流

### hystrix

1. 生产者配置降级与熔断【熔断器，降级处理，有明显区别】
2. 消费者配置降级【降级推荐配置到客户】
3. 监控面板需要配合actuator进行使用



相关项目结构

<img src="https://gitee.com/zhudunfeng/cloudimage/raw/master/image/202203081641968.png" alt="image-20220308164134879" style="zoom:67%;" />

