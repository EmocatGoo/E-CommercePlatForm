package com.yyblcc.ecommerceplatforms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yyblcc.ecommerceplatforms.domain.DTO.OrderDTO;
import com.yyblcc.ecommerceplatforms.domain.DTO.OrderReviewDTO;
import com.yyblcc.ecommerceplatforms.domain.DTO.OrderStatsuDTO;
import com.yyblcc.ecommerceplatforms.domain.DTO.UserSignUpRefundDTO;
import com.yyblcc.ecommerceplatforms.domain.po.Order;
import com.yyblcc.ecommerceplatforms.domain.po.PageBean;
import com.yyblcc.ecommerceplatforms.domain.po.Result;
import com.yyblcc.ecommerceplatforms.domain.query.OrderQuery;

public interface OrderService extends IService<Order> {
    Result<PageBean> pageList(OrderQuery orderQuery);

    Result<PageBean> craftsmanPageList(OrderQuery orderQuery);

    Result<PageBean> pageUserOrders(OrderQuery orderQuery);

    Result createOrder(OrderDTO orderDTO);

    Result userUpdateOrderStatus(OrderStatsuDTO orderStatsuDTO);

    Result updateOrderStatus(OrderStatsuDTO orderStatsuDTO);

    Result signUpRefund(UserSignUpRefundDTO userSignUpRefundDTO);

    Result reviewOrder(OrderReviewDTO orderReviewDTO);
}
