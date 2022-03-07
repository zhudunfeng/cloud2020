package com.adun.springcloud.lb.impl;

import com.adun.springcloud.lb.LoadBalancer;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Zhu Dunfeng
 * @date 2022/3/1 19:28
 */
@Component
public class MyLB implements LoadBalancer {

    private AtomicInteger nextServerCyclicCounter;

    public MyLB() {
        this.nextServerCyclicCounter = new AtomicInteger(0);
    }


    private int getAndIncrement() {
        int current;
        int next;
        do {
            current = this.nextServerCyclicCounter.get();
            next = current >= 2147483647 ? 0 : current + 1;
        } while (!nextServerCyclicCounter.compareAndSet(current, next));
        System.out.println("*****第几次访问，次数next: "+next);
        return next;
    }

    //负载均衡算法：rest接口第几次请求数 % 服务器集群总数量 = 实际调用服务器位置下标  ，每次服务重启动后rest接口计数从1开始。
    @Override
    public ServiceInstance instances(List<ServiceInstance> serviceInstances) {
        int index = getAndIncrement() % serviceInstances.size();
        return serviceInstances.get(index);
    }

}
