package com.adun.springcloud.service;

import com.adun.springcloud.entities.Payment;

/**
 * @author Zhu Dunfeng
 * @date 2022/2/26
 */
public interface PaymentService {

    public int create(Payment payment);

    public Payment getPaymentById(Long id);

}
