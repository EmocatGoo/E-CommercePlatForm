package com.yyblcc.ecommerceplatforms.listener;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yyblcc.ecommerceplatforms.domain.message.CartDeleteMessage;
import com.yyblcc.ecommerceplatforms.domain.po.Cart;
import com.yyblcc.ecommerceplatforms.domain.po.CartItem;
import com.yyblcc.ecommerceplatforms.mapper.CartItemMapper;
import com.yyblcc.ecommerceplatforms.mapper.CartMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@RocketMQMessageListener(
        topic = "cart-delete-topic",
        consumerGroup = "cart-delete-group",
        consumeMode = ConsumeMode.CONCURRENTLY
)
@Service
@RequiredArgsConstructor
@Slf4j
public class CartDeleteListener implements RocketMQListener<CartDeleteMessage> {

    private final CartItemMapper cartItemMapper;
    private final CartMapper cartMapper;
    private final StringRedisTemplate stringRedisTemplate;


    @Override
    public void onMessage(CartDeleteMessage cartDeleteMessage) {
        Long userId = cartDeleteMessage.getUserId();
        List<Long> productIds = cartDeleteMessage.getProductIds();

        if (productIds == null || productIds.isEmpty()) {
            log.warn("删除购物车消息为空，userId={}", userId);
            return;
        }
        try{
            List<CartItem> cartItems = cartItemMapper.selectList(new LambdaQueryWrapper<CartItem>()
                    .eq(CartItem::getUserId,userId)
                    .in(CartItem::getProductId,productIds));

            if (cartItems == null || cartItems.isEmpty()) {
                log.warn("无可删除商品!");
                return;
            }
            int totalQuantity = 0;
            int checkedQuantity = 0;
            for (CartItem cartItem : cartItems) {
                totalQuantity += cartItem.getQuantity();
                if (cartItem.getChecked() != null && cartItem.getChecked().equals(1)){
                    checkedQuantity += cartItem.getQuantity();
                }
            }

            cartItemMapper.delete(new LambdaUpdateWrapper<CartItem>()
                    .eq(CartItem::getUserId, userId)
                    .in(CartItem::getProductId, productIds));
            productIds.forEach(productId -> {
                stringRedisTemplate.opsForHash().delete("cart:items:" + userId,"product:"+productId);
            });

            if (totalQuantity > 0) {
                cartMapper.update(new LambdaUpdateWrapper<Cart>()
                        .eq(Cart::getUserId, userId)
                        .setSql("item_count = GREATEST(item_count - " + totalQuantity + ", 0)")
                        .setSql("checked_count = GREATEST(checked_count - " + checkedQuantity + ", 0)"));
                stringRedisTemplate.opsForHash().increment("cart:user:" + userId, "item_count", -totalQuantity);
                stringRedisTemplate.opsForHash().increment("cart:user:" + userId, "checked_count", -checkedQuantity);
            }
        }catch (Exception e){
            log.error("购物车删除落库失败，userId={}, productIds={}", userId, productIds, e);
            throw e;
        }
    }
}
