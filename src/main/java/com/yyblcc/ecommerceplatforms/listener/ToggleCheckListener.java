package com.yyblcc.ecommerceplatforms.listener;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yyblcc.ecommerceplatforms.constant.StatusConstant;
import com.yyblcc.ecommerceplatforms.domain.message.CheckMessage;
import com.yyblcc.ecommerceplatforms.domain.po.Cart;
import com.yyblcc.ecommerceplatforms.domain.po.CartItem;
import com.yyblcc.ecommerceplatforms.mapper.CartItemMapper;
import com.yyblcc.ecommerceplatforms.mapper.CartMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

@RocketMQMessageListener(
        topic = "cart-check-topic",
        consumerGroup = "cart-check-consumer-group"
)
@Service
@RequiredArgsConstructor
@Slf4j
public class ToggleCheckListener implements RocketMQListener<CheckMessage> {

    private final CartItemMapper cartItemMapper;
    private final CartMapper cartMapper;

    //1.根据userId,productId查询购物车项表（cartItem表）
    //2. 没有，直接返回；
    //3.有购物车项，根据传入的是否勾选状态判断
    //4.如果checked为true（勾选），再查看查询到的购物车项是否有checked标识
    //5.未勾选，将新加入的数量赋值给delta变量，更新cartItem的checked状态
    //6.已勾选，将需要删减的数量赋值给delta变量，随后再更新checked状态
    //7.根据数量是否变化，更新购物车的checked_count字段
    @Override
    public void onMessage(CheckMessage checkMessage) {
        Long userId = checkMessage.getUserId();
        Long productId = checkMessage.getProductId();
        boolean checked = checkMessage.getChecked();
        int quantity = checkMessage.getQuantity();
        int delta;
        try{
            CartItem item = cartItemMapper.selectOne(new LambdaQueryWrapper<CartItem>()
                    .eq(CartItem::getUserId, checkMessage.getUserId())
                    .eq(CartItem::getProductId, checkMessage.getProductId()));
            if (item == null) {
                log.warn("用户 {} 的购物车中未找到商品 {}，可能已被删除，忽略本次勾选操作", userId, productId);
                return;
            }

            if (checked) {
                if (item.getChecked() == null || item.getChecked().equals(StatusConstant.DISABLE)) {
                    delta = quantity;
                }else {
                    delta = 0;
                }
            }else{
                if (item.getChecked() != null && item.getChecked().equals(StatusConstant.ENABLE)) {
                    delta = -quantity;
                }else{
                    delta = 0;
                }
            }
            //更新勾选状态
            cartItemMapper.update(new LambdaUpdateWrapper<CartItem>()
                    .eq(CartItem::getUserId,userId)
                    .eq(CartItem::getProductId,productId)
                    .set(CartItem::getChecked, checked ? 1:0));
            //数量有变化，需要更新购物车
            if (delta != 0){
                cartMapper.update(new LambdaUpdateWrapper<Cart>()
                        .eq(Cart::getUserId,userId)
                        .setSql("checked_count = checked_count + " + delta));
                log.info("用户 {} 商品 {} 勾选状态更新成功 → {}, checked_count 变化: {}",
                        userId, productId, checked, delta);
            }

        }catch (Exception e){
            log.error("购物车勾选状态同步失败，userId={}, productId={}, checked={}",
                    userId, productId, checked, e);
            throw new RuntimeException("勾选状态落库失败", e);
        }

    }
}
