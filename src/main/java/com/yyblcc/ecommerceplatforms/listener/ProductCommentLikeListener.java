package com.yyblcc.ecommerceplatforms.listener;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yyblcc.ecommerceplatforms.domain.DTO.ProductCommentLikeDTO;
import com.yyblcc.ecommerceplatforms.domain.message.ProductCommentLikeMessage;
import com.yyblcc.ecommerceplatforms.domain.po.ProductCommentLike;
import com.yyblcc.ecommerceplatforms.mapper.ProductCommentLikeMapper;
import com.yyblcc.ecommerceplatforms.service.ProductCommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RocketMQMessageListener(
        topic = "product-comment-like-topic",
        consumerGroup = "product-comment-like-consumer-group",
        consumeMode = ConsumeMode.CONCURRENTLY
)
@RequiredArgsConstructor
public class ProductCommentLikeListener implements RocketMQListener<ProductCommentLikeMessage> {
    private final ProductCommentLikeMapper productCommentLikeMapper;
    private final ProductCommentService productCommentService;

    @Override
    public void onMessage(ProductCommentLikeMessage message) {
        Long userId = message.getUserId();
        Long commentId = message.getCommentId();
        Long productId = message.getProductId();
        boolean liked = message.isLiked();

        if (liked) {
            int row = productCommentLikeMapper.update(new LambdaUpdateWrapper<ProductCommentLike>()
                    .eq(ProductCommentLike::getCommentId, commentId)
                    .eq(ProductCommentLike::getUserId, userId)
                    .eq(ProductCommentLike::getProductId, productId)
                    .set(ProductCommentLike::getStatus, 0)
                    .set(ProductCommentLike::getUpdateTime, LocalDateTime.now())
                    .last("FOR UPDATE"));
            if (row > 0) {
                productCommentService.update().setSql("like_count = like_count - 1")
                        .eq("id", commentId).update();
            }
        }else {
            ProductCommentLike productCommentLike = new ProductCommentLike();
            BeanUtils.copyProperties(message,productCommentLike);
            productCommentLike.setCreateTime(LocalDateTime.now());
            productCommentLike.setUpdateTime(LocalDateTime.now());
            productCommentLike.setStatus(1);
            int row = productCommentLikeMapper.insert(productCommentLike);
            if (row > 0) {
                productCommentService.update().setSql("like_count = like_count + 1")
                        .eq("id", commentId).update();
            }
        }
    }
}
