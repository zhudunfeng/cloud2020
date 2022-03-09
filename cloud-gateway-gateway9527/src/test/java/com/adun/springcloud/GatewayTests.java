package com.adun.springcloud;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.ZonedDateTime;

/**
 * @author Zhu Dunfeng
 * @date 2022/3/9 10:18
 */
@SpringBootTest
public class GatewayTests {

    @Test
    public void testTime(){
        ZonedDateTime now = ZonedDateTime.now();
        System.out.println(now);
    }
}
