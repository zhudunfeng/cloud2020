package com.adun.myrule;

import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.RandomRule;
import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Zhu Dunfeng
 * @date 2022/2/28 15:21
 */
@Configuration
//@RibbonClient(name = "CLOUD-PAYMENT-SERVICE",configuration= MySelfRule.class)放在此处不起作用
public class MySelfRule {

    @Bean
    public IRule myRule(){
        return new RandomRule();
    }

}
