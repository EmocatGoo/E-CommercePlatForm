package com.yyblcc.ecommerceplatforms.listener;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yyblcc.ecommerceplatforms.domain.message.AddCartMessage;
import com.yyblcc.ecommerceplatforms.domain.po.Cart;
import com.yyblcc.ecommerceplatforms.domain.po.CartItem;
import com.yyblcc.ecommerceplatforms.mapper.CartItemMapper;
import com.yyblcc.ecommerceplatforms.mapper.CartMapper;
import lombok.RequiredArgsConstructor;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;
@RocketMQMessageListener(
        topic = "cart-add-topic",
        consumerGroup = "cart-add-consumer-group"
)
@Service
@RequiredArgsConstructor
public class CartItemListener implements RocketMQListener<AddCartMessage> {

    private final CartMapper cartMapper;
    private final CartItemMapper cartItemMapper;

    @Override
    public void onMessage(AddCartMessage msg) {
        Cart cart = cartMapper.selectOne(Wrappers.<Cart>lambdaQuery().eq(Cart::getUserId, msg.getUserId()));
        if (cart == null) {
            cart = Cart.builder().userId(msg.getUserId()).itemCount(0).checkedCount(0).build();
            cartMapper.insert(cart);
        }

        CartItem exist = cartItemMapper.selectOne(Wrappers.<CartItem>lambdaQuery()
                .eq(CartItem::getUserId, msg.getUserId())
                .eq(CartItem::getProductId, msg.getProductId()));

        if (exist != null) {
            cartItemMapper.update(new LambdaUpdateWrapper<CartItem>().eq(CartItem::getId, exist.getId())
                    .setSql("quantity = quantity + " + msg.getQuantity())
                    .set(msg.isChecked(), CartItem::getIsChecked, 1));
        } else {
            CartItem newItem = CartItem.builder()
                    .cartId(cart.getId())
                    .userId(msg.getUserId())
                    .craftsmanId(msg.getCraftsmanId())
                    .productId(msg.getProductId())
                    .productName(msg.getProductName())
                    .productImage(msg.getProductImage())
                    .price(msg.getPriceAtAdd())
                    .quantity(msg.getQuantity())
                    .isChecked(msg.isChecked() ? 1 : 0)
                    .build();
            cartItemMapper.insert(newItem);
        }

        cartMapper.update(new LambdaUpdateWrapper<Cart>()
                .eq(Cart::getUserId, msg.getUserId())
                .setSql("item_count = item_count + " + msg.getQuantity())
                .set(msg.isChecked(), Cart::getCheckedCount,
                        Wrappers.<Cart>lambdaUpdate().setSql("checked_count = checked_count + " + msg.getQuantity())));
    }
}
