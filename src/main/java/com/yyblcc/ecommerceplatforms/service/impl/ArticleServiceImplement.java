package com.yyblcc.ecommerceplatforms.service.impl;


import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yyblcc.ecommerceplatforms.annotation.UpdateBloomFilter;
import com.yyblcc.ecommerceplatforms.constant.StatusConstant;
import com.yyblcc.ecommerceplatforms.domain.DTO.ArticleDTO;
import com.yyblcc.ecommerceplatforms.domain.DTO.ArticleReviewDTO;
import com.yyblcc.ecommerceplatforms.domain.VO.ArticleVO;
import com.yyblcc.ecommerceplatforms.domain.message.ArticleLikeFavoriteMessage;
import com.yyblcc.ecommerceplatforms.domain.message.ProductLikeFavoriteMessage;
import com.yyblcc.ecommerceplatforms.domain.po.*;
import com.yyblcc.ecommerceplatforms.domain.query.ArticleQuery;
import com.yyblcc.ecommerceplatforms.domain.query.PageQuery;
import com.yyblcc.ecommerceplatforms.exception.BusinessException;
import com.yyblcc.ecommerceplatforms.mapper.ArticleFavoriteMapper;
import com.yyblcc.ecommerceplatforms.mapper.ArticleLikeMapper;
import com.yyblcc.ecommerceplatforms.mapper.ArticleMapper;
import com.yyblcc.ecommerceplatforms.service.ArticleService;
import com.yyblcc.ecommerceplatforms.util.StpKit;
import com.yyblcc.ecommerceplatforms.util.context.AuthContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArticleServiceImplement extends ServiceImpl<ArticleMapper, Article> implements ArticleService {
    private final ArticleMapper articleMapper;
    private final StringRedisTemplate stringRedisTemplate;

    private final RocketMQTemplate rocketMQTemplate;
    private static final DefaultRedisScript<Long> LIKE_SCRIPT;
    static {
        LIKE_SCRIPT = new DefaultRedisScript<>();
        LIKE_SCRIPT.setLocation(new ClassPathResource("like.lua"));
        LIKE_SCRIPT.setResultType(Long.class);
    }

    private final ArticleLikeMapper articleLikeMapper;
    private final ArticleFavoriteMapper articleFavoriteMapper;

    @Override
    @UpdateBloomFilter
    public Result createArticle(ArticleDTO articleDTO) {
        try{
            stringRedisTemplate.keys("article:*").forEach(stringRedisTemplate::delete);
        } catch (RuntimeException e) {
            log.warn("无缓存信息");
        }
        if (articleDTO == null) {
            throw new BusinessException("无文章信息");
        }
        Long authorId = AuthContext.getUserId();
        Article article = new Article();
        BeanUtils.copyProperties(articleDTO, article);
        article.setAuthorId(authorId);
        article.setStatus(0);
        article.setReviewStatus(0);
        article.setCreateTime(LocalDateTime.now());
        article.setUpdateTime(LocalDateTime.now());
        save(article);
        stringRedisTemplate.keys("article:*").forEach(stringRedisTemplate::delete);
        return Result.success("发布成功，请等待管理员审核");
    }

    @Override
    public Result deleteArticle(Long id) {
        Article article = query().eq("id", id).one();
        if (article == null) {
            throw new BusinessException("未找到文章信息");
        }
        try{
            stringRedisTemplate.keys("article:*").forEach(stringRedisTemplate::delete);
        } catch (RuntimeException e) {
            log.warn("无缓存信息");
        }
        removeById(id);
        return Result.success("删除成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result batchDeleteArticle(List<Long> ids) {
        if (CollUtil.isEmpty(ids)){
            throw new BusinessException("未找到对应文章信息");
        }
        try{
            stringRedisTemplate.keys("article:*").forEach(stringRedisTemplate::delete);
        } catch (RuntimeException e) {
            log.warn("无缓存信息");
        }
        removeBatchByIds(ids);
        return Result.success("批量删除成功");
    }

    @Override
    //编辑文章
    public Result updateArticle(ArticleDTO articleDTO) {
        try{
            stringRedisTemplate.keys("article:*").forEach(stringRedisTemplate::delete);
        } catch (RuntimeException e) {
            log.warn("无缓存信息");
        }
        if (articleDTO == null) {
            throw new BusinessException("无文章信息");
        }
        // 根据ID查询文章，检查当前状态
        Article existingArticle = query().eq("id", articleDTO.getId()).one();
        if (existingArticle == null) {
            throw new BusinessException("未找到文章信息");
        }
        // 检查文章是否为上架状态（status为1）
        if (existingArticle.getStatus().equals(1)) {
            return Result.error("文章当前为上架状态，请先下架后再修改");
        }
        
        Article article = new Article();
        BeanUtils.copyProperties(articleDTO, article);
        article.setStatus(0);
        article.setReviewStatus(0);
        article.setUpdateTime(LocalDateTime.now());
        try{
            stringRedisTemplate.keys("article:*").forEach(stringRedisTemplate::delete);
        } catch (RuntimeException e) {
            log.warn("无缓存信息");
        }
        updateById(article);
        return Result.success("修改成功，请等待管理员审核");
    }

    @Override
    public Result pageArticle(ArticleQuery articleQuery) {
        Page<Article> articlePage = articleMapper.selectPage(new Page<>(articleQuery.getPage(),articleQuery.getPageSize()),
                new LambdaQueryWrapper<Article>()
                        .like(articleQuery.getArticleTitle() !=null,Article::getArticleTitle,articleQuery.getArticleTitle())
                        .eq(articleQuery.getArticleReviewStatus() !=null,Article::getReviewStatus,articleQuery.getArticleReviewStatus())
                        .orderByAsc(Article::getCreateTime));
        List<ArticleVO> voList = articlePage.getRecords().stream().map(this::convertArticleVO).toList();
        PageBean<ArticleVO> pageBean = new PageBean<>(articlePage.getTotal(),voList);
        return Result.success(pageBean);
    }

    @Override
    public Result reviewArticle(ArticleReviewDTO articleReviewDTO) {
        try{
            stringRedisTemplate.keys("article:*").forEach(stringRedisTemplate::delete);
        } catch (RuntimeException e) {
            log.warn("无缓存信息");
        }
        if (articleReviewDTO == null) {
            throw new BusinessException("未找到文章信息");
        }
        Long adminId = AuthContext.getUserId();
        Long articleId = articleReviewDTO.getId();
        Long authorId = articleReviewDTO.getAuthorId();
        Integer authorType = articleReviewDTO.getAuthorType();

        if (authorType.equals(1)){
            if (adminId.equals(authorId)){
                return Result.error("不能审核自己发布的文章");
            }
        }

        Article article = query().eq("id", articleId).one();

        if (article == null){
            return Result.error("未找到文章信息！");
        }

        if (articleReviewDTO.getStatus().equals(1)){
            article.setReviewStatus(articleReviewDTO.getStatus());
            if (updateById(article)){
                stringRedisTemplate.keys("article:*").forEach(stringRedisTemplate::delete);
                return Result.success("审核完成");
            }
        } else if (articleReviewDTO.getStatus().equals(2)) {
            article.setReviewStatus(articleReviewDTO.getStatus());
            article.setRefuseReason(articleReviewDTO.getRefuseReason());
            if (updateById(article)){
                stringRedisTemplate.keys("article:*").forEach(stringRedisTemplate::delete);
                return Result.success("审核完成");
            }
        }
        return Result.error("审核失败");
    }

    @Override
    public Result getArticleDetail(Long id) {
        if (id == null) {
            return Result.error("未找到文章信息");
        }
        Article article = query().eq("id", id).one();
        if (article == null){
            return Result.error("未找到文章信息或文章被删除");
        }
        ArticleVO articleVO = convertArticleVO(article);
        Long userId = StpKit.USER.getLoginIdAsLong();
        UserArticleLike existingUserLike = articleLikeMapper.selectOne(
                new LambdaQueryWrapper<UserArticleLike>()
                        .eq(UserArticleLike::getUserId, userId)
                        .eq(UserArticleLike::getArticleId, id));
         if (existingUserLike != null) {
             articleVO.setUserLikeStatus(existingUserLike.getStatus().equals(1));
        } else {
            articleVO.setUserLikeStatus(false);
        }
        UserArticleFavorite existingUserFavorite = articleFavoriteMapper.selectOne(
                new LambdaQueryWrapper<UserArticleFavorite>()
                        .eq(UserArticleFavorite::getUserId, userId)
                        .eq(UserArticleFavorite::getArticleId, id));
        if (existingUserFavorite != null) {
            articleVO.setUserFavoriteStatus(existingUserFavorite.getStatus().equals(1));
        } else {
            articleVO.setUserFavoriteStatus(false);
        }
        return Result.success(articleVO);
    }

    @Override
    public Result updateCover(Long id, String cover) {
        try{
            stringRedisTemplate.keys("article:*").forEach(stringRedisTemplate::delete);
        } catch (RuntimeException e) {
            log.warn("无缓存信息");
        }
        Article dbArticle = query().eq("id", id).one();
        if (dbArticle == null){
            throw new BusinessException("未找到文章信息");
        }
        dbArticle.setCoverImage(cover);
        if (updateById(dbArticle)){
            try{
                stringRedisTemplate.keys("article:*").forEach(stringRedisTemplate::delete);
            } catch (RuntimeException e) {
                log.warn("无缓存信息");
            }
            return Result.success("修改成功");
        }
        return Result.error("修改失败");
    }

    @Override
    public Result pageCraftsmanArticle(PageQuery query) {
        Long authorId = StpKit.CRAFTSMAN.getLoginIdAsLong();
        Page<Article> articlePage = articleMapper.selectPage(new Page<>(query.getPage(),query.getPageSize()),
                new LambdaQueryWrapper<Article>()
                        .eq(Article::getAuthorType,2)
                        .eq(Article::getAuthorId,authorId)
                        .like(query.getKeyword() !=null,Article::getArticleTitle,query.getKeyword())
                        .orderByAsc(Article::getCreateTime));
        List<ArticleVO> voList = articlePage.getRecords().stream().map(this::convertArticleVO).toList();
        PageBean<ArticleVO> pageBean = new PageBean<>(articlePage.getTotal(),voList);
        return Result.success(pageBean);
    }

    @Override
    public Result setArticleStatus(ArticleReviewDTO dto) {
        try{
            stringRedisTemplate.keys("article:*").forEach(stringRedisTemplate::delete);
        } catch (RuntimeException e) {
            log.warn("无缓存信息");
        }
        Long articleId = dto.getId();
        Integer status = dto.getStatus();
        Article article = query().eq("id", articleId).one();
        if (article == null){
            return Result.error("未找到文章信息");
        }
        if (article.getReviewStatus().equals(StatusConstant.ENABLE)){
            if (status.equals(StatusConstant.ENABLE)){
                article.setStatus(status);
                if (updateById(article)) {
                    try{
                        stringRedisTemplate.keys("article:*").forEach(stringRedisTemplate::delete);
                    } catch (RuntimeException e) {
                        log.warn("无缓存信息");
                    }
                    return Result.success("已发布");
                }
            }else if(status.equals(StatusConstant.DISABLE)){
                article.setStatus(status);
                if (updateById(article)) {
                    try{
                        stringRedisTemplate.keys("article:*").forEach(stringRedisTemplate::delete);
                    } catch (RuntimeException e) {
                        log.warn("无缓存信息");
                    }
                    return Result.success("已下架");
                }
            }
        } else if (article.getReviewStatus().equals(StatusConstant.DISABLE)) {
            return Result.error("文章正在审核中");
        }else{
            return Result.error("文章审核未通过");
        }
        return null;
    }


    @Override
    public Result<List<ArticleVO>> getKnowledgeArticleByViewCount() {
        String key = "article:heritage:hot";
        try{
            String cacheStr = stringRedisTemplate.opsForValue().get(key);
            if (cacheStr !=null){
                if (cacheStr.isEmpty()){
                    return Result.success();
                }
                List<ArticleVO> articleList = JSON.parseObject(cacheStr, new TypeReference<List<ArticleVO>>() {});
                return Result.success(articleList);
            }
        } catch (RuntimeException e) {
            log.warn("获取缓存失败: ");
        }
        List<Article> knowledgeArticles = articleMapper.selectList(new LambdaQueryWrapper<Article>()
                        .eq(Article::getArticleType,3)
                        .eq(Article::getStatus,StatusConstant.ENABLE)
                        .orderByDesc(Article::getViewCount)
                        .last("limit 6"));
        List<ArticleVO> voList = knowledgeArticles.stream().map(this::convertArticleVO).toList();
        try {
            stringRedisTemplate.opsForValue().set(key, JSON.toJSONString(voList), Duration.ofMinutes(10 + new Random().nextInt(0,5)));
        } catch (RuntimeException e) {
            log.warn("设置缓存失败: ");
        }
        return Result.success(voList);
    }

    @Override
    public Result<PageBean<ArticleVO>> getAllHeritage(ArticleQuery query) {
        boolean isCondition = query.getArticleTitle() != null || query.getCategoryId() != null;
        String key = "article:heritage:page:"+query.getPage()+":"+query.getPageSize();
        if (!isCondition){
            try{
                String cacheStr = stringRedisTemplate.opsForValue().get(key);
                if (cacheStr != null){
                    if (cacheStr.isEmpty()){
                        return Result.success();
                    }
                    PageBean<ArticleVO> cachePageBean = JSON.parseObject(cacheStr, new TypeReference<PageBean<ArticleVO>>() {});
                    return Result.success(cachePageBean);
                }
            } catch (RuntimeException e) {
                throw new RuntimeException(e);
            }
        }
        Page<Article> articlePage = articleMapper.selectPage(new Page<>(query.getPage(), query.getPageSize()),
                new LambdaQueryWrapper<Article>()
                        .eq(Article::getArticleType, 3)
                        .eq(Article::getStatus, StatusConstant.ENABLE)
                        .eq(query.getCategoryId() != null,Article::getCategoryId,query.getCategoryId())
                        .like(query.getArticleTitle() != null,Article::getArticleTitle,query.getArticleTitle())
                        .orderByDesc(Article::getViewCount));
        List<ArticleVO> voList = articlePage.getRecords().stream().map(this::convertArticleVO).toList();
        PageBean<ArticleVO> pageBean = new PageBean<>(articlePage.getTotal(), voList);
        if (!isCondition){
            stringRedisTemplate.opsForValue().set(key, JSON.toJSONString(pageBean), Duration.ofMinutes(10 + new Random().nextInt(0,5)));
        }
        return Result.success(pageBean);
    }

    @Override
    public void viewArticle(Long articleId) {
        Long userId = StpKit.USER.getLoginIdAsLong();
        try {
            String today = LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE);
            String viewKey = "article:view:" + articleId + ":" + today;

            Boolean isMember = stringRedisTemplate.opsForSet().isMember(viewKey, userId.toString());
            if (Boolean.TRUE.equals(isMember)) {
                log.info("用户{}今天已查看过文章{}", userId, articleId);
                return;
            }
            stringRedisTemplate.opsForSet().add(viewKey, userId.toString());
            long secondsUntilMidnight  = getSecondsUntilTomorrowMidnight();
            stringRedisTemplate.expire(viewKey,secondsUntilMidnight, TimeUnit.SECONDS);
            boolean success = update(new LambdaUpdateWrapper<Article>()
                    .eq(Article::getId, articleId)
                    .setSql("view_count = view_count + 1"));
            if (success) {
                log.info("文章id为：{} 文章浏览量+1", articleId);
            }
        }catch (Exception e){
            log.warn("记录文章{}浏览量发生异常，用户{}：{}", articleId, userId, e.getMessage());
        }
    }

    private long getSecondsUntilTomorrowMidnight(){
         LocalDateTime now = LocalDateTime.now();
         LocalDateTime tomorrowMidnight = now.toLocalDate().plusDays(1).atStartOfDay();
         return Duration.between(now, tomorrowMidnight).getSeconds() + 3600;
    }

    @Override
    public Result<String> toggleArticleLike(Long articleId) {
        Long userId = AuthContext.getUserId();
        Article article = query().eq("id", articleId).one();
        if (article.getStatus().equals(StatusConstant.DISABLE) || article.getReviewStatus().equals(StatusConstant.DISABLE)){
            return Result.error("文章已下架");
        }
        String requestId = userId + "_" + articleId + "_LIKE_" + System.currentTimeMillis();

        ArticleLikeFavoriteMessage articleLikeFavoriteMessage = ArticleLikeFavoriteMessage.builder()
                .articleId(articleId)
                .userId(userId)
                .requestId(requestId)
                .type("LIKE")
                .build();

        boolean currentLike = isLiked(userId,articleId);
        articleLikeFavoriteMessage.setAction(currentLike ? -1 : 1);
        rocketMQTemplate.asyncSend("article-like-favorite-topic", articleLikeFavoriteMessage, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("消息发送成功");
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("消息发送失败");
            }
        });
        return Result.success(currentLike ? "取消点赞" : "已点赞");
    }

    private boolean isLiked(Long userId,Long articleId){
        UserArticleLike like = articleLikeMapper.selectOne(new LambdaQueryWrapper<UserArticleLike>()
                .eq(UserArticleLike::getUserId, userId)
                .eq(UserArticleLike::getArticleId, articleId));
        if (like == null){
            return false;
        }else{
            return !like.getStatus().equals(0);
        }
    }

    @Override
    public Result<String> toggleArticleFavorite(Long articleId) {
        Long userId = AuthContext.getUserId();
        Article article = query().eq("id", articleId).one();
        if (article.getStatus().equals(StatusConstant.DISABLE) || article.getReviewStatus().equals(StatusConstant.DISABLE)){
            return Result.error("文章已下架");
        }
        String requestId = userId + "_" + articleId + "_FAVORITE_" + System.currentTimeMillis();

        ArticleLikeFavoriteMessage articleLikeFavoriteMessage = ArticleLikeFavoriteMessage.builder()
                .articleId(articleId)
                .userId(userId)
                .requestId(requestId)
                .type("FAVORITE")
                .build();

        boolean currentFavorite = isFavorited(userId,articleId);
        articleLikeFavoriteMessage.setAction(currentFavorite ? -1 : 1);
        rocketMQTemplate.asyncSend("article-like-favorite-topic", articleLikeFavoriteMessage, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("消息发送成功");
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("消息发送失败");
            }
        });
        return Result.success(currentFavorite ? "取消收藏" : "已收藏");
    }

    @Override
    public Result<PageBean<ArticleVO>> selectAllArticles(ArticleQuery query) {
        List<ArticleVO> list = articleMapper.selectPage(new Page<>(query.getPage(), query.getPageSize()), new LambdaQueryWrapper<Article>()
                .eq(Article::getStatus, StatusConstant.ENABLE)
                .eq(query.getArticleType() != null, Article::getArticleType, query.getArticleType())
                .like(query.getArticleTitle() != null, Article::getArticleTitle, query.getArticleTitle())
                .ne(query.getArticleType() == null ,Article::getArticleType, 3)
                .orderByDesc(Article::getViewCount))
                .getRecords()
                .stream()
                .map(this::convertArticleVO)
                .toList();

        return Result.success( new PageBean<>((long)list.size(), list));
    }

    private boolean isFavorited(Long userId,Long articleId){
        UserArticleFavorite favorite = articleFavoriteMapper.selectOne(new LambdaQueryWrapper<UserArticleFavorite>()
                .eq(UserArticleFavorite::getUserId, userId)
                .eq(UserArticleFavorite::getArticleId, articleId));
        if (favorite == null){
            return false;
        }else{
            return !favorite.getStatus().equals(0);
        }
    }

    private ArticleVO convertArticleVO(Article article){
        ArticleVO articleVO = new ArticleVO();
        BeanUtils.copyProperties(article,articleVO);
        return articleVO;
    }


}
