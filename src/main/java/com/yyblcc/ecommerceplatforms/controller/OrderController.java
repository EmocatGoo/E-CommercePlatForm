package com.yyblcc.ecommerceplatforms.controller;

import com.yyblcc.ecommerceplatforms.domain.DTO.*;
import com.yyblcc.ecommerceplatforms.domain.VO.OrderAdminVO;
import com.yyblcc.ecommerceplatforms.domain.VO.OrderCraftsmanVO;
import com.yyblcc.ecommerceplatforms.domain.VO.OrderUserVO;
import com.yyblcc.ecommerceplatforms.domain.VO.RefundFVO;
import com.yyblcc.ecommerceplatforms.domain.po.PageBean;
import com.yyblcc.ecommerceplatforms.domain.po.Result;
import com.yyblcc.ecommerceplatforms.domain.query.OrderQuery;
import com.yyblcc.ecommerceplatforms.domain.query.RefundQuery;
import com.yyblcc.ecommerceplatforms.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    /**
     * 管理员分页条件查看订单
     * @param orderQuery
     * @return
     */
    @GetMapping("/admin-page")
    public Result<PageBean<OrderAdminVO>> pageList(@Validated OrderQuery orderQuery){
        log.info("订单查询:{}",orderQuery);
        return orderService.pageList(orderQuery);
    }

    /**
     * 匠人分页条件查看订单
     * @param orderQuery
     * @return
     */
    @GetMapping("/craftsman-page")
    public Result<PageBean<OrderCraftsmanVO>> craftsmanPageList(@Validated OrderQuery orderQuery){
        log.info("匠人查询订单:{}",orderQuery);
        return orderService.craftsmanPageList(orderQuery);
    }
    @GetMapping("/craftsman-orders")
    public Result<PageBean<OrderCraftsmanVO>> getOrdersCount(){
        log.info("查询匠人所有订单数量");
        return orderService.countCraftsmanOrders();
    }

    /**
     * 用户查看条件分页订单
     * @param orderQuery
     * @return
     */
    @GetMapping("/page")
    public Result<PageBean<OrderUserVO>> page(@Validated OrderQuery orderQuery){
        log.info("用户查询订单：{}",orderQuery);
        return orderService.pageUserOrders(orderQuery);
    }

    @PostMapping
    public Result createOrder(@RequestBody @Validated OrderDTO orderDTO){
        log.info("初始化订单:{}",orderDTO);
        return orderService.createOrder(orderDTO);
    }

    /**
     * 用户确认收货/评价订单，修改对应状态
     * @param orderStatsuDTO
     * @return
     */
    @PutMapping("/status-user")
    public Result userUpdateOrderStatus(@RequestBody @Validated OrderStatsuDTO orderStatsuDTO){
        //通过paySn，上下文id查询到对应的Order or Orders,然后设置orderStatus(收货)
        log.info("用户确认收货：{}",orderStatsuDTO);
        return orderService.userUpdateOrderStatus(orderStatsuDTO);
    }

    /**
     * 匠人发货，修改对应订单状态
     * @param orderStatsuDTO
     * @return
     */
    @PutMapping("/status")
    public Result updateOrderStatus(@RequestBody @Validated OrderStatsuDTO orderStatsuDTO){
        //通过paySn，上下文id查询到对应的Order or Orders,然后设置orderStatus（发货，但是这里有一个预备动作，需要添加物流号和物流公司名称）
        log.info("匠人确认发货：{}",orderStatsuDTO);
        return orderService.updateOrderStatus(orderStatsuDTO);
    }

    @PostMapping("/refund")
    public Result signUpRefund(@RequestBody UserSignUpRefundDTO userSignUpRefundDTO){
        log.info("用户发起退款申请:{}",userSignUpRefundDTO);
        return orderService.signUpRefund(userSignUpRefundDTO);
    }

    @PostMapping("/reviewOrder")
    public Result reviewOrder(@RequestBody @Validated OrderReviewDTO orderReviewDTO){
        log.info("匠人审核用户退款:{}",orderReviewDTO);
        return orderService.reviewOrder(orderReviewDTO);
    }

    @GetMapping("/refund-page")
    public Result<PageBean<RefundFVO>> refundPageList(@Validated RefundQuery refundQuery){
        log.info("退款订单:{}",refundQuery);
        return orderService.refundList(refundQuery);
    }

    @GetMapping("/myOrders")
    public Result<List<OrderUserVO>> myOrders(){
        log.info("用户查询订单");
        return orderService.myOrder();
    }

    @GetMapping("/getOrderCounts")
    public Result getOrderCounts(){
        log.info("查询所有订单数量");
        return orderService.getOrderCounts();
    }

    @GetMapping("/getSalesAmount")
    public Result getSalesAmount(){
        log.info("查询所有订单销售额");
        return orderService.getSalesAmount();
    }

    @GetMapping("/getAllOrders")
    public Result getAllOrders(){
        log.info("查询所有订单");
        return orderService.getAllOrders();
    }

    @PostMapping("/comment")
    public Result comment(@RequestBody @Validated OrderCommentDTO dto){
        log.info("用户评价订单:{}",dto);
        return orderService.comment(dto);
    }

    @GetMapping("/getAmount")
    public Result getCraftsmanAmount(@RequestParam Long craftsmanId){
        log.info("查询匠人所有订单销售额");
        return orderService.getCraftsmanSalesAmount(craftsmanId);
    }

}
