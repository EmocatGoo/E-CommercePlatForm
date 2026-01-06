package com.yyblcc.ecommerceplatforms.listener;

import com.alibaba.fastjson.JSON;
import com.yyblcc.ecommerceplatforms.domain.message.*;
import com.yyblcc.ecommerceplatforms.domain.po.Cart;
import com.yyblcc.ecommerceplatforms.domain.po.CartItem;
import com.yyblcc.ecommerceplatforms.mapper.CartItemMapper;
import com.yyblcc.ecommerceplatforms.mapper.CartMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
@RocketMQMessageListener(topic = "cart-sync-topic", consumerGroup = "cart-sync-consumer-group")
public class CartSyncListener implements RocketMQListener<UpdateCartQuantityMessage> {

    private final CartMapper cartMapper;
    private final CartItemMapper cartItemMapper;

    @Override
    public void onMessage(UpdateCartQuantityMessage message) {
        Long userId = message.getUserId();
        Long productId = message.getProductId();
        Long cartId = message.getCartId();
        Integer quantity = message.getQuantity();
        Integer checked = message.getChecked();
        CartItem item = cartItemMapper.selectOne(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .eq(CartItem::getProductId, productId)
                .eq(CartItem::getCartId, cartId));
        if (item != null) {
            item.setQuantity(quantity);
            item.setChecked(checked);
            cartItemMapper.updateById(item);
        }
    }
}
