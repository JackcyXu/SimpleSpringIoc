package com.spring.ioc.service.impl;

import com.spring.ioc.annotation.IocResource;
import com.spring.ioc.annotation.IocService;
import com.spring.ioc.service.IOrderService;
import com.spring.ioc.service.IUserService;

@IocService(name = "useraze")
public class UserService implements IUserService {

    /*比较脆弱啊 这块的属性名称一定要用实现类来命名 且 按照第一个字母要小写的原则 否则很报错的*/
    @IocResource
    private IOrderService orderService;

    @Override
    public String findOrder(String username) {
        return orderService.findOrder(username);
    }
}
