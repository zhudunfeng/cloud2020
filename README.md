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


## 服务配置中心



<img src="https://gitee.com/zhudunfeng/cloudimage/raw/master/image/202203131700645.png" alt="image-20220313170052544" style="zoom:67%;" />

### Spring Cloud Config

#### configServer

##### pom.xml

```xml
 <dependency>
     <groupId>org.springframework.cloud</groupId>
     <artifactId>spring-cloud-config-server</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

##### application.yml

```yaml
spring:
  application:
    name:  cloud-config-center #注册进Eureka服务器的微服务名
  cloud:
    config:
      server:
        git:
          uri: git@github.com:adun/springcloud-config.git #GitHub上面的git仓库名字
        ####搜索目录
          search-paths:
            - springcloud-config
      ####读取分支
      label: master

```

##### 主启动类

```java
@EnableConfigServer
```



#### 微服务客户实例

##### pom.xml

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-config</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

##### bootstrap.yml

```yaml
server:
  port: 3355

spring:
  application:
    name: config-client
  cloud:
    #Config客户端配置
    config:
      label: master #分支名称
      name: config #配置文件名称
      profile: dev #读取后缀名称   上述3个综合：master分支上config-dev.yml的配置文件被读取http://config-3344.com:3344/master/config-dev.yml
      uri: http://localhost:3344 #配置中心地址k

#服务注册到eureka地址
eureka:
  client:
    service-url:
      defaultZone: http://localhost:7001/eureka
# 暴露监控端点
management:
  endpoints:
    web:
      exposure:
        include: "*"
```

##### 需要刷新的类，添加注解

```java
//可动态刷新配置
@RefreshScope
```



### Spring Cloud Bus【底层使用消息中间件进行通知】

> Spring Cloud Bus能管理和传播分布式系统间的消息，就像一个分布式执行器，可用于广播状态更改、事件推送等，也可以当作微服务间的通信通道。

Spring Cloud Bus 配合 Spring Cloud Config 使用可以实现配置的动态刷新。

==Bus支持两种消息代理：RabbitMQ 和 Kafka==

可以通过引入不同的坐标进行选择

> `spring-cloud-starter-bus-amqp` or `spring-cloud-starter-bus-kafka`



#### 什么是总线

在微服务架构的系统中，==通常会使用轻量级的消息代理来构建一个共用的消息主题==，并让系统中所有微服务实例都连接上来。==由于该主题中产生的消息会被所有实例监听和消费，所以称它为消息总线。==在总线上的各个实例，都可以方便地广播一些需要让其他连接在该主题上的实例都知道的消息。

#### 基本原理

ConfigClient实例都监听MQ中同一个topic(默认是springCloudBus)。当一个服务刷新数据的时候，它会把这个信息放入到Topic中，这样其它监听同一Topic的服务就能得到通知，然后去更新自身的配置。



#### 基本架构

![image-20220313164848797](https://gitee.com/zhudunfeng/cloudimage/raw/master/image/202203131648869.png)



#### 配置中心服务端

##### pom.xml

```xml
<!--添加消息总线RabbitMQ支持-->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-bus-amqp</artifactId>
</dependency>
<!--添加服务监控，用于服务刷新数据-->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

##### yml

```yaml
#rabbitmq相关配置
rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
    
##rabbitmq相关配置,暴露bus刷新配置的端点
management:
  endpoints: #暴露bus刷新配置的端点
    web:
      exposure:
        include: 'bus-refresh'
```

##### 主启动类

```java
//激活分布式配置服务中心
@EnableConfigServer
```



#### 配置中心客户端

##### pom.xml

```xml
<!--添加消息总线RabbitMQ支持-->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-bus-amqp</artifactId>
</dependency>
<!--添加服务监控，用于服务刷新数据-->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

##### bootstrap.yml

```yaml
#rabbitmq相关配置 15672是Web管理界面的端口；5672是MQ访问的端口
rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
# 暴露监控端点
management:
  endpoints:
    web:
      exposure:
        include: "*"   # 'refresh'

```

##### 需要动态刷新的类，添加注解

```java
//可动态刷新配置
@RefreshScope
```



## Spring Cloud Stream

> 引入目的：屏蔽底层消息中间件的差异,降低切换成本，统一消息的编程模型
