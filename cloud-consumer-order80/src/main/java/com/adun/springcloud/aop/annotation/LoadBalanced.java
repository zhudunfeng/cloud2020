package com.adun.springcloud.aop.annotation;

/**
 * @author Zhu Dunfeng
 * @date 2022/3/1
 */

import java.lang.annotation.*;

@Documented
@Target(value = {ElementType.METHOD})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface LoadBalanced {

    String path() default "";

    String instance() default "";

}
