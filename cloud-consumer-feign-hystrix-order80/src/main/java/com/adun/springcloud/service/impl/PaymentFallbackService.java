package com.adun.springcloud.service.impl;

import com.adun.springcloud.service.PaymentHystrixService;
import org.springframework.stereotype.Component;

/**
 * @author Zhu Dunfeng
 * @date 2022/3/7 21:40
 */
@Component
public class PaymentFallbackService implements PaymentHystrixService {
    @Override
    public String paymentInfo_OK(Integer id) {
        return "服务调用失败，提示来自：cloud-consumer-feign-order80";
    }

    @Override
    public String paymentInfo_TimeOut(Integer id) {
        return "服务调用失败，提示来自：cloud-consumer-feign-order80";
    }
}
