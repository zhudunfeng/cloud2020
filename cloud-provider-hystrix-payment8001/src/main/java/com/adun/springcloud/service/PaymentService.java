package com.adun.springcloud.service;

import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author Zhu Dunfeng
 * @date 2022/3/7 10:52
 */
public interface PaymentService {


    /**
     * 正常访问，一切OK
     *
     * @param id
     * @return
     */
    public String paymentInfo_OK(Integer id);

    /**
     * 超时访问，演示降级
     *
     * @param id
     * @return
     */
    public String paymentInfo_TimeOut(Integer id);


    /**
     * 测试断路器
     * @param id
     * @return
     */
    public String paymentCircuitBreaker(@PathVariable("id") Integer id);
}
