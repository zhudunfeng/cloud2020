package com.adun.feign.impl;

import com.adun.feign.PaymentService;
import com.adun.springcloud.entities.CommonResult;
import com.adun.springcloud.entities.Payment;
import org.springframework.stereotype.Component;

/**
 * @author ADun
 * @date 2022/8/14 17:24
 */
@Component
public class PaymentFallbackService implements PaymentService {
    @Override
    public CommonResult<Payment> paymentSQL(Long id) {
        return new CommonResult<>(444,"服务降级返回,没有该流水信息",new Payment(id, "errorSerial......"));
    }
}
