package com.yyblcc.ecommerceplatforms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yyblcc.ecommerceplatforms.constant.StatusConstant;
import com.yyblcc.ecommerceplatforms.domain.VO.ArticleVO;
import com.yyblcc.ecommerceplatforms.domain.po.Article;
import com.yyblcc.ecommerceplatforms.mapper.ArticleMapper;
import jodd.util.CollectionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArticleRankService {
    private final StringRedisTemplate stringRedisTemplate;

    private final ArticleMapper articleMapper;

    private static final String ARTICLE_HOT_ZSET_KEY = "article:hot:zset:type3";
    private static final String ARTICLE_HOT_CACHE_KEY = "article:heritage:hot";
    private static final String ARTICLE_VIEW_PREFIX = "article:view:";

    @Async
    public void recordArticleViewForZSet(Long articleId,Long userId){
        try{
            String today = LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE);
            String viewKey = ARTICLE_VIEW_PREFIX + articleId + ":" + today;

            Boolean isMember = stringRedisTemplate.opsForSet().isMember(viewKey, userId.toString());
            if (Boolean.TRUE.equals(isMember)) {
                log.info("用户{}今天已查看过文章{}", userId, articleId);
                return;
            }

            Double currentScore = stringRedisTemplate.opsForZSet().incrementScore(ARTICLE_HOT_ZSET_KEY, articleId.toString(), 1);

            log.debug("文章{} ZSet分数更新为：{}", articleId, currentScore);

        }catch (Exception e){
            log.error("更新文章 {} ZSet分数失败，userId: {}",articleId,userId,e);
        }
    }


    public List<ArticleVO> getHotArticlesFromZSet(int limit){
        try{
            Set<String> articleIds = stringRedisTemplate.opsForZSet().reverseRange(ARTICLE_HOT_ZSET_KEY, 0, limit - 1);

            if (CollectionUtils.isEmpty(articleIds)){
                return Collections.emptyList();
            }

            List<Long> idList = articleIds.stream().map(Long::parseLong).toList();

            List<Article> articles = articleMapper.selectList(new LambdaQueryWrapper<Article>()
                    .in(Article::getId, idList)
                    .eq(Article::getArticleType,3)
                    .eq(Article::getStatus, StatusConstant.ENABLE));

            Map<Long, Article> articleMap = articles.stream()
                    .collect(Collectors.toMap(Article::getId, Function.identity()));

            List<ArticleVO> result = new ArrayList<>();
            for (String idStr : articleIds) {
                Long articleId = Long.parseLong(idStr);
                Article article = articleMap.get(articleId);
                if (article != null){
                    ArticleVO vo = new ArticleVO();
                    BeanUtils.copyProperties(article, vo);
                    result.add(vo);
                }
            }
            return result;
        }catch (Exception e){
            log.error("获取热门文章失败", e);
            return Collections.emptyList();
        }
    }

    public void initZSetFromDB(){
        try{
            List<Article> hotArticles = articleMapper.selectList(new LambdaQueryWrapper<Article>()
                    .eq(Article::getArticleType,3)
                    .eq(Article::getStatus, StatusConstant.ENABLE)
                    .orderByDesc(Article::getViewCount)
                    .last("LIMIT 20"));

            for (Article article : hotArticles){
                stringRedisTemplate.opsForZSet().add(ARTICLE_HOT_ZSET_KEY, article.getId().toString(), article.getViewCount());
            }
            stringRedisTemplate.expire(ARTICLE_HOT_ZSET_KEY, Duration.ofDays(3));
            log.info("初始化文章ZSet成功，数量：{}", hotArticles.size());
        }catch (Exception e){
            log.error("初始化文章ZSet失败", e);
        }
    }

    @Scheduled(cron = "0 0 4 * * ?")
    public void cleanInvalidArticdlesFromZSet(){
        try {
            Set<String> articleIds = stringRedisTemplate.opsForZSet().range(ARTICLE_HOT_ZSET_KEY, 0, -1);
            if (CollectionUtils.isEmpty(articleIds)) {
                return;
            }
            List<Long> idList = articleIds.stream().map(Long::parseLong).toList();
            List<Article> validArticles = articleMapper.selectList(new LambdaQueryWrapper<Article>()
                    .in(Article::getId, idList)
                    .eq(Article::getArticleType, 3)
                    .eq(Article::getStatus, StatusConstant.ENABLE)
                    .select(Article::getId));

            Set<Long> validArticleIds = validArticles.stream().map(Article::getId)
                    .collect(Collectors.toSet());

            List<String> invalidIds = new ArrayList<>();
            for (String idStr : articleIds) {
                Long articleId = Long.parseLong(idStr);
                if (!validArticleIds.contains(articleId)) {
                    invalidIds.add(idStr);
                }
            }
            if (!invalidIds.isEmpty()){
                stringRedisTemplate.opsForZSet()
                        .remove(ARTICLE_HOT_ZSET_KEY,
                                (Object) invalidIds.toArray(new String[0]));
                log.info("清理无效文章成功，数量：{}", invalidIds.size());
            }
        }catch (Exception e){
            log.error("清理无效文章失败", e);
        }
    }
}
