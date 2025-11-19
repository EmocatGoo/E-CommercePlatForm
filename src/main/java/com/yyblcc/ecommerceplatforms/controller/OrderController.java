package com.yyblcc.ecommerceplatforms.controller;

import com.yyblcc.ecommerceplatforms.domain.po.Order;
import com.yyblcc.ecommerceplatforms.domain.po.Result;
import com.yyblcc.ecommerceplatforms.domain.query.OrderQuery;
import com.yyblcc.ecommerceplatforms.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/page")
    public Result<Order> pageList(@Validated OrderQuery orderQuery){
        log.info("订单查询:{}",orderQuery);
        return orderService.pageList(orderQuery);
    }

}
