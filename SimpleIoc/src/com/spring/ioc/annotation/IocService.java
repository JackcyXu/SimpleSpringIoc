package com.spring.ioc.annotation;

import java.lang.annotation.*;

/**
 * 自定义服务的依赖注入
 */
//@Documented
//@Retention(RetentionPolicy.RUNTIME)
//@Target(ElementType.TYPE)
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface IocService {
    String name() default  "";
}
