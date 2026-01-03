package com.yyblcc.ecommerceplatforms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yyblcc.ecommerceplatforms.domain.DTO.*;
import com.yyblcc.ecommerceplatforms.domain.VO.OrderAdminVO;
import com.yyblcc.ecommerceplatforms.domain.VO.OrderCraftsmanVO;
import com.yyblcc.ecommerceplatforms.domain.VO.OrderUserVO;
import com.yyblcc.ecommerceplatforms.domain.VO.RefundFVO;
import com.yyblcc.ecommerceplatforms.domain.po.Order;
import com.yyblcc.ecommerceplatforms.domain.po.PageBean;
import com.yyblcc.ecommerceplatforms.domain.po.Result;
import com.yyblcc.ecommerceplatforms.domain.query.OrderQuery;
import com.yyblcc.ecommerceplatforms.domain.query.RefundQuery;

import java.util.List;

public interface OrderService extends IService<Order> {
    Result<PageBean<OrderAdminVO>> pageList(OrderQuery orderQuery);

    Result<PageBean<OrderCraftsmanVO>> craftsmanPageList(OrderQuery orderQuery);

    Result<PageBean<OrderUserVO>> pageUserOrders(OrderQuery orderQuery);

    Result createOrder(OrderDTO orderDTO);

    Result userUpdateOrderStatus(OrderStatsuDTO orderStatsuDTO);

    Result updateOrderStatus(OrderStatsuDTO orderStatsuDTO);

    Result signUpRefund(UserSignUpRefundDTO userSignUpRefundDTO);

    Result reviewOrder(OrderReviewDTO orderReviewDTO);

    Result<PageBean<RefundFVO>> refundList(RefundQuery refundQuery);

    Result<List<OrderUserVO>> myOrder();
    Result countCraftsmanOrders();

    Result getOrderCounts();

    Result getSalesAmount();

    Result getAllOrders();

    Result comment(OrderCommentDTO orderDTO);
}
