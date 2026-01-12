package com.yyblcc.ecommerceplatforms.listener;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yyblcc.ecommerceplatforms.domain.Enum.OrderStatusEnum;
import com.yyblcc.ecommerceplatforms.domain.po.OrderItem;
import com.yyblcc.ecommerceplatforms.domain.po.Refund;
import com.yyblcc.ecommerceplatforms.mapper.OrderItemMapper;
import com.yyblcc.ecommerceplatforms.mapper.OrderMapper;
import com.yyblcc.ecommerceplatforms.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Collections;

@RocketMQMessageListener(
        topic = "order-refund-topic",
        consumerGroup = "order-refund-consumer-group"
)
@Service
@RequiredArgsConstructor
public class RefundListener implements RocketMQListener<Refund> {

    private final PaymentService paymentService;
    private final OrderItemMapper orderItemMapper;

    @Override
    public void onMessage(Refund refund) {
        if (refund == null) {
            return;
        }
        orderItemMapper.update(new LambdaUpdateWrapper<OrderItem>()
                .eq(OrderItem::getOrderSn, refund.getOrderSn())
                .eq(OrderItem::getCraftsmanId, refund.getCraftsmanId())
                .set(OrderItem::getRefundStatus, OrderStatusEnum.SUCCESS.getCode())
                .set(OrderItem::getRefundAmount,refund.getRefundAmount())
                .last("FOR UPDATE"));
//        paymentService.refund(refund.getOrderSn());
    }
}
