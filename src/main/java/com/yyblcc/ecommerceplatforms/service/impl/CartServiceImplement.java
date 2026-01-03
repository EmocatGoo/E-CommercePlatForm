package com.yyblcc.ecommerceplatforms.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yyblcc.ecommerceplatforms.domain.DTO.AddCartDTO;
import com.yyblcc.ecommerceplatforms.domain.DTO.CartItemToggle;
import com.yyblcc.ecommerceplatforms.domain.Enum.ProductStatusEnum;
import com.yyblcc.ecommerceplatforms.domain.VO.CartItemVO;
import com.yyblcc.ecommerceplatforms.domain.VO.CartVO;
import com.yyblcc.ecommerceplatforms.domain.message.AddCartMessage;
import com.yyblcc.ecommerceplatforms.domain.message.CartDeleteMessage;
import com.yyblcc.ecommerceplatforms.domain.message.CheckMessage;
import com.yyblcc.ecommerceplatforms.domain.message.UpdateCartQuantityMessage;
import com.yyblcc.ecommerceplatforms.domain.po.Cart;
import com.yyblcc.ecommerceplatforms.domain.po.CartItem;
import com.yyblcc.ecommerceplatforms.domain.po.Product;
import com.yyblcc.ecommerceplatforms.domain.po.Result;
import com.yyblcc.ecommerceplatforms.exception.BusinessException;
import com.yyblcc.ecommerceplatforms.mapper.CartItemMapper;
import com.yyblcc.ecommerceplatforms.mapper.CartMapper;
import com.yyblcc.ecommerceplatforms.mapper.ProductMapper;
import com.yyblcc.ecommerceplatforms.service.CartService;
import com.yyblcc.ecommerceplatforms.util.StpKit;
import com.yyblcc.ecommerceplatforms.util.context.AuthContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.*;


