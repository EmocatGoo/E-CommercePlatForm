package com.yyblcc.ecommerceplatforms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yyblcc.ecommerceplatforms.domain.po.OrderComment;
import com.yyblcc.ecommerceplatforms.domain.po.OrderItem;
import com.yyblcc.ecommerceplatforms.mapper.OrderCommentMapper;
import com.yyblcc.ecommerceplatforms.mapper.OrderItemMapper;
import com.yyblcc.ecommerceplatforms.service.OrderCommentService;
import com.yyblcc.ecommerceplatforms.service.OrderItemService;
import org.springframework.stereotype.Service;

@Service
public class OrderCommentServiceImplement extends ServiceImpl<OrderCommentMapper, OrderComment> implements OrderCommentService {
}
