package com.adun.springcloud.service.impl;

import com.adun.springcloud.dao.PaymentDao;
import com.adun.springcloud.entities.Payment;
import com.adun.springcloud.service.PaymentService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author Zhu Dunfeng
 * @date 2022/2/26 20:52
 */
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