@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImplement extends ServiceImpl<CartMapper, Cart> implements CartService {

    private final StringRedisTemplate stringRedisTemplate;
    private final RocketMQTemplate rocketMQTemplate;
    private final ProductMapper productMapper;
    private static final String CART_HASH = "cart:user:";
    private static final String CART_ITEMS_HASH = "cart:items:";
    private final CartItemMapper cartItemMapper;

    @Override
    public void addItem(Long userId, AddCartDTO dto) {
        Product product = productMapper.selectById(dto.getProductId());
        if (product == null || !product.getStatus().equals(ProductStatusEnum.LISTED.getCode())){
            throw new BusinessException("商品不存在或已下架");
        }

        String userKey = CART_HASH + userId;
        String itemsKey = CART_ITEMS_HASH + userId;
        String itemHashKey = "product:" + product.getId();

        stringRedisTemplate.opsForHash().increment(userKey,"item_count",dto.getQuantity());
        if (Boolean.TRUE.equals(dto.getChecked())){
            stringRedisTemplate.opsForHash().increment(userKey,"checked_count",dto.getQuantity());
        }
        CartItem dbCartItem = cartItemMapper.selectOne(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getProductId, product.getId())
                .eq(CartItem::getUserId, userId));
        if (dbCartItem != null){
            dbCartItem.setQuantity(dbCartItem.getQuantity() + dto.getQuantity());
            stringRedisTemplate.opsForHash().put(itemsKey,itemHashKey, JSON.toJSONString(dbCartItem));
            return;
        }
        CartItem cartItem = CartItem.builder()
                .craftsmanId(product.getCraftsmanId())
                .productId(product.getId())
                .quantity(dto.getQuantity())
                .productName(product.getProductName())
                .productImage(product.getImageUrl().getFirst())
                .stock(product.getStock())
                .price(product.getPrice())
                .checked(Boolean.TRUE.equals(dto.getChecked()) ? 1 : 0)
                .build();

        stringRedisTemplate.opsForHash().put(itemsKey,itemHashKey, JSON.toJSONString(cartItem));
        AddCartMessage message = AddCartMessage.builder()
                .userId(userId)
                .productId(product.getId())
                .craftsmanId(product.getCraftsmanId())
                .productName(product.getProductName())
                .productImage(product.getImageUrl().getFirst())
                .priceAtAdd(product.getPrice())
                .quantity(dto.getQuantity())
                .checked(Boolean.TRUE.equals(dto.getChecked()))
                .build();
        rocketMQTemplate.asyncSend("cart-add-topic", message, new SendCallback() {
            @Override public void onSuccess(SendResult result) {

            }
            @Override public void onException(Throwable e) {
                log.error("MQ发送失败", e);
            }
        });
    }

    @Override
    public void toggleCheck(Long userId, Long productId, Boolean checked) {
        String userKey = CART_HASH + userId;
        String itemsKey = CART_ITEMS_HASH + userId;
        String itemHashKey = "product:" + productId;

        String json = (String) stringRedisTemplate.opsForHash().get(itemsKey, itemHashKey);
        log.warn("是否是勾选？当前勾选的布尔值为：{}", checked);
        if (json == null){
            throw new BusinessException("商品不在购物车");
        }
        CartItem item = JSON.parseObject(json, CartItem.class);
        int delta = checked ? item.getQuantity() : -item.getQuantity();

        stringRedisTemplate.opsForHash().increment(userKey, "checked_count", delta);
        item.setChecked(checked ? 1 : 0);
        stringRedisTemplate.opsForHash().put(itemsKey, itemHashKey, JSON.toJSONString(item));
        CheckMessage checkMessageMessage = CheckMessage.builder()
                                        .userId(userId)
                                        .productId(productId)
                                        .checked(checked)
                                        .quantity(item.getQuantity())
                                        .build();
        rocketMQTemplate.asyncSend("cart-check-topic", checkMessageMessage, new SendCallback() {
            @Override public void onSuccess(SendResult result) {

            }
            @Override public void onException(Throwable e) {
                log.error("MQ发送失败",e);
            }
        });
    }


    @Override
    public CartVO getMyCart(Long userId) {
        String userKey = CART_HASH + userId;
        String itemsKey = CART_ITEMS_HASH + userId;
        
        // 检查Redis中是否存在用户购物车信息，如果不存在则从数据库加载
        Map<Object,Object> userMap = stringRedisTemplate.opsForHash().entries(userKey);
        Map<Object,Object> itemsMap = stringRedisTemplate.opsForHash().entries(itemsKey);
        
        // 如果Redis中没有数据，则从数据库加载
        if (userMap.isEmpty() && itemsMap.isEmpty()) {
            loadCartFromDatabaseToRedis(userId, userKey, itemsKey);
            userMap = stringRedisTemplate.opsForHash().entries(userKey);
            itemsMap = stringRedisTemplate.opsForHash().entries(itemsKey);
        }

        // 处理用户购物车统计信息
        int itemCount = 0;
        int checkedCount = 0;
        if (!userMap.isEmpty()) {
            Object itemCountObj = userMap.get("item_count");
            if (itemCountObj != null) {
                try {
                    itemCount = Integer.parseInt(itemCountObj.toString());
                } catch (NumberFormatException e) {
                    itemCount = 0;
                }
            }
            
            Object checkedCountObj = userMap.get("checked_count");
            if (checkedCountObj != null) {
                try {
                    checkedCount = Integer.parseInt(checkedCountObj.toString());
                } catch (NumberFormatException e) {
                    checkedCount = 0;
                }
            }
        }

        List<CartItemVO> cartItemVOS = new ArrayList<>();
        if (!itemsMap.isEmpty()) {
            cartItemVOS = itemsMap.values().stream().map(o -> {
                CartItem cartItem = JSON.parseObject(o.toString(), CartItem.class);
                Product product = productMapper.selectById(cartItem.getProductId());
                
                if (product == null) {
                    return null;
                }

                BigDecimal savePrice = cartItem.getPrice().subtract(product.getPrice()).multiply(new BigDecimal(cartItem.getQuantity()));

                return CartItemVO.builder()
                        .productId(product.getId())
                        .productName(product.getProductName())
                        .image(product.getImageUrl().getFirst())
                        .priceAtAdd(cartItem.getPrice())
                        .currentPrice(product.getPrice())
                        .quantity(cartItem.getQuantity())
                        .checked(cartItem.getChecked() == 1)
                        .savePrice(savePrice)
                        .build();
            })
            .filter(Objects::nonNull)
                    .toList();
        }

        return CartVO.builder()
                .itemCount(itemCount)
                .checkedCount(checkedCount)
                .items(cartItemVOS)
                .build();
    }
    
    private void loadCartFromDatabaseToRedis(Long userId, String userKey, String itemsKey) {
        LambdaQueryWrapper<Cart> cartQueryWrapper = new LambdaQueryWrapper<>();
        cartQueryWrapper.eq(Cart::getUserId, userId);
        Cart cart = this.getOne(cartQueryWrapper);
        
        if (cart != null) {
            stringRedisTemplate.opsForHash().put(userKey, "item_count", String.valueOf(cart.getItemCount()));
            stringRedisTemplate.opsForHash().put(userKey, "checked_count", String.valueOf(cart.getCheckedCount()));
            stringRedisTemplate.expire(userKey, Duration.ofHours(2));

            List<CartItem> cartItems = cartItemMapper.selectList(new LambdaQueryWrapper<CartItem>().eq(CartItem::getCartId,cart.getId()));
            if (cartItems != null && !cartItems.isEmpty()) {
                for (CartItem cartItem : cartItems) {
                    String itemHashKey = "product:" + cartItem.getProductId();
                    stringRedisTemplate.opsForHash().put(itemsKey, itemHashKey, JSON.toJSONString(cartItem));
                    stringRedisTemplate.expire(itemsKey, Duration.ofHours(2));
                }
            }
        }
    }

    @Override
    public Result<?> batchdelete(List<Long> productIds) {
        if (productIds.isEmpty()) {
            return Result.error("没有可删除项!");
        }

        Long userId = StpKit.USER.getLoginIdAsLong();
        String userKey = CART_HASH + userId;
        String itemsKey = CART_ITEMS_HASH + userId;

        List<String> deleteKeys = new ArrayList<>();

        int totalDecrement = 0;
        int checkedDecrement = 0;

        for (Long productId : productIds) {
            String itemHashKey = "product:" + productId;
            String json = (String) stringRedisTemplate.opsForHash().get(itemsKey, itemHashKey);
            if (json != null){
                CartItem item = JSON.parseObject(json, CartItem.class);
                totalDecrement += item.getQuantity();
                if (item.getChecked() == 1) {
                    checkedDecrement += item.getQuantity();
                }
                deleteKeys.add(itemHashKey);
                stringRedisTemplate.opsForHash().delete(itemsKey, itemHashKey);
            }
        }

        if (!deleteKeys.isEmpty()){
            stringRedisTemplate.opsForHash().delete(itemsKey, deleteKeys);
            stringRedisTemplate.opsForHash().increment(userKey,"item_count", -totalDecrement);
            if (checkedDecrement > 0) {
                stringRedisTemplate.opsForHash().increment(userKey,"checked_count", -checkedDecrement);
            }
        }
        CartDeleteMessage message = CartDeleteMessage.builder()
                                                    .userId(userId)
                                                    .productIds(productIds).build();
        rocketMQTemplate.asyncSend("cart-delete-topic",message,new SendCallback() {
            @Override public void onSuccess(SendResult result) {

            }
            @Override public void onException(Throwable e) {
                log.error("mq消息发送失败!");
            }
        });

        return Result.success("删除成功!");
    }

    @Override
    public Result<?> delete(Long productId) {
        Long userId = StpKit.USER.getLoginIdAsLong();
        String userKey = CART_HASH + userId;
        String itemsKey = CART_ITEMS_HASH + userId;
        String itemHashKey = "product:" + productId;
        String cartItemCache = (String) stringRedisTemplate.opsForHash().get(itemsKey, itemHashKey);
        if (cartItemCache == null){
            return Result.success("没有可删除的商品信息");
        }
        CartItem cartItem = JSON.parseObject(cartItemCache, CartItem.class);
        int quantity = cartItem.getQuantity();
        boolean checked = cartItem.getChecked() == 1;

        stringRedisTemplate.opsForHash().delete(itemsKey, itemHashKey);
        stringRedisTemplate.opsForHash().increment(userKey,"item_count", -quantity);
        if (checked) {
            stringRedisTemplate.opsForHash().increment(userKey,"checked_count", -quantity);
        }
        CartDeleteMessage message = CartDeleteMessage.builder()
                                                    .userId(userId)
                                                    .productIds(Collections.singletonList(productId))
                                                    .build();
        rocketMQTemplate.asyncSend("cart-delete-topic",message,new SendCallback() {
            @Override public void onSuccess(SendResult result) {

            }
            @Override public void onException(Throwable e) {
                log.error("MQ发送失败",e);
            }
        });

        return Result.success("删除成功!");
    }

    @Override
    public void toggleQuantity(CartItemToggle dto) {
        Long userId = StpKit.USER.getLoginIdAsLong();
        String userKey = CART_HASH + userId;
        String itemsKey = CART_ITEMS_HASH + userId;
        String itemHashKey = "product:" + dto.getProductId();

        // 从Redis获取购物车项目
        String json = (String) stringRedisTemplate.opsForHash().get(itemsKey, itemHashKey);
        if (json == null) {
            throw new BusinessException("商品不在购物车中");
        }
        Integer newQuantity = dto.getQuantity();
        CartItem cartItem = JSON.parseObject(json, CartItem.class);
        Integer lastQuantity = cartItem.getQuantity();
        int quantityDiff = newQuantity - lastQuantity;
        stringRedisTemplate.opsForHash().increment(userKey, "item_count", quantityDiff);
        if(cartItem.getChecked().equals(1)){
            stringRedisTemplate.opsForHash().increment(userKey, "checked_count", quantityDiff);
        }
        cartItem.setQuantity(newQuantity);
        stringRedisTemplate.opsForHash().put(itemsKey, itemHashKey, JSON.toJSONString(cartItem));
        UpdateCartQuantityMessage message = UpdateCartQuantityMessage.builder()
                                                                .userId(userId)
                                                                .productId(dto.getProductId())
                                                                .cartId(cartItem.getCartId())
                                                                .quantity(cartItem.getQuantity())
                                                                .checked(cartItem.getChecked())
                                                                .build();
        rocketMQTemplate.asyncSend("cart-update-topic", message, new SendCallback() {
            @Override public void onSuccess(SendResult result) {
                log.info("购物车数量更新消息发送成功");
            }
            @Override public void onException(Throwable e) {
                log.error("购物车数量更新消息发送失败", e);
            }
        });
    }


}
