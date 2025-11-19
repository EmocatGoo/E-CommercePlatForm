package com.yyblcc.ecommerceplatforms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yyblcc.ecommerceplatforms.domain.po.Order;
import com.yyblcc.ecommerceplatforms.domain.po.Result;
import com.yyblcc.ecommerceplatforms.domain.query.OrderQuery;
import com.yyblcc.ecommerceplatforms.mapper.OrderMapper;
import com.yyblcc.ecommerceplatforms.service.OrderService;
import org.springframework.stereotype.Service;

@Service
public class OrderServiceImplement extends ServiceImpl<OrderMapper, Order> implements OrderService {
    @Override
    public Result<Order> pageList(OrderQuery orderQuery) {
        return null;
    }
}
