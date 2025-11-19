package com.yyblcc.ecommerceplatforms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yyblcc.ecommerceplatforms.domain.po.Order;
import com.yyblcc.ecommerceplatforms.domain.po.Result;
import com.yyblcc.ecommerceplatforms.domain.query.OrderQuery;

public interface OrderService extends IService<Order> {
    Result<Order> pageList(OrderQuery orderQuery);
}
