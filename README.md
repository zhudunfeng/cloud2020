

# SpringCloud

SpringCloud2020年的技术变更

## 关于Cloud各种组件的停更/升级/替换

SpringCloudNetflix组件大部分进入维护状态，从而Spring官方推荐进行一些相应组件的升级与替换

维护前的主要cloud组件

<img src="https://cloudimgs-1301504220.cos.ap-nanjing.myqcloud.com/image/202203081638521.png" alt="image-20220308163814442" style="zoom: 67%;" />

推荐替换的相应组件

![image-20220308163942294](https://cloudimgs-1301504220.cos.ap-nanjing.myqcloud.com/image/202203081639345.png)





## 微服务架构编码构建

### 未引入服务注册与发现组件实现服务调用(RestTemplate)

![image](https://user-images.githubusercontent.com/48040850/155866502-5e2966be-eae3-40c0-bbf6-b9aa3939d798.png)

> 此时，项目未引入SpringCloud的服务注册与发现组件，服务之间的调用使用restTemplate进行相互调用

（1）项目架构

```txt
cloud2020
	cloud-api-commons  服务提供与消费共同使用的相关类
	cloud-consumer-oreder80    服务消费80
	cloud-provider-payment8001 服务提供8001
```



![image-20220227111214723](https://cloudimgs-1301504220.cos.ap-nanjing.myqcloud.com/image/202202271112780.png)

（2）核心代码

cloud-provider-payment8001

```java
package com.adun.springcloud.service.impl;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Resource
    private PaymentDao paymentDao;

    @Override
    public int create(Payment payment) {
        return paymentDao.create(payment);
    }

    @Override
    public Payment getPaymentById(Long id) {
        return paymentDao.getPaymentById(id);
    }
}
```

cloud-consumer-oreder80

```java
package com.adun.springcloud.controller;

@RestController
@Slf4j
public class OrderController {

    public static final String PAYMENT_URL="http://localhost:8001";
    //引入服务注册中心后可以使用注释替换上方代码
    //public static final String PAYMENT_URL="http://CLOUD-PAYMENT-SERVICE";

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
}

```



## 微服务注册中心

1. 什么是服务治理　

   ​        Spring Cloud 封装了 Netflix 公司开发的 Eureka 模块来实现服务治理

   ​       在传统的rpc远程调用框架中，管理每个服务与服务之间依赖关系比较复杂，管理比较复杂，所以需要使用服务治理，==管理服务于服务之间依赖关系==，可以实现服务调用、负载均衡、容错等，实现服务发现与注册。

2. 什么是服务注册与发现
           Eureka采用了CS的设计架构，Eureka Server 作为服务注册功能的服务器，它是服务注册中心。而系统中的其他微服务，使用 Eureka的客户端连接到 Eureka Server并维持心跳连接。这样系统的维护人员就可以通过 Eureka Server 来监控系统中各个微服务是否正常运行。

   ​        在服务注册与发现中，有一个注册中心。当服务器启动的时候，会把当前自己服务器的信息 比如 服务地址通讯地址等以别名方式注册到注册中心上。另一方（消费者|服务提供者），以该别名的方式去注册中心上获取到实际的服务通讯地址，然后再实现本地RPC调用RPC远程调用框架核心设计思想：在于注册中心，因为使用注册中心管理每个服务与服务之间的一个依赖关系(服务治理概念)。在任何rpc远程框架中，都会有一个注册中心(存放服务地址相关信息(接口地址))

   ​	下左图是Eureka系统架构，右图是Dubbo的架构，请对比

   ![1660210993916](README.assets/1660210993916.png)

### Eureka项目相关

![1660215822047](README.assets/1660215822047.png)



Eureka包含两个组件：==Eureka Server==和==Eureka Client==

==Eureka Server提供服务注册服务==
各个微服务节点通过配置启动后，会在EurekaServer中进行注册，这样EurekaServer中的服务注册表中将会存储所有可用服务节点的信息，服务节点的信息可以在界面中直观看到。

==EurekaClient通过注册中心进行访问==
是一个Java客户端，用于简化Eureka Server的交互，客户端同时也具备一个内置的、使用轮询(round-robin)负载算法的负载均衡器。在应用启动后，将会向Eureka Server发送心跳(默认周期为30秒)。如果Eureka Server在多个心跳周期内没有接收到某个节点的心跳，EurekaServer将会从服务注册表中把这个服务节点移除（默认90秒）

#### 单机Eureka构建步骤

>此时，项目未引入SpringCloud的服务注册与发现组件，服务之间的调用使用restTemplate进行相互调用

##### 1、项目架构

```
cloud2020
	cloud-api-commons  服务提供与消费共同使用的相关类
	cloud-eureka-server7001    服务注册中心7001
	cloud-consumer-oreder80    服务消费80
	cloud-provider-payment8001 服务提供8001
```



##### 2、构建cloud-eureka-server7001

pom引入

```xml
<!--eureka-server-->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
</dependency>
```

yml修改

```yaml
server:
  port: 7001

eureka:
  instance:
    hostname: localhost #eureka服务端的实例名称
  client:
    #false表示不向注册中心注册自己。
    register-with-eureka: false
    #false表示自己端就是注册中心，我的职责就是维护服务实例，并不需要去检索服务
    fetch-registry: false
    service-url:
    #设置与Eureka Server交互的地址查询服务和注册服务都需要依赖这个地址。
      defaultZone: http://${eureka.instance.hostname}:${server.port}/eureka/
```

主启动类添加注解`@EnableEurekaServer`

```java
@SpringBootApplication
@EnableEurekaServer
public class EurekaMain7001
{
    public static void main(String[] args)
    {
        SpringApplication.run(EurekaMain7001.class,args);
    }
}
```

测试

http://localhost:7001/

![1660211707392](README.assets/1660211707392.png)







##### 3、EurekaClient端cloud-provider-payment8001

将注册进EurekaServer成为服务提供者provider

改POM

```xml
 <!--eureka-client-->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

修改yml

```yaml
server:
  port: 8001

spring:
  application:
    name: cloud-payment-service
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource            # 当前数据源操作类型
    driver-class-name: org.gjt.mm.mysql.Driver              # mysql驱动包
    url: jdbc:mysql://localhost:3306/db2019?useUnicode=true&characterEncoding=utf-8&useSSL=false
    username: root
    password: 123456

##eureka-client注册自己到eureka-server
eureka:
  client:
    #表示是否将自己注册进EurekaServer默认为true。
    register-with-eureka: true
    #是否从EurekaServer抓取已有的注册信息，默认为true。单节点无所谓，集群必须设置为true才能配合ribbon使用负载均衡
    fetchRegistry: true
    service-url:
      defaultZone: http://localhost:7001/eureka
```

修改主启动类添加注解`@EnableEurekaClient`

```java
@SpringBootApplication
@EnableEurekaClient
public class PaymentMain8001
{
    public static void main(String[] args)
    {
        SpringApplication.run(PaymentMain8001.class,args);
    }
}
```



##### 4、EurekaClient端cloud-consumer-order80

将注册进EurekaServer成为服务消费者consumer

改POM

```xml
 <!--eureka-client-->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

修改yml

```yaml
server:
  port: 80

spring:
    application:
        name: cloud-order-service

eureka:
  client:
    #表示是否将自己注册进EurekaServer默认为true。
    register-with-eureka: true
    #是否从EurekaServer抓取已有的注册信息，默认为true。单节点无所谓，集群必须设置为true才能配合ribbon使用负载均衡
    fetchRegistry: true
    service-url:
      defaultZone: http://localhost:7001/eurek
```

修改主启动类添加注解`@EnableEurekaClient`

```java
@SpringBootApplication
@EnableEurekaClient
public class OrderMain80
{
    public static void main(String[] args)
    {
        SpringApplication.run(PaymentMain8001.class,args);
    }
}
```



##### 5、测试

![1660212263255](README.assets/1660212263255.png)





#### 集群Eureka构建步骤

##### 1、基本项目架构

```
cloud2020
	cloud-api-commons  服务提供与消费共同使用的相关类
	cloud-eureka-server7001    服务注册中心7001
	cloud-eureka-server7002	   服务注册中线7002
	cloud-consumer-oreder80    服务消费80
	cloud-provider-payment8001 服务提供8001
	cloud-provider-payment8002 服务提供8002
```

<img src="https://cloudimgs-1301504220.cos.ap-nanjing.myqcloud.com/image/202202271342914.png" alt="image-20220227134225873" style="zoom:50%;" />



##### 2、eureka server集群基本原理与实现

![1660212472671](README.assets/1660212472671.png)

- [ ] 问题：微服务RPC远程服务调用最核心的是什么 ?
         高可用，试想你的注册中心只有一个only one， 它出故障了那就呵呵(￣▽￣)"了，会导致整个为服务环境不可用，所以

  ==解决办法：搭建Eureka注册中心集群 ，实现负载均衡+故障容错==

<img src="https://cloudimgs-1301504220.cos.ap-nanjing.myqcloud.com/image/202202271725855.png" alt="image-202202271725855" style="zoom:67%;" />

<img src="https://cloudimgs-1301504220.cos.ap-nanjing.myqcloud.com/image/202202271419150.png" alt="image-202202271419150" style="zoom:67%;" />



##### 3、EurekaServer集群环境构建步骤

cloud-eureka-server7001

cloud-eureka-server7002

改POM

```xml
<!--eureka-server-->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
</dependency>
```

写yml【7001与7002相互注册】

7001

```yaml
server:
  port: 7001


eureka:
  instance:
    hostname: eureka7001.com #eureka服务端的实例名称
  client:
    register-with-eureka: false     #false表示不向注册中心注册自己。
    fetch-registry: false     #false表示自己端就是注册中心，我的职责就是维护服务实例，并不需要去检索服务
    service-url:
      defaultZone: http://eureka7002.com:7002/eureka/
```

7002

```yaml
server:
  port: 7002


eureka:
  instance:
    hostname: eureka7002.com #eureka服务端的实例名称
  client:
    register-with-eureka: false     #false表示不向注册中心注册自己。
    fetch-registry: false     #false表示自己端就是注册中心，我的职责就是维护服务实例，并不需要去检索服务
    service-url:
      defaultZone: http://eureka7001.com:7001/eureka/
```

主启动

```java
@SpringBootApplication
@EnableEurekaServer
public class EurekaMain7002
{
    public static void main(String[] args)
    {
        SpringApplication.run(EurekaMain7002.class,args);
    }
}
```



##### 4、将支付服务8001微服务发布到上面2台Eureka集群配置中

改yml

```yaml
server:
  port: 8001

spring:
  application:
    name: cloud-payment-service
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource            # 当前数据源操作类型
    driver-class-name: org.gjt.mm.mysql.Driver              # mysql驱动包
    url: jdbc:mysql://localhost:3306/db2019?useUnicode=true&characterEncoding=utf-8&useSSL=false
    username: root
    password: 123456


eureka:
  client:
    #表示是否将自己注册进EurekaServer默认为true。
    register-with-eureka: true
    #是否从EurekaServer抓取已有的注册信息，默认为true。单节点无所谓，集群必须设置为true才能配合ribbon使用负载均衡
    fetchRegistry: true
    service-url:
      #defaultZone: http://localhost:7001/eureka
      defaultZone: http://eureka7001.com:7001/eureka,http://eureka7002.com:7002/eureka  # 集群版

mybatis:
  mapperLocations: classpath:mapper/*.xml
  type-aliases-package: com.atguigu.springcloud.entities    # 所有Entity别名类所在包
```





##### 5、将订单服务80微服务发布到上面2台Eureka集群配置中

改yml

```yaml
server:
  port: 80

spring:
    application:
        name: cloud-order-service

eureka:
  client:
    #表示是否将自己注册进EurekaServer默认为true。
    register-with-eureka: true
    #是否从EurekaServer抓取已有的注册信息，默认为true。单节点无所谓，集群必须设置为true才能配合ribbon使用负载均衡
    fetchRegistry: true
    service-url:
      #defaultZone: http://localhost:7001/eureka
      defaultZone: http://eureka7001.com:7001/eureka,http://eureka7002.com:7002/eureka  # 集群版
```



##### 6、测试01

![1660213332102](README.assets/1660213332102.png)





##### 7、负载均衡环境搭建测试

###### （1）支付服务提供者8001/8002集群环境构建

修改yml【注册中心为集群需要特别配置】

> 是否从EurekaServer抓取已有的注册信息，默认为true。单节点无所谓，集群必须设置为true才能配合ribbon使用负载均衡
>     fetchRegistry: true

```yaml
server:
  port: 8002

spring:
  application:
    name: cloud-payment-service
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource            # 当前数据源操作类型
    driver-class-name: org.gjt.mm.mysql.Driver              # mysql驱动包
    url: jdbc:mysql://localhost:3306/db2019?useUnicode=true&characterEncoding=utf-8&useSSL=false
    username: root
    password: 123456

eureka:
  client:
    #表示是否将自己注册进EurekaServer默认为true。
    register-with-eureka: true
    #是否从EurekaServer抓取已有的注册信息，默认为true。单节点无所谓，集群必须设置为true才能配合ribbon使用负载均衡
    fetchRegistry: true
    service-url:
      defaultZone: http://eureka7001.com:7001/eureka,http://eureka7002.com:7002/eureka  # 集群版
      #defaultZone: http://localhost:7001/eureka  # 单机版


mybatis:
  mapperLocations: classpath:mapper/*.xml
  type-aliases-package: com.atguigu.springcloud.entities    # 所有Entity别名类所在包

```

修改8001/8002的Controller,返回当前服务的端口名

```java

@RestController
@Slf4j
public class PaymentController
{
    @Value("${server.port}")
    private String serverPort;

    @Resource
    private PaymentService paymentService;

    @PostMapping(value = "/payment/create")
    public CommonResult create(@RequestBody Payment payment)
    {
        int result = paymentService.create(payment);
        log.info("*****插入操作返回结果:" + result);

        if(result > 0)
        {
            return new CommonResult(200,"插入成功,返回结果"+result+"\t 服务端口："+serverPort,payment);
        }else{
            return new CommonResult(444,"插入失败",null);
        }
    }

    @GetMapping(value = "/payment/get/{id}")
    public CommonResult<Payment> getPaymentById(@PathVariable("id") Long id)
    {
        Payment payment = paymentService.getPaymentById(id);
        log.info("*****查询结果:{}",payment);
        if (payment != null) {
            return new CommonResult(200,"查询成功"+"\t 服务端口："+serverPort,payment);
        }else{
            return new CommonResult(444,"没有对应记录,查询ID: "+id,null);
        }
    }
}
```



###### （2）订单服务访问地址不能写死

```java
//public static final String PAYMENT_SRV = "http://localhost:8001";
 
// 通过在eureka上注册过的微服务名称调用
public static final String PAYMENT_SRV = "http://CLOUD-PAYMENT-SERVICE";
```



###### （3）使用@LoadBalanced注解赋予RestTemplate负载均衡的能力

> 提前说一下Ribbon的负载均衡功能: LoadBalanced底层与ribbon相似，使用方式一致

```java
@Configuration
public class ApplicationContextBean
{
    @Bean
    @LoadBalanced //使用@LoadBalanced注解赋予RestTemplate负载均衡的能力
    public RestTemplate getRestTemplate()
    {
        return new RestTemplate();
    }
}
```



###### （4）测试02

![1660214046026](README.assets/1660214046026.png)







#### actuator微服务信息完善

###### （1）主机名称:服务名称修改

问题

没有主机名

![1660214298878](README.assets/1660214298878.png)

修改yml

```yaml
eureka:
  client:
    #表示是否将自己注册进EurekaServer默认为true。
    register-with-eureka: true
    #是否从EurekaServer抓取已有的注册信息，默认为true。单节点无所谓，集群必须设置为true才能配合ribbon使用负载均衡
    fetchRegistry: true
    service-url:
      defaultZone: http://eureka7001.com:7001/eureka,http://eureka7002.com:7002/eureka  # 集群版
      #defaultZone: http://localhost:7001/eureka  # 单机版
  #    
  instance:
    instance-id: payment8001
```

![1660214366193](README.assets/1660214366193.png)



###### （2）访问信息有IP信息提示

问题

没有IP提示

修改yml

```yaml
eureka:
  client:
    #表示是否将自己注册进EurekaServer默认为true。
    register-with-eureka: true
    #是否从EurekaServer抓取已有的注册信息，默认为true。单节点无所谓，集群必须设置为true才能配合ribbon使用负载均衡
    fetchRegistry: true
    service-url:
      defaultZone: http://eureka7001.com:7001/eureka,http://eureka7002.com:7002/eureka  # 集群版
      #defaultZone: http://localhost:7001/eureka  # 单机版
  instance:
    instance-id: payment8001
    prefer-ip-address: true     #访问路径可以显示IP地址
```



![1660214472156](README.assets/1660214472156.png)



#### 服务发现Discovery

> 对于注册进eureka里面的微服务，可以通过服务发现来获得该服务的信息

##### (1)修改cloud-provider-payment8001的Controller

```java
package com.atguigu.springcloud.controller;

import com.atguigu.springcloud.entities.CommonResult;
import com.atguigu.springcloud.entities.Payment;
import com.atguigu.springcloud.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @auther zzyy
 * @create 2020-01-27 21:17
 */
@RestController
@Slf4j
public class PaymentController
{
    @Value("${server.port}")
    private String serverPort;

    @Resource
    private PaymentService paymentService;

    //服务发现相关
    @Resource
    private DiscoveryClient discoveryClient;

    @PostMapping(value = "/payment/create")
    public CommonResult create(@RequestBody Payment payment)
    {
        int result = paymentService.create(payment);
        log.info("*****插入操作返回结果:" + result);

        if(result > 0)
        {
            return new CommonResult(200,"插入成功,返回结果"+result+"\t 服务端口："+serverPort,payment);
        }else{
            return new CommonResult(444,"插入失败",null);
        }
    }

    @GetMapping(value = "/payment/get/{id}")
    public CommonResult<Payment> getPaymentById(@PathVariable("id") Long id)
    {
        Payment payment = paymentService.getPaymentById(id);
        log.info("*****查询结果:{}",payment);
        if (payment != null) {
            return new CommonResult(200,"查询成功"+"\t 服务端口："+serverPort,payment);
        }else{
            return new CommonResult(444,"没有对应记录,查询ID: "+id,null);
        }
    }

    //服务发现相关
    @GetMapping(value = "/payment/discovery")
    public Object discovery()
    {
        List<String> services = discoveryClient.getServices();
        for (String element : services) {
            System.out.println(element);
        }

        List<ServiceInstance> instances = discoveryClient.getInstances("CLOUD-PAYMENT-SERVICE");
        for (ServiceInstance element : instances) {
            System.out.println(element.getServiceId() + "\t" + element.getHost() + "\t" + element.getPort() + "\t"
                    + element.getUri());
        }
        return this.discoveryClient;
    }
}
 
 

```



##### （2）8001主启动类添加注解`@EnableDiscoveryClient`

```java
@SpringBootApplication
@EnableEurekaClient
@EnableDiscoveryClient //服务发现【可以平替@EnableEurekaClient】
public class PaymentMain8001
{
    public static void main(String[] args)
    {
        SpringApplication.run(PaymentMain8001.class,args);
    }
}

```

##### （3）自测

![1660214912458](README.assets/1660214912458.png)



#### Eureka自我保护

##### 1、故障现象

概述
保护模式主要用于一组客户端和Eureka Server之间存在网络分区场景下的保护。一旦进入保护模式，
==Eureka Server将会尝试保护其服务注册表中的信息，不再删除服务注册表中的数据，也就是不会注销任何微服务。==

如果在Eureka Server的首页看到以下这段提示，则说明Eureka进入了保护模式：
EMERGENCY! EUREKA MAY BE INCORRECTLY CLAIMING INSTANCES ARE UP WHEN THEY'RE NOT. 
RENEWALS ARE LESSER THAN THRESHOLD AND HENCE THE INSTANCES ARE NOT BEING EXPIRED JUST TO BE SAFE 

![1660215009699](README.assets/1660215009699.png)

 

##### 2、导致原因

>总结：
>
>一句话：某时刻某一个微服务不可用了，Eureka不会立刻清理，依旧会对该微服务的信息进行保存
>
>属于CAP里面的AP分支

为什么会产生Eureka自我保护机制？

为了防止EurekaClient可以正常运行，但是 与 EurekaServer网络不通情况下，EurekaServer不会立刻将EurekaClient服务剔除



什么是自我保护模式？

默认情况下，如果EurekaServer在一定时间内没有接收到某个微服务实例的心跳，EurekaServer将会注销该实例（默认90秒）。但是当网络分区故障发生(延时、卡顿、拥挤)时，微服务与EurekaServer之间无法正常通信，以上行为可能变得非常危险了——因为微服务本身其实是健康的，此时本不应该注销这个微服务。Eureka通过“自我保护模式”来解决这个问题——当EurekaServer节点在短时间内丢失过多客户端时（可能发生了网络分区故障），那么这个节点就会进入自我保护模式。


在自我保护模式中，Eureka Server会保护服务注册表中的信息，不再注销任何服务实例。
它的设计哲学就是宁可保留错误的服务注册信息，也不盲目注销任何可能健康的服务实例。一句话讲解：好死不如赖活着

综上，自我保护模式是一种应对网络异常的安全保护措施。它的架构哲学是宁可同时保留所有微服务（健康的微服务和不健康的微服务都会保留）也不盲目注销任何健康的微服务。使用自我保护模式，可以让Eureka集群更加的健壮、稳定。

 ![1660215089163](README.assets/1660215089163.png)

#####  3、怎么禁止自我保护

###### （1）注册中心eureakeServer端7001

![1660215199235](README.assets/1660215199235.png)

修改yml

```yaml
server:
  port: 7001

spring:
  application:
    name: eureka-cluster-server

eureka:
  instance:
    hostname: eureka7001.com
  client:
    register-with-eureka: false
    fetch-registry: false
    service-url:
      #defaultZone: http://eureka7002.com:7002/eureka,http://eureka7003.com:7003/eureka
      defaultZone: http://eureka7001.com:7001/eureka
  server:
    #关闭自我保护机制，保证不可用服务被及时踢除
       enable-self-preservation: false
    eviction-interval-timer-in-ms: 2000
```

 关闭效果

![1660215254444](README.assets/1660215254444.png)

######  

###### （2）生产者客户端eureakeClient端8001

![1660215323695](README.assets/1660215323695.png)

yml

```yaml
server:
  port: 8001

###服务名称(服务注册到eureka名称)
spring:
    application:
        name: cloud-provider-payment

eureka:
  client: #服务提供者provider注册进eureka服务列表内
    service-url:
      register-with-eureka: true
      fetch-registry: true
      # cluster version
      #defaultZone: http://eureka7001.com:7001/eureka,http://eureka7002.com:7002/eureka,http://eureka7003.com:7003/eureka
      # singleton version
      defaultZone: http://eureka7001.com:7001/eureka
#心跳检测与续约时间
#开发时设置小些，保证服务关闭后注册中心能即使剔除服务
  instance:
  #Eureka客户端向服务端发送心跳的时间间隔，单位为秒(默认是30秒)
    lease-renewal-interval-in-seconds: 1
  #Eureka服务端在收到最后一次心跳后等待时间上限，单位为秒(默认是90秒)，超时将剔除服务
    lease-expiration-duration-in-seconds: 2
```

 

### zookeeper相关项目

![1660215864730](README.assets/1660215864730.png)



#### 1、基本项目架构

注册中心zookeeper需要安装到linux

![1660215992738](README.assets/1660215992738.png)

```txt
cloud2020
	cloud-provider-payment8004		服务提供者8004
	cloud-consumerzk-order80		服务消费者80
```

<img src="https://cloudimgs-1301504220.cos.ap-nanjing.myqcloud.com/image/202202272257070.png" alt="image-20220227225729931" style="zoom:80%;" />

#### 2、项目搭建

##### （1）新建cloud-provider-payment8004

改pom【zookeeper版本必须与自己安装的一致】

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <parent>
        <artifactId>cloud2020</artifactId>
        <groupId>com.adun.springcloud</groupId>
        <version>1.0-SNAPSHOT</version>
    </parent>
    <modelVersion>4.0.0</modelVersion>
    <artifactId>cloud-provider-payment8004</artifactId>


    <dependencies>
        <!-- SpringBoot整合Web组件 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency><!-- 引入自己定义的api通用包，可以使用Payment支付Entity -->
            <groupId>com.adun.springcloud</groupId>
            <artifactId>cloud-api-commons</artifactId>
            <version>${project.version}</version>
        </dependency>

        <!-- SpringBoot整合zookeeper客户端 -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-zookeeper-discovery</artifactId>
            <!--先排除自带的zookeeper3.5.3-->
            <exclusions>
                <exclusion>
                    <groupId>org.apache.zookeeper</groupId>
                    <artifactId>zookeeper</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
        <!--添加zookeeper3.4.9版本-->
        <dependency>
            <groupId>org.apache.zookeeper</groupId>
            <artifactId>zookeeper</artifactId>
            <version>3.4.9</version>
        </dependency>


        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <scope>runtime</scope>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>

```

写yml

```yaml
#8004表示注册到zookeeper服务器的支付服务提供者端口号
server:
  port: 8004
#服务别名----注册zookeeper到注册中心名称
spring:
  application:
    name: cloud-provider-payment
  cloud:
    zookeeper:
      connect-string: 192.168.111.144:2181
```

主启动

```java
@SpringBootApplication
@EnableDiscoveryClient //该注解用于向使用consul或者zookeeper作为注册中心时注册服务
public class PaymentMain8004
{
    public static void main(String[] args)
    {
        SpringApplication.run(PaymentMain8004.class,args);
    }

}
```

业务类

```java
@RestController
public class PaymentController
{
    @Value("${server.port}")
    private String serverPort;

    @RequestMapping(value = "/payment/zk")
    public String paymentzk()
    {
        return "springcloud with zookeeper: "+serverPort+"\t"+ UUID.randomUUID().toString();
    }
}
```

验证测试

http://localhost:8004/payment/zk

![1660216388381](README.assets/1660216388381.png)



验证测试2

![1660216470623](README.assets/1660216470623.png)



思考：服务节点是临时节点还是持久节点

临时节点



##### （2）新建cloud-consumerzk-order80

改pom

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <parent>
        <artifactId>cloud2020</artifactId>
        <groupId>com.adun.springcloud</groupId>
        <version>1.0-SNAPSHOT</version>
    </parent>
    <modelVersion>4.0.0</modelVersion>

    <artifactId>cloud-consumerzk-order81</artifactId>


    <dependencies>
        <!-- SpringBoot整合Web组件 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <!-- SpringBoot整合zookeeper客户端 -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-zookeeper-discovery</artifactId>
            <!--先排除自带的zookeeper-->
            <exclusions>
                <exclusion>
                    <groupId>org.apache.zookeeper</groupId>
                    <artifactId>zookeeper</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
        <!--添加zookeeper3.4.9版本-->
        <dependency>
            <groupId>org.apache.zookeeper</groupId>
            <artifactId>zookeeper</artifactId>
            <version>3.4.9</version>
        </dependency>

        <!--热部署-->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <scope>runtime</scope>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

写yml

```yaml
server:
  port: 80

spring:
  application:
    name: cloud-consumer-order
  cloud:
  #注册到zookeeper地址
    zookeeper:
      connect-string: 192.168.111.144:2181
```

主启动

```java
@SpringBootApplication
@EnableDiscoveryClient
public class OrderZK80 {
    public static void main(String[] args) {
        SpringApplication.run(OrderZK80.class, args);
    }
}
```

业务类

配置bean

```java
@Configuration
public class ApplicationContextBean
{
    @Bean
    @LoadBalanced
    public RestTemplate getRestTemplate()
    {
        return new RestTemplate();
    }
}
```

controller

```java
@RestController
public class OrderZKController
{
    public static final String INVOKE_URL = "http://cloud-provider-payment";

    @Autowired
    private RestTemplate restTemplate;

    @RequestMapping(value = "/consumer/payment/zk")
    public String paymentInfo()
    {
        String result = restTemplate.getForObject(INVOKE_URL+"/payment/zk", String.class);
        System.out.println("消费者调用支付服务(zookeeper)--->result:" + result);
        return result;
    }

}

```

验证测试

![1660216702504](README.assets/1660216702504.png)



访问测试地址

http://localhost/consumer/payment/zk



### consul相关项目

![1660216987745](README.assets/1660216987745.png)

#### 基本项目架构

```txt
cloud2020
	cloud-providerconsul-payment8006	服务提供者8006
	cloud-consumerconsul-order80		服务消费者80
```

<img src="https://cloudimgs-1301504220.cos.ap-nanjing.myqcloud.com/image/202202272259322.png" alt="image-20220227225915255" style="zoom:80%;" />

#### 1、原理

> https://www.consul.io/intro/index.html

##### 是什么

![1660217067343](README.assets/1660217067343.png)

Consul 是一套开源的分布式服务发现和配置管理系统，由 HashiCorp 公司用 ==Go 语言开发==。

提供了微服务系统中的服务治理、配置中心、控制总线等功能。这些功能中的每一个都可以根据需要单独使用，也可以一起使用以构建全方位的服务网格，总之Consul提供了一种完整的服务网格解决方案。

它具有很多优点。包括： 基于 raft 协议，比较简洁； 支持健康检查, 同时支持 HTTP 和 DNS 协议 支持跨数据中心的 WAN 集群 提供图形界面 跨平台，支持 Linux、Mac、Windows



##### 做什么

![1660217240530](README.assets/1660217240530.png)

Spring Cloud Consul 具有如下特性：

![1660217221161](README.assets/1660217221161.png)

##### 怎么用

https://www.springcloud.cc/spring-cloud-consul.html



#### 2、安装并运行Consul

![1660217391535](README.assets/1660217391535.png)

查看版本

![1660217406773](README.assets/1660217406773.png)

结果页面

![1660217425565](README.assets/1660217425565.png)

#### 3、项目搭建

（1）新建Module支付服务provider8006

改pom

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <parent>
        <artifactId>cloud2020</artifactId>
        <groupId>com.adun.springcloud</groupId>
        <version>1.0-SNAPSHOT</version>
    </parent>
    <modelVersion>4.0.0</modelVersion>


    <artifactId>cloud-providerconsul-payment8006</artifactId>


    <dependencies>
        <!--SpringCloud consul-server -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-consul-discovery</artifactId>
        </dependency>
        <!-- SpringBoot整合Web组件 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <!--日常通用jar包配置-->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <scope>runtime</scope>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

写yml

```yaml
###consul服务端口号
server:
  port: 8006

spring:
  application:
    name: consul-provider-payment
  ####consul注册中心地址
  cloud:
    consul:
      host: localhost
      port: 8500
      discovery:
        #hostname: 127.0.0.1
        service-name: ${spring.application.name}

```

主启动

```java
@SpringBootApplication
@EnableDiscoveryClient //该注解用于向使用consul或者zookeeper作为注册中心时注册服务
public class PaymentMain8006 {

    public static void main(String[] args) {
        SpringApplication.run(PaymentMain8006.class, args);
    }

}
```

业务类

```java
@RestController
public class PaymentController
{
    @Value("${server.port}")
    private String serverPort;

    @RequestMapping(value = "/payment/consul")
    public String paymentzk()
    {
        return "springcloud with consul: "+serverPort+"\t"+ UUID.randomUUID().toString();
    }
}

```

验证测试

http://localhost:8006/payment/consul

![1660217820544](README.assets/1660217820544.png)



（2）新建Module消费服务order80

改pom

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <parent>
        <artifactId>cloud2020</artifactId>
        <groupId>com.adun.springcloud</groupId>
        <version>1.0-SNAPSHOT</version>
    </parent>
    <modelVersion>4.0.0</modelVersion>

    <artifactId>cloud-consumerconsul-order80</artifactId>

    <dependencies>
        <!--common-->
        <dependency>
            <groupId>com.adun.springcloud</groupId>
            <artifactId>cloud-api-commons</artifactId>
            <version>${project.version}</version>
        </dependency>

        <!--SpringCloud consul-server -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-consul-discovery</artifactId>
        </dependency>
        <!-- SpringBoot整合Web组件 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <!--日常通用jar包配置-->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <scope>runtime</scope>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>

```

写yml

```yaml
###consul服务端口号
server:
  port: 80

spring:
  application:
    name: cloud-consumer-order
  ####consul注册中心地址
  cloud:
    consul:
      host: localhost
      port: 8500
      discovery:
        #hostname: 127.0.0.1
        service-name: ${spring.application.name}
```

主启动

```java
@SpringBootApplication
@EnableDiscoveryClient
public class OrderConsulMain80 {
    public static void main(String[] args) {
        SpringApplication.run(OrderConsulMain80.class, args);
    }
}
```

业务类

配置bean

```java
@Configuration
public class ApplicationContextBean
{
    @Bean
    @LoadBalanced
    public RestTemplate getRestTemplate()
    {
        return new RestTemplate();
    }
}
```

controller

```java
@RestController
public class OrderConsulController
{
    public static final String INVOKE_URL = "http://cloud-provider-payment"; //consul-provider-payment

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping(value = "/consumer/payment/consul")
    public String paymentInfo()
    {
        String result = restTemplate.getForObject(INVOKE_URL+"/payment/consul", String.class);
        System.out.println("消费者调用支付服务(consule)--->result:" + result);
        return result;
    }
}

```

验证测试

http://localhost/consumer/payment/consul

![1660217979128](README.assets/1660217979128.png)

### 三个注册中心的异同点

![1660218033204](README.assets/1660218033204.png)

##### 经典CAP

==最多只能同时较好的满足两个。==
 CAP理论的核心是：==一个分布式系统不可能同时很好的满足一致性，可用性和分区容错性这三个需求，==

因此，根据 CAP 原理将 NoSQL 数据库分成了满足 CA 原则、满足 CP 原则和满足 AP 原则三 大类：
CA - 单点集群，满足一致性，可用性的系统，通常在可扩展性上不太强大。
CP - 满足一致性，分区容忍必的系统，通常性能不是特别高。
AP - 满足可用性，分区容忍性的系统，通常可能对一致性要求低一些。

<img src="https://cloudimgs-1301504220.cos.ap-nanjing.myqcloud.com/image/202202272308504.png" alt="image-20220227230802462" style="zoom:67%;" />

<img src="https://cloudimgs-1301504220.cos.ap-nanjing.myqcloud.com/image/202202272309193.png" alt="image-20220227230944151" style="zoom:67%;" />





#### AP(Eureka)

AP架构
当网络分区出现后，为了保证可用性，系统B==可以返回旧值==，保证系统的可用性。
==结论：违背了一致性C的要求，只满足可用性和分区容错，即AP==

![1660218227773](README.assets/1660218227773.png)



#### CP(Zookeeper/Consul)

CP架构
当网络分区出现后，为了保证一致性，就必须拒接请求，否则无法保证一致性
==结论：违背了可用性A的要求，只满足一致性和分区容错，即CP==

![1660218259751](README.assets/1660218259751.png)





## 微服务负载均衡组件

### Ribbon负载均衡服务调用

![1660218861061](README.assets/1660218861061.png)

#### 基本项目架构

```
cloud2020
	cloud-api-commons  服务提供与消费共同使用的相关类
	cloud-eureka-server7001    服务注册中心7001
	cloud-eureka-server7002	   服务注册中线7002
	cloud-consumer-oreder80    服务消费80
	cloud-provider-payment8001 服务提供8001
	cloud-provider-payment8002 服务提供8002
```

#### 1、概述

##### （1）是什么

Spring Cloud Ribbon是基于Netflix Ribbon实现的一套==客户端       负载均衡的工具==。

简单的说，Ribbon是Netflix发布的开源项目，主要功能是提供==客户端的软件负载均衡算法和服务调用==。Ribbon客户端组件提供一系列完善的配置项如连接超时，重试等。简单的说，就是在配置文件中列出Load Balancer（简称LB）后面所有的机器，Ribbon会自动的帮助你基于某种规则（如简单轮询，随机连接等）去连接这些机器。我们很容易使用Ribbon实现自定义的负载均衡算法。



##### （2）官网资料

https://github.com/Netflix/ribbon/wiki/Getting-Started

Ribbon目前也进入维护模式

![1660219009331](README.assets/1660219009331.png)

未来替换方案

![](README.assets/图片.jpeg)



##### （3）能做什么

![1660219258563](README.assets/1660219258563.png)

集中式LB

即在服务的消费方和提供方之间使用独立的LB设施(可以是硬件，如F5, 也可以是软件，如nginx), 由该设施负责把访问请求通过某种策略转发至服务的提供方；



进程内LB

将LB逻辑集成到消费方，消费方从服务注册中心获知有哪些地址可用，然后自己再从这些地址中选择出一个合适的服务器。

==Ribbon就属于进程内LB==，它只是一个类库，==集成于消费方进程==，消费方通过它来获取到服务提供方的地址。



#### 2、Ribbon负载均衡演示

##### （1）架构说明

![1660228668284](README.assets/1660228668284.png)



Ribbon在工作时分成两步

第一步先选择 EurekaServer ,它优先选择在同一个区域内负载较少的server.

第二步再根据用户指定的策略，==在从server取到的服务注册列表中选择一个地址==。
其中Ribbon提供了多种策略：比如轮询、随机和根据响应时间加权。

总结：Ribbon其实就是一个软负载均衡的客户端组件，
他可以和其他所需请求的客户端结合使用，和eureka结合只是其中的一个实例



##### （2）pom说明【springboot的eureka-client的starter集成了ribbon】

之前写样例时候没有引入spring-cloud-starter-ribbon也可以使用ribbon,

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-ribbon</artifactId>
</dependency>
```

猜测spring-cloud-starter-netflix-eureka-client自带了spring-cloud-starter-ribbon引用，
证明如下： ==可以看到spring-cloud-starter-netflix-eureka-client 确实引入了Ribbon==

![1660228926991](README.assets/1660228926991.png)



##### （3）使用RestTemplate+Eurka+Ribbon实现负载均衡

> 查看: 
>
> Eureka项目相关 》集群Eureka构建步骤》负载均衡环境搭建测试



#### 3、Ribbon核心组件IRule

##### （1）IRule：根据特定算法中从服务列表中选取一个要访问的服务

![1660229915271](README.assets/1660229915271.png)

![1660229958538](README.assets/1660229958538.png)

```java
com.netflix.loadbalancer.RoundRobinRule		轮询

com.netflix.loadbalancer.RandomRule			随机

com.netflix.loadbalancer.RetryRule			
先按照RoundRobinRule的策略获取服务，如果获取服务失败则在指定时间内会进行重试，获取可用的服务

WeightedResponseTimeRule					
对RoundRobinRule的扩展，响应速度越快的实例选择权重越大，越容易被选择

BestAvailableRule
会先过滤掉由于多次访问故障而处于断路器跳闸状态的服务，然后选择一个并发量最小的服务

AvailabilityFilteringRule
先过滤掉故障实例，再选择并发较小的实例

ZoneAvoidanceRule
默认规则,复合判断server所在区域的性能和server的可用性选择服务器
```

##### (2)如何替换(自定义负载均衡算法)

![1660230286826](README.assets/1660230286826.png)



###### （1）注意配置细节

> 官方文档明确给出了警告：
> 这个自定义配置类不能放在@ComponentScan所扫描的当前包下以及子包下，
> 否则我们自定义的这个配置类就会被所有的Ribbon客户端所共享，达不到特殊化定制的目的了

![1660230309168](README.assets/1660230309168.png)

###### （2）新建package【注意位置】

![1660230452595](README.assets/1660230452595.png)





###### （3）上面包下新建MySelfRule规则类

```java
@Configuration
public class MySelfRule
{
    @Bean
    public IRule myRule()
    {
        return new RandomRule();//定义为随机
    }
}
```

###### （4）主启动类添加`@RibbonClient`

```java
//自定义ribbon负载均衡算法
//分别指定多个服务调用负载均衡算法
//@RibbonClients(value = {
//        @RibbonClient(name = "CLOUD-PAYMENT-SERVICE",configuration= MySelfRule.class),
//        @RibbonClient(name = "CLOUD-PAYMENT-SERVICE",configuration= MySelfRule.class)})
//指定单个服务调用负载均衡算法
@RibbonClient(name = "CLOUD-PAYMENT-SERVICE",configuration= MySelfRule.class)
public class OrderMain80 {
    public static void main(String[] args) {
        SpringApplication.run(OrderMain80.class, args);
    }
}
```

###### （5）测试

http://localhost/consumer/payment/get/31







#### 4、Ribbon负载均衡算法

![1660230777089](README.assets/1660230777089.png)



##### （1）原理

==负载均衡算法：rest接口第几次请求数 % 服务器集群总数量 = 实际调用服务器位置下标  ，每次服务重启动后rest接口计数从1开始。==

`List<ServiceInstance> instances = discoveryClient.getInstances("CLOUD-PAYMENT-SERVICE");`

如：   List [0] instances = 127.0.0.1:8002
　　　List [1] instances = 127.0.0.1:8001

8001+ 8002 组合成为集群，它们共计2台机器，集群总数为2， 按照轮询算法原理：

当总请求数为1时： 1 % 2 =1 对应下标位置为1 ，则获得服务地址为127.0.0.1:8001
当总请求数位2时： 2 % 2 =0 对应下标位置为0 ，则获得服务地址为127.0.0.1:8002
当总请求数位3时： 3 % 2 =1 对应下标位置为1 ，则获得服务地址为127.0.0.1:8001
当总请求数位4时： 4 % 2 =0 对应下标位置为0 ，则获得服务地址为127.0.0.1:8002
如此类推......

##### (2)RoundRobinRule源码

```java
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.netflix.loadbalancer;

import com.netflix.client.config.IClientConfig;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RoundRobinRule extends AbstractLoadBalancerRule {
    private AtomicInteger nextServerCyclicCounter;
    private static final boolean AVAILABLE_ONLY_SERVERS = true;
    private static final boolean ALL_SERVERS = false;
    private static Logger log = LoggerFactory.getLogger(RoundRobinRule.class);

    public RoundRobinRule() {
        this.nextServerCyclicCounter = new AtomicInteger(0);
    }

    public RoundRobinRule(ILoadBalancer lb) {
        this();
        this.setLoadBalancer(lb);
    }

    public Server choose(ILoadBalancer lb, Object key) {
        if (lb == null) {
            log.warn("no load balancer");
            return null;
        } else {
            Server server = null;
            int count = 0;

            while(true) {
                if (server == null && count++ < 10) {
                    //获取所有启用并可访问的服务器
                    List<Server> reachableServers = lb.getReachableServers();
                    //获取所有已知服务器（可达和不可达的）
                    List<Server> allServers = lb.getAllServers();
                    //可达服务器总数
                    int upCount = reachableServers.size();
                    //所有服务器总数
                    int serverCount = allServers.size();
                    if (upCount != 0 && serverCount != 0) {
                        int nextServerIndex = this.incrementAndGetModulo(serverCount);
                        server = (Server)allServers.get(nextServerIndex);
                        if (server == null) {
                            Thread.yield();
                        } else {
                            if (server.isAlive() && server.isReadyToServe()) {
                                return server;
                            }

                            server = null;
                        }
                        continue;
                    }

                    log.warn("No up servers available from load balancer: " + lb);
                    return null;
                }

                if (count >= 10) {
                    log.warn("No available alive servers after 10 tries from load balancer: " + lb);
                }

                return server;
            }
        }
    }

    
    private int incrementAndGetModulo(int modulo) {
        int current;
        int next;
        do {
            current = this.nextServerCyclicCounter.get();
            //核心就是取模
            next = (current + 1) % modulo;
        } while(!this.nextServerCyclicCounter.compareAndSet(current, next));

        return next;
    }

    public Server choose(Object key) {
        return this.choose(this.getLoadBalancer(), key);
    }

    public void initWithNiwsConfig(IClientConfig clientConfig) {
    }
}

```

##### （3）手写

> 自己试着写一个本地负载均衡器试试

![1660232423375](README.assets/1660232423375.png)

- eureka-server    7001/7002集群启动

- 8001/8002微服务改造

  controller

  ```java
  @RestController
  @Slf4j
  @RequestMapping(value ="/payment")
  public class PaymentController {
  
      @Resource
      private PaymentService paymentService;
  
      @Value("${server.port}")
      private String port;
  
      @Resource
      private DiscoveryClient discoveryClient;
  
      @PostMapping(value = "/create")
      public CommonResult create(@RequestBody Payment payment){
          int result = paymentService.create(payment);
          log.info("*****插入操作返回结果:" + result);
          if(result>0){
              return new CommonResult(200,"插入数据库成功:ServerPort:"+port,result);
          }else{
              return new CommonResult(444,"插入数据库失败",null);
          }
      }
  
      @GetMapping(value = "/get/{id}")
      public CommonResult<Payment> getPaymentById(@PathVariable("id") Long id){
          Payment payment = paymentService.getPaymentById(id);
          log.info("*****查询结果:{}",payment);
          if (payment != null) {
              return new CommonResult(200,"查询成功:ServerPort:"+port,payment);
          }else {
              return new CommonResult(444,"没有对应记录,查询ID: "+id,null);
          }
      }
  
      @GetMapping(value = "/discovery")
      public Object discovery(){
          List<String> services = discoveryClient.getServices();
          for (String service : services) {
              log.info("****service:{}",service);
          }
  
          List<ServiceInstance> instances = discoveryClient.getInstances("CLOUD-PAYMENT-SERVICE");
          for (ServiceInstance instance : instances) {
              log.info(instance.getServiceId()+"\t"+instance.getHost()+"\t"+instance.getPort()+"\t"+instance.getUri());
          }
  
          return this.discoveryClient;
      }
  
      
      //负载均衡测试
      @GetMapping(value = "/lb")
      public String getPaymentLB(){
          return port;
      }
  
  }
  
  ```

- 80订单微服务改造

  (i)ApplicationContextBean去掉注解@LoadBalanced)

  ```java
  @Configuration
  public class ApplicationContextBean
  {
      @Bean
      //@LoadBalanced
      public RestTemplate getRestTemplate()
      {
          return new RestTemplate();
      }
  }
  ```

  

  (ii)LoadBalancer接口

  ```java
  public interface LoadBalancer
  {
      ServiceInstance instances(List<ServiceInstance> serviceInstances);
  }
  ```

  (iii)MyLB

  ```java
  @Component
  public class MyLB implements LoadBalancer
  {
      private AtomicInteger atomicInteger = new AtomicInteger(0);
  
      public final int getAndIncrement()
      {
          int current;
          int next;
          do
          {
              current = this.atomicInteger.get();
              next = current >= 2147483647 ? 0 : current + 1;
          } while(!this.atomicInteger.compareAndSet(current, next));
          System.out.println("*****next: "+next);
          return next;
      }
  
  
      @Override
      public ServiceInstance instances(List<ServiceInstance> serviceInstances)
      {
          int index = getAndIncrement() % serviceInstances.size();
          return serviceInstances.get(index);
      }
  }
  ```

  (iv)OrderController

  ```java
  @RestController
  public class OrderController
  {
      //public static final String PAYMENT_SRV = "http://localhost:8001";
      public static final String PAYMENT_SRV = "http://CLOUD-PAYMENT-SERVICE";
  
      @Resource
      private RestTemplate restTemplate;
      //可以获取注册中心上的服务列表
      @Resource
      private DiscoveryClient discoveryClient;
      @Resource
      private LoadBalancer loadBalancer;
  
      @GetMapping("/consumer/payment/create")
      public CommonResult<Payment> create(Payment payment)
      {
          return restTemplate.postForObject(PAYMENT_SRV+"/payment/create",payment,CommonResult.class);
      }
  
      @GetMapping("/consumer/payment/get/{id}")
      public CommonResult<Payment> getPayment(@PathVariable("id") Long id)
      {
          return restTemplate.getForObject(PAYMENT_SRV+"/payment/get/"+id,CommonResult.class);
      }
  
      @GetMapping("/consumer/payment/getForEntity/{id}")
      public CommonResult<Payment> getPayment2(@PathVariable("id") Long id)
      {
          ResponseEntity<CommonResult> entity = restTemplate.getForEntity(PAYMENT_SRV+"/payment/get/"+id, CommonResult.class);
          if(entity.getStatusCode().is2xxSuccessful()){
              return entity.getBody();
          }else {
              return new CommonResult(444, "操作失败");
          }
      }
   
   
   	//自定义负载均衡算法
      @Resource
      private LoadBalancer loadBalancer;
  
      @GetMapping("/consumer/payment/lb")
      public String getPaymentLB()
      {
          List<ServiceInstance> instances = discoveryClient.getInstances("CLOUD-PAYMENT-SERVICE");
  
          if(instances == null || instances.size()<=0) {
              return null;
          }
          ServiceInstance serviceInstance = loadBalancer.instances(instances);
          URI uri = serviceInstance.getUri();
  
          return restTemplate.getForObject(uri+"/payment/lb",String.class);
      }
  }
  ```

  (v)测试

  http://localhost/consumer/payment/lb





## 微服务调用

### restTemplate

进行简单的服务调用

<img src="https://cloudimgs-1301504220.cos.ap-nanjing.myqcloud.com/image/202203081426858.png" alt="image-20220308142650801" style="zoom: 80%;" />

<img src="https://cloudimgs-1301504220.cos.ap-nanjing.myqcloud.com/image/202203081427195.png" alt="image-20220308142726146" style="zoom:80%;" />

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

<img src="https://cloudimgs-1301504220.cos.ap-nanjing.myqcloud.com/image/202203081626925.png" alt="image-20220308162636836" style="zoom:67%;" />



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

<img src="https://cloudimgs-1301504220.cos.ap-nanjing.myqcloud.com/image/202203081627823.png" alt="image-20220308162730725" style="zoom:67%;" />

## 服务熔断降级与限流

### hystrix

1. 生产者配置降级与熔断【熔断器，降级处理，有明显区别】
2. 消费者配置降级【降级推荐配置到客户】
3. 监控面板需要配合actuator进行使用



相关项目结构

<img src="https://cloudimgs-1301504220.cos.ap-nanjing.myqcloud.com/image/202203081641968.png" alt="image-20220308164134879" style="zoom:67%;" />


## 服务配置中心



<img src="https://cloudimgs-1301504220.cos.ap-nanjing.myqcloud.com/image/202203131700645.png" alt="image-20220313170052544" style="zoom:67%;" />

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

![image-20220313164848797](https://cloudimgs-1301504220.cos.ap-nanjing.myqcloud.com/image/202203131648869.png)



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



## Spring Cloud Sleuth
