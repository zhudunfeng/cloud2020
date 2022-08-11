

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

###### （1）支付服务提供者8001集群环境构建

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

<img src="https://cloudimgs-1301504220.cos.ap-nanjing.myqcloud.com/image/202202272257070.png" alt="image-20220227225729931" style="zoom:80%;" />

### consul相关项目

<img src="https://cloudimgs-1301504220.cos.ap-nanjing.myqcloud.com/image/202202272259322.png" alt="image-20220227225915255" style="zoom:80%;" />

### 三个注册中心的对比

<img src="https://cloudimgs-1301504220.cos.ap-nanjing.myqcloud.com/image/202202272308504.png" alt="image-20220227230802462" style="zoom:67%;" />

<img src="https://cloudimgs-1301504220.cos.ap-nanjing.myqcloud.com/image/202202272309193.png" alt="image-20220227230944151" style="zoom:67%;" />







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
