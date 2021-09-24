package com.spring.ioc.test;

import com.spring.ioc.context.SpringContext;
import com.spring.ioc.service.IUserService;

public class SpringIocTest {
    public static void main(String[] args) throws Exception {
        String path = "com.spring.ioc.service.impl";
        SpringContext context = new SpringContext(path);


         IUserService userService = (IUserService) context.getBean("useraze");
        System.out.println(userService.findOrder("xmz"));
    }
}
