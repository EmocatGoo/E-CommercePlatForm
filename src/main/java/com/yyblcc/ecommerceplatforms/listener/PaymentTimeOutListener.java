package com.yyblcc.ecommerceplatforms.listener;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yyblcc.ecommerceplatforms.domain.Enum.OrderStatusEnum;
import com.yyblcc.ecommerceplatforms.domain.Enum.PayStatusEnum;
import com.yyblcc.ecommerceplatforms.domain.po.Order;
import com.yyblcc.ecommerceplatforms.domain.po.Payment;
import com.yyblcc.ecommerceplatforms.mapper.OrderMapper;
import com.yyblcc.ecommerceplatforms.mapper.PaymentMapper;
import com.yyblcc.ecommerceplatforms.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@RocketMQMessageListener(
        topic = "payment_time_out",
        consumerGroup = "payment_time_out_group"
)
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentTimeOutListener implements RocketMQListener<String> {
    private final PaymentService paymentService;
    private final PaymentMapper paymentMapper;
    private final OrderMapper orderMapper;

    @Override
    public void onMessage(String mergePaySn) {
        log.info("支付超时，启动延时取消操作");
        Payment payment = paymentService.query().eq("merge_pay_sn", mergePaySn).one();
        payment.setPayStatus(PayStatusEnum.CANCEL.getCode());
        payment.setExpireTime(LocalDateTime.now());
        paymentMapper.updateById(payment);

        List<Order> orders = orderMapper.selectList(new LambdaQueryWrapper<Order>().eq(Order::getPaySn, mergePaySn));

        if (!orders.isEmpty()) {
            for (Order order : orders) {
                // 使用主键更新，这样可以利用主键索引
                orderMapper.update(null,new LambdaUpdateWrapper<Order>()
                        .eq(Order::getOrderSn, order.getOrderSn()) // 使用orderSn(有索引)而不是paySn
                        .set(Order::getOrderStatus, OrderStatusEnum.CANCEL.getCode())
                        .set(Order::getPayStatus, PayStatusEnum.CANCEL.getCode())
                        .set(Order::getCancelReason, "支付超时取消订单")
                        .set(Order::getCancelTime, LocalDateTime.now())
                        .last("FOR UPDATE"));
            }
        }
    }
}
