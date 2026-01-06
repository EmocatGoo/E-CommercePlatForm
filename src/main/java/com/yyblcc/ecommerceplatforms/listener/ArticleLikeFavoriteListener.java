package com.yyblcc.ecommerceplatforms.listener;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yyblcc.ecommerceplatforms.domain.message.ArticleLikeFavoriteMessage;
import com.yyblcc.ecommerceplatforms.domain.po.Article;
import com.yyblcc.ecommerceplatforms.domain.po.UserArticleFavorite;
import com.yyblcc.ecommerceplatforms.domain.po.UserArticleLike;
import com.yyblcc.ecommerceplatforms.mapper.ArticleFavoriteMapper;
import com.yyblcc.ecommerceplatforms.mapper.ArticleLikeMapper;
import com.yyblcc.ecommerceplatforms.mapper.ArticleMapper;
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
        topic = "article-like-favorite-topic",
        consumerGroup = "article-like-favorite-consumer-group",
        consumeMode = ConsumeMode.CONCURRENTLY
)
@RequiredArgsConstructor
@Slf4j
public class ArticleLikeFavoriteListener implements RocketMQListener<ArticleLikeFavoriteMessage> {
    private final ArticleLikeMapper articleLikeMapper;
    private final ArticleFavoriteMapper articleFavoriteMapper;
    private final ArticleMapper articleMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private static final String IDEMPOTENT_KEY = "like_favorite:article:idempotent:";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onMessage(ArticleLikeFavoriteMessage message) {
        String key = IDEMPOTENT_KEY + message.getRequestId();
        if (Boolean.TRUE.equals(stringRedisTemplate.opsForValue().setIfAbsent(key,"1", Duration.ofMinutes(10)))) {
            doAction(message);
        }else {
            log.info("重复消息，已被消费: {}", message.getRequestId());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void doAction(ArticleLikeFavoriteMessage message){
        if ("LIKE".equals(message.getType())){
            handleLikeAction(message);
        } else if ("FAVORITE".equals(message.getType())) {
            handleFavoriteAction(message);
        }
    }
    private void handleLikeAction(ArticleLikeFavoriteMessage message) {
        UserArticleLike existingLike = articleLikeMapper.selectOne(
                new LambdaQueryWrapper<UserArticleLike>()
                        .eq(UserArticleLike::getArticleId, message.getArticleId())
                        .eq(UserArticleLike::getUserId, message.getUserId())
        );

        if (message.getAction().equals(1)) {
            if (existingLike == null) {
                articleLikeMapper.insert(UserArticleLike.builder()
                        .articleId(message.getArticleId())
                        .userId(message.getUserId())
                        .createTime(LocalDateTime.now())
                        .updateTime(LocalDateTime.now())
                        .status(1)
                        .build());
            } else {
                existingLike.setStatus(1);
                existingLike.setUpdateTime(LocalDateTime.now());
                articleLikeMapper.updateById(existingLike);
            }
            articleMapper.update(new LambdaUpdateWrapper<Article>()
                    .eq(Article::getId, message.getArticleId())
                    .setSql("like_count = like_count + 1"));
        } else {
            if (existingLike != null && existingLike.getStatus() != 0) {
                existingLike.setStatus(0);
                existingLike.setUpdateTime(LocalDateTime.now());
                articleLikeMapper.updateById(existingLike);
                articleMapper.update(new LambdaUpdateWrapper<Article>()
                        .eq(Article::getId, message.getArticleId())
                        .setSql("like_count = like_count - 1"));
            }
        }
    }
    private void handleFavoriteAction(ArticleLikeFavoriteMessage message) {
        UserArticleFavorite existingFavorite = articleFavoriteMapper.selectOne(
                new LambdaQueryWrapper<UserArticleFavorite>()
                        .eq(UserArticleFavorite::getArticleId, message.getArticleId())
                        .eq(UserArticleFavorite::getUserId, message.getUserId())
        );
        if (message.getAction().equals(1)) {
            if (existingFavorite == null) {
                articleFavoriteMapper.insert(UserArticleFavorite.builder()
                        .articleId(message.getArticleId())
                        .userId(message.getUserId())
                        .createTime(LocalDateTime.now())
                        .updateTime(LocalDateTime.now())
                        .status(1)
                        .build());
            } else {
                existingFavorite.setStatus(1);
                existingFavorite.setUpdateTime(LocalDateTime.now());
                articleFavoriteMapper.updateById(existingFavorite);
            }
            articleMapper.update(new LambdaUpdateWrapper<Article>()
                    .eq(Article::getId, message.getArticleId())
                    .setSql("favorite_count = favorite_count + 1"));
        } else {
            if (existingFavorite != null && existingFavorite.getStatus() != 0) {
                existingFavorite.setStatus(0);
                existingFavorite.setUpdateTime(LocalDateTime.now());
                articleFavoriteMapper.updateById(existingFavorite);
                articleMapper.update(new LambdaUpdateWrapper<Article>()
                        .eq(Article::getId, message.getArticleId())
                        .setSql("favorite_count = favorite_count - 1"));
            }
        }
    }
}
