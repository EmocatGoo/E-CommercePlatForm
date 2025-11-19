package com.yyblcc.ecommerceplatforms.controller;

import com.yyblcc.ecommerceplatforms.domain.DTO.OrderDTO;
import com.yyblcc.ecommerceplatforms.domain.VO.CreateOrderVO;
import com.yyblcc.ecommerceplatforms.domain.po.Order;
import com.yyblcc.ecommerceplatforms.domain.po.PageBean;
import com.yyblcc.ecommerceplatforms.domain.po.Result;
import com.yyblcc.ecommerceplatforms.domain.query.OrderQuery;
import com.yyblcc.ecommerceplatforms.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/admin-page")
    public Result<PageBean> pageList(@Validated OrderQuery orderQuery){
        log.info("订单查询:{}",orderQuery);
        return orderService.pageList(orderQuery);
    }

    @GetMapping("/craftsman-page")
    public Result<PageBean> craftsmanPageList(@Validated OrderQuery orderQuery){
        log.info("匠人查询订单:{}",orderQuery);
        return orderService.craftsmanPageList(orderQuery);
    }

    @GetMapping("/page")
    public Result<PageBean> page(@Validated OrderQuery orderQuery){
        log.info("用户查询订单：{}",orderQuery);
        return orderService.pageUserOrders(orderQuery);
    }

    @PostMapping
    public Result createOrder(@Validated OrderDTO orderDTO){
        log.info("初始化订单:{}",orderDTO);
        return orderService.createOrder(orderDTO);
    }
}
