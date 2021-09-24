package com.spring.ioc.annotation;

import java.lang.annotation.*;

/**
 * 自定义属性的依赖注入
 */
//@Documented
//@Target(ElementType.FIELD)
//@Retention(RetentionPolicy.CLASS)
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface IocResource {
}
