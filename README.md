# cloud2020
SpringCloud2020年的技术变更

## 初期项目结构
> 此时，项目未引入SpringCloud的服务注册与发现组件，服务之间的调用使用restTemplate进行相互调用



![image](https://user-images.githubusercontent.com/48040850/155866502-5e2966be-eae3-40c0-bbf6-b9aa3939d798.png)



![image-20220227111214723](https://gitee.com/zhudunfeng/cloudimage/raw/master/image/202202271112780.png)

## 引入Eureka服务发现与注册

<img src="https://gitee.com/zhudunfeng/cloudimage/raw/master/image/202202271342914.png" alt="image-20220227134225873" style="zoom:80%;" />

eureka server集群

<img src="https://gitee.com/zhudunfeng/cloudimage/raw/master/image/202202271725855.png" alt="image-20220227172534797" style="zoom:67%;" />

![image-20220227141957114](https://gitee.com/zhudunfeng/cloudimage/raw/master/image/202202271419150.png)

## zookeeper相关项目

<img src="https://gitee.com/zhudunfeng/cloudimage/raw/master/image/202202272257070.png" alt="image-20220227225729931" style="zoom:80%;" />

## consul相关项目

<img src="https://gitee.com/zhudunfeng/cloudimage/raw/master/image/202202272259322.png" alt="image-20220227225915255" style="zoom:80%;" />
