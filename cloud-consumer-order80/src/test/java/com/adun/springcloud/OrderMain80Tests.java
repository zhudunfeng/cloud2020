package com.adun.springcloud;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author Zhu Dunfeng
 * @date 2022/3/1 0:17
 */
@SpringBootTest
public class OrderMain80Tests {

    @Test
    public void context(){
        //局部变量没有初始值
        int a;
        a=1;
        int b;
        b=2;
        System.out.println(a);
        System.out.println(b);
        System.out.println(Integer.MAX_VALUE);
        System.out.println(Long.MAX_VALUE);

    }

}
