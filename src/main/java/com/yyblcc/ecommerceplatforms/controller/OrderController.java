package com.yyblcc.ecommerceplatforms.controller;

import com.yyblcc.ecommerceplatforms.domain.DTO.OrderDTO;
import com.yyblcc.ecommerceplatforms.domain.DTO.OrderStatsuDTO;
import com.yyblcc.ecommerceplatforms.domain.DTO.UserSignUpRefundDTO;
import com.yyblcc.ecommerceplatforms.domain.po.PageBean;
import com.yyblcc.ecommerceplatforms.domain.po.Result;
import com.yyblcc.ecommerceplatforms.domain.query.OrderQuery;
import com.yyblcc.ecommerceplatforms.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
    public Result<PageBean> pageList(@Validated OrderQuery orderQuery){
        log.info("订单查询:{}",orderQuery);
        return orderService.pageList(orderQuery);
    }

    /**
     * 匠人分页条件查看订单
     * @param orderQuery
     * @return
     */
    @GetMapping("/craftsman-page")
    public Result<PageBean> craftsmanPageList(@Validated OrderQuery orderQuery){
        log.info("匠人查询订单:{}",orderQuery);
        return orderService.craftsmanPageList(orderQuery);
    }

    /**
     * 用户查看条件分页订单
     * @param orderQuery
     * @return
     */
    @GetMapping("/page")
    public Result<PageBean> page(@Validated OrderQuery orderQuery){
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

}
