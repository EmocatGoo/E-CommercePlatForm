package com.yyblcc.ecommerceplatforms.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yyblcc.ecommerceplatforms.domain.DTO.AddCartDTO;
import com.yyblcc.ecommerceplatforms.domain.DTO.CartItemRedis;
import com.yyblcc.ecommerceplatforms.domain.VO.CartItemVO;
import com.yyblcc.ecommerceplatforms.domain.VO.CartVO;
import com.yyblcc.ecommerceplatforms.domain.message.AddCartMessage;
import com.yyblcc.ecommerceplatforms.domain.message.CheckEvent;
import com.yyblcc.ecommerceplatforms.domain.po.Cart;
import com.yyblcc.ecommerceplatforms.domain.po.CartItem;
import com.yyblcc.ecommerceplatforms.domain.po.Product;
import com.yyblcc.ecommerceplatforms.exception.BusinessException;
import com.yyblcc.ecommerceplatforms.mapper.CartMapper;
import com.yyblcc.ecommerceplatforms.mapper.ProductMapper;
import com.yyblcc.ecommerceplatforms.service.CartService;
import lombok.RequiredArgsConstructor;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class CartServiceImplement extends ServiceImpl<CartMapper, Cart> implements CartService {

    private final StringRedisTemplate stringRedisTemplate;
    private final RocketMQTemplate rocketMQTemplate;
    private final ProductMapper productMapper;
    private static final String CART_HASH = "cart:user:";
    private static final String CART_ITEMS_HASH = "cart:items:";

    @Override
    public void addItem(Long userId, AddCartDTO dto) {

        Product product = productMapper.selectById(dto.getProductId());
        if (product == null || product.getStatus() != 1){
            throw new BusinessException("商品不存在或已下架");
        }

        String userKey = CART_HASH + userId;
        String itemsKey = CART_ITEMS_HASH + userId;
        String itemHashKey = "item:" + product.getId();

        stringRedisTemplate.opsForHash().increment(userKey,"item_count",dto.getQuantity());
        if (Boolean.TRUE.equals(dto.getIsChecked())){
            stringRedisTemplate.opsForHash().increment(userKey,"checked_count",dto.getQuantity());
        }
        CartItem cartItem = CartItem.builder()
                .craftsmanId(product.getCraftsmanId())
                .productId(product.getId())
                .quantity(dto.getQuantity())
                .productName(product.getProductName())
                .productImage(product.getImageUrl())
                .price(product.getPrice())
                .isChecked(Boolean.TRUE.equals(dto.getIsChecked()) ? 1 : 0)
                .build();

        stringRedisTemplate.opsForHash().put(itemsKey,itemHashKey, JSON.toJSONString(cartItem));
        AddCartMessage message = AddCartMessage.builder()
                .userId(userId)
                .productId(product.getId())
                .craftsmanId(product.getCraftsmanId())
                .productName(product.getProductName())
                .productImage(product.getImageUrl())
                .priceAtAdd(product.getPrice())
                .quantity(dto.getQuantity())
                .checked(Boolean.TRUE.equals(dto.getIsChecked()))
                .build();
        rocketMQTemplate.asyncSend("cart-add-topic", message, new SendCallback() {
            @Override public void onSuccess(SendResult result) { }
            @Override public void onException(Throwable e) {
                log.error("MQ发送失败", e);
            }
        });
    }

    @Override
    public void toggleCheck(Long userId, Long productId, Boolean checked) {
        String userKey = CART_HASH + userId;
        String itemsKey = CART_ITEMS_HASH + userId;
        String itemHashKey = "item:" + productId;

        String json = (String) stringRedisTemplate.opsForHash().get(itemsKey, itemHashKey);
        if (json == null){
            throw new BusinessException("商品不在购物车");
        }
        CartItem item = JSON.parseObject(json, CartItem.class);
        int delta = checked ? item.getQuantity() : -item.getQuantity();

        stringRedisTemplate.opsForHash().increment(userKey, "checked_count", delta);
        item.setIsChecked(checked ? 1 : 0);
        stringRedisTemplate.opsForHash().put(itemsKey, itemHashKey, JSON.toJSONString(item));
        CheckEvent checkEventMessage = CheckEvent.builder()
                                        .userId(userId)
                                        .productId(productId)
                                        .checked(checked)
                                        .quantity(item.getQuantity())
                                        .build();
        rocketMQTemplate.asyncSend("cart-check-topic", checkEventMessage, new SendCallback() {
            @Override public void onSuccess(SendResult result) { }
            @Override public void onException(Throwable e) {
                log.error("MQ发送失败",e);
            }
        });
    }

    @Override
    public CartVO getMyCart(Long userId) {
        String userKey = CART_HASH + userId;
        String itemsKey = CART_ITEMS_HASH + userId;

        Map<Object,Object> userMap = stringRedisTemplate.opsForHash().entries(userKey);
        Map<Object,Object> itemsMap = stringRedisTemplate.opsForHash().entries(itemsKey);

        Integer itemCount = Integer.valueOf(userMap.getOrDefault("item_count",0).toString());
        Integer checkedCount = Integer.valueOf(itemsMap.getOrDefault("checked_count",0).toString());

        List<CartItemVO> cartItemVOS = itemsMap.entrySet().stream().map(entry ->{
            CartItem cartItem = JSON.parseObject(entry.getValue().toString(), CartItem.class);
            Product product = productMapper.selectById(cartItem.getProductId());
            BigDecimal savePrice = cartItem.getPrice().subtract(product.getPrice()).multiply(new BigDecimal(cartItem.getQuantity()));

            return CartItemVO.builder()
                    .productId(product.getId())
                    .productName(product.getProductName())
                    .image(product.getImageUrl())
                    .priceAtAdd(cartItem.getPrice())
                    .currentPrice(product.getPrice())
                    .quantity(cartItem.getQuantity())
                    .checked(cartItem.getIsChecked() == 1)
                    .savePrice(savePrice)
                    .build();
        }).toList();

        return CartVO.builder()
                .itemCount(itemCount)
                .checkedCount(checkedCount)
                .items(cartItemVOS)
                .build();
    }
}
