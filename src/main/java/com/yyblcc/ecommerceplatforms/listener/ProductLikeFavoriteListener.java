package com.yyblcc.ecommerceplatforms.listener;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yyblcc.ecommerceplatforms.domain.message.ProductLikeFavoriteMessage;
import com.yyblcc.ecommerceplatforms.domain.po.Product;
import com.yyblcc.ecommerceplatforms.domain.po.UserProductFavorite;
import com.yyblcc.ecommerceplatforms.domain.po.UserProductLike;
import com.yyblcc.ecommerceplatforms.mapper.ProductFavoriteMapper;
import com.yyblcc.ecommerceplatforms.mapper.ProductLikeMapper;
import com.yyblcc.ecommerceplatforms.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RocketMQMessageListener(
        topic = "product-like-favorite-topic",
        consumerGroup = "product-like-favorite-consumer-group",
        consumeMode = ConsumeMode.CONCURRENTLY
)
@RequiredArgsConstructor
@Slf4j
public class ProductLikeFavoriteListener implements RocketMQListener<ProductLikeFavoriteMessage> {
    private final ProductLikeMapper productLikeMapper;
    private final ProductFavoriteMapper  productFavoriteMapper;
    private final ProductMapper productMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private static final String IDEMPOTENT_KEY = "like_collect:product:idempotent:";
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onMessage(ProductLikeFavoriteMessage productLikeFavoriteMessage) {
        String key = IDEMPOTENT_KEY + productLikeFavoriteMessage.getRequestId();
        if (Boolean.TRUE.equals(stringRedisTemplate.opsForValue().setIfAbsent(key,"1", Duration.ofMinutes(10)))) {
            doAction(productLikeFavoriteMessage);
        }else {
            log.info("重复消息，已被消费: {}", productLikeFavoriteMessage.getRequestId());
        }
    }
    @Transactional(rollbackFor = Exception.class)
    public void doAction(ProductLikeFavoriteMessage msg) {
        if ("LIKE".equals(msg.getType())) {
            handleLikeAction(msg);
        } else if ("FAVORITE".equals(msg.getType())) {
            handleFavoriteAction(msg);
        }
    }

    private void handleLikeAction(ProductLikeFavoriteMessage msg){
        UserProductLike existingLike = productLikeMapper.selectOne(
                new LambdaQueryWrapper<UserProductLike>()
                        .eq(UserProductLike::getUserId, msg.getUserId())
                        .eq(UserProductLike::getProductId, msg.getProductId())
        );
        if (msg.getAction().equals(1)){
            if (existingLike == null){
                productLikeMapper.insert(UserProductLike.builder()
                        .productId(msg.getProductId())
                        .userId(msg.getUserId())
                        .createTime(LocalDateTime.now())
                        .updateTime(LocalDateTime.now())
                        .status(1)
                        .build());
            }else{
                existingLike.setStatus(1);
                existingLike.setUpdateTime(LocalDateTime.now());
                productLikeMapper.updateById(existingLike);
            }
            productMapper.update(new LambdaUpdateWrapper<Product>()
                    .eq(Product::getId, msg.getProductId())
                    .setSql("like_count = like_count + 1"));
        }else{
            if (existingLike != null && existingLike.getStatus() != 0){
                existingLike.setStatus(0);
                existingLike.setUpdateTime(LocalDateTime.now());
                productLikeMapper.updateById(existingLike);
                productMapper.update(new LambdaUpdateWrapper<Product>()
                    .eq(Product::getId, msg.getProductId())
                    .setSql("like_count = like_count - 1"));
             }
         }
     }
     private void handleFavoriteAction(ProductLikeFavoriteMessage msg){
        UserProductFavorite existingFavorite = productFavoriteMapper.selectOne(
                new LambdaQueryWrapper<UserProductFavorite>()
                .eq(UserProductFavorite::getUserId, msg.getUserId())
                .eq(UserProductFavorite::getProductId, msg.getProductId())
        );
        if (msg.getAction().equals(1)){
            if (existingFavorite == null){
                productFavoriteMapper.insert(UserProductFavorite.builder()
                        .productId(msg.getProductId())
                        .userId(msg.getUserId())
                        .createTime(LocalDateTime.now())
                        .updateTime(LocalDateTime.now())
                        .status(1)
                        .build());
            }else{
                existingFavorite.setStatus(1);
                existingFavorite.setUpdateTime(LocalDateTime.now());
                productFavoriteMapper.updateById(existingFavorite);
            }
            productMapper.update(new LambdaUpdateWrapper<Product>()
                    .eq(Product::getId, msg.getProductId())
                    .setSql("favorite_count = favorite_count + 1"));
        }else{
            if (existingFavorite != null && existingFavorite.getStatus() != 0){
                existingFavorite.setStatus(0);
                existingFavorite.setUpdateTime(LocalDateTime.now());
                productFavoriteMapper.updateById(existingFavorite);
                productMapper.update(new LambdaUpdateWrapper<Product>()
                        .eq(Product::getId, msg.getProductId())
                        .setSql("favorite_count = favorite_count - 1"));
            }
        }
     }
 }
