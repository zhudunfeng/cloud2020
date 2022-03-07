package com.adun.springcloud.aop.aspect;

import com.adun.springcloud.aop.annotation.LoadBalanced;
import com.adun.springcloud.lb.LoadBalancer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.List;

/**
 * @author Zhu Dunfeng
 * @date 2022/3/1 21:18
 */
@Aspect
@Component
public class LoadBalanceHandler {


    @Autowired
    private DiscoveryClient discoveryClient;

    @Autowired
    private LoadBalancer loadBalancer;

    @Autowired
    private RestTemplate restTemplate;

    /**
     *     切入点表达式
     */
    @Pointcut("@annotation(com.adun.springcloud.aop.annotation.LoadBalanced)")
    public void pointCut(){}


    @Around("pointCut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        //判断注解是否存在
        boolean annotationPresent = method.isAnnotationPresent(LoadBalanced.class);
        if (annotationPresent) {
            LoadBalanced annotation = method.getAnnotation(LoadBalanced.class);
            String path = annotation.path();
            String instance = annotation.instance();
            List<ServiceInstance> instances = discoveryClient.getInstances(instance);
            if(instances == null || instances.size() <= 0) {
                return null;
            }
            ServiceInstance serviceInstance = loadBalancer.instances(instances);
            URI uri = serviceInstance.getUri();
            if(!path.startsWith("/")){
                path="/"+path;
            }
            joinPoint.proceed();
            return restTemplate.getForObject(uri+path,String.class);
        }

        return null;
    }

}
