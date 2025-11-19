package com.yyblcc.ecommerceplatforms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yyblcc.ecommerceplatforms.domain.DTO.OrderDTO;
import com.yyblcc.ecommerceplatforms.domain.VO.CreateOrderVO;
import com.yyblcc.ecommerceplatforms.domain.po.Order;
import com.yyblcc.ecommerceplatforms.domain.po.PageBean;
import com.yyblcc.ecommerceplatforms.domain.po.Result;
import com.yyblcc.ecommerceplatforms.domain.query.OrderQuery;

public interface OrderService extends IService<Order> {
    Result<PageBean> pageList(OrderQuery orderQuery);

    Result<PageBean> craftsmanPageList(OrderQuery orderQuery);

    Result<PageBean> pageUserOrders(OrderQuery orderQuery);

    Result createOrder(OrderDTO orderDTO);
}
