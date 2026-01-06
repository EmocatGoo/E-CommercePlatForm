package com.yyblcc.ecommerceplatforms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yyblcc.ecommerceplatforms.domain.po.OrderItem;
import com.yyblcc.ecommerceplatforms.mapper.OrderItemMapper;
import com.yyblcc.ecommerceplatforms.service.OrderItemService;
import org.springframework.stereotype.Service;

@Service
public class OrderItemServiceImplement extends ServiceImpl<OrderItemMapper, OrderItem> implements OrderItemService {
}
