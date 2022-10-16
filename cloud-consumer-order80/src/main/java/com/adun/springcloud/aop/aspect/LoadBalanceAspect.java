package com.adun.springcloud.aop.aspect;

import com.adun.springcloud.aop.annotation.LoadBalanced;
import com.adun.springcloud.lb.LoadBalancer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.List;

/**
 * @author Zhu Dunfeng
 * @date 2022/3/1 21:18
 */
@Aspect
@Component
public class LoadBalanceAspect {


    @Autowired
    private DiscoveryClient discoveryClient;

    @Autowired
    private LoadBalancer loadBalancer;


    //定义传出公共空间
    public static final ThreadLocal<String> THREAD_LOCAL=new ThreadLocal<>();


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
            if(!path.trim().startsWith("/")){
                path="/"+path;
            }
            THREAD_LOCAL.set(uri+path);
            Object result = joinPoint.proceed();
            //防止内存泄露，必须删除，不然会造成线程不安全，因为在这里我们使用的是tomcat线程池
            //请求结束不代表线程结束
            THREAD_LOCAL.remove();
            return result;
        }

        return null;
    }

    public static String getUrl(){
        return THREAD_LOCAL.get();
    }

}