package com.spring.ioc.service.impl;

import com.spring.ioc.annotation.IocService;
import com.spring.ioc.service.IOrderService;

@IocService
public class OrderService implements IOrderService {
    @Override
    public String findOrder(String username) {
        return "用户"+username+"的订单编号是:1001";
    }
}
