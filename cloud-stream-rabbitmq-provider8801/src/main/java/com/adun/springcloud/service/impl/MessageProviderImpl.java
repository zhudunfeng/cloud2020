package com.adun.springcloud.service.impl;

import com.adun.springcloud.service.IMessageProvider;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.messaging.MessageChannel;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import java.util.UUID;

/**
 * @author Zhu Dunfeng
 * @date 2022/3/13 17:52
 */
// 可以理解为是一个消息的发送管道的定义
@EnableBinding(Source.class)
public class MessageProviderImpl implements IMessageProvider {

    // 消息的发送管道
    @Resource
    private MessageChannel output;

    @Override
    public String send() {
        String serial = UUID.randomUUID().toString();
        // 创建并发送消息
        output.send(MessageBuilder.withPayload(serial).build());
        System.out.println("***serial: "+serial);
        return serial;
    }
}
