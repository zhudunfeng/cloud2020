package com.adun.springcloud.controller;

import com.adun.springcloud.entities.CommonResult;
import com.adun.springcloud.entities.Payment;
import com.adun.springcloud.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author Zhu Dunfeng
 * @date 2022/2/26 20:51
 */
@RestController
@Slf4j
@RequestMapping(value ="/payment")
public class PaymentController {

    @Resource
    private PaymentService paymentService;

    @Value("${server.port}")
    private String port;

    @PostMapping(value = "/create")
    public CommonResult create(@RequestBody Payment payment){
        int result = paymentService.create(payment);
        log.info("*****插入操作返回结果:" + result);
        if(result>0){
            return new CommonResult(200,"插入数据库成功:ServerPort:"+port,result);
        }else{
            return new CommonResult(444,"插入数据库失败",null);
        }
    }

    @GetMapping(value = "/get/{id}")
    public CommonResult getPaymentById(@PathVariable("id") Long id){
        Payment payment = paymentService.getPaymentById(id);
        log.info("*****查询结果:{}",payment);
        if (payment != null) {
            return new CommonResult(200,"查询成功:ServerPort:"+port,payment);
        }else {
            return new CommonResult(444,"没有对应记录,查询ID: "+id,null);
        }
    }

}
