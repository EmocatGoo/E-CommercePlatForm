package com.yyblcc.ecommerceplatforms.listener;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yyblcc.ecommerceplatforms.domain.message.LikeFavoriteMessage;
import com.yyblcc.ecommerceplatforms.domain.po.Product;
import com.yyblcc.ecommerceplatforms.domain.po.Result;
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
public class ProductLikeFavoriteListener implements RocketMQListener<LikeFavoriteMessage> {
    private final ProductLikeMapper productLikeMapper;
    private final ProductFavoriteMapper  productFavoriteMapper;
    private final ProductMapper productMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private static final String IDEMPOTENT_KEY = "like_collect:idempotent:";
    @Override
    public void onMessage(LikeFavoriteMessage likeFavoriteMessage) {
        String key = IDEMPOTENT_KEY + likeFavoriteMessage.getRequestId();
        if (Boolean.TRUE.equals(stringRedisTemplate.opsForValue().setIfAbsent(key,"1", Duration.ofMinutes(10)))) {
            doAction(likeFavoriteMessage);
        }else {
            log.info("重复消息，已被消费: {}", likeFavoriteMessage.getRequestId());
        }
    }
    @Transactional(rollbackFor = Exception.class)
    public void doAction(LikeFavoriteMessage msg) {
        if ("LIKE".equals(msg.getType())) {
            if (msg.getAction().equals(1)) {
                productLikeMapper.insert(UserProductLike.builder()
                    .productId(msg.getProductId())
                    .userId(msg.getUserId())
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .build());
                productMapper.update(new LambdaUpdateWrapper<Product>()
                    .eq(Product::getId, msg.getProductId())
                    .setSql("like_count = like_count + 1")
                    .last("FOR UPDATE"));
            } else {
                UserProductLike productLike = productLikeMapper.selectOne(new LambdaQueryWrapper<UserProductLike>()
                        .eq(UserProductLike::getUserId, msg.getUserId())
                        .eq(UserProductLike::getProductId, msg.getProductId()));
                productLike.setStatus(0);
                productLikeMapper.updateById(productLike);
                productMapper.update(new LambdaUpdateWrapper<Product>()
                        .eq(Product::getId, productLike.getProductId())
                        .setSql("like_count = like_count - 1")
                        .last("FOR UPDATE"));
            }
        } else if ("FAVORITE".equals(msg.getType())) {
            if (msg.getAction() == 1) {
                productFavoriteMapper.insert(UserProductFavorite.builder()
                        .productId(msg.getProductId())
                        .userId(msg.getUserId())
                        .createTime(LocalDateTime.now())
                        .updateTime(LocalDateTime.now())
                        .build());
                productMapper.update(new LambdaUpdateWrapper<Product>()
                        .eq(Product::getId, msg.getProductId())
                        .setSql("favorite_count = favorite_count + 1")
                        .last("FOR UPDATE"));
            } else {
                UserProductFavorite productFavorite = productFavoriteMapper.selectOne(new LambdaQueryWrapper<UserProductFavorite>()
                        .eq(UserProductFavorite::getUserId, msg.getUserId())
                        .eq(UserProductFavorite::getProductId, msg.getProductId()));
                productFavorite.setStatus(0);
                productFavoriteMapper.updateById(productFavorite);
                productMapper.update(new LambdaUpdateWrapper<Product>()
                        .eq(Product::getId, productFavorite.getProductId())
                        .setSql("like_count = like_count - 1")
                        .last("FOR UPDATE"));
            }
        }
    }
}
