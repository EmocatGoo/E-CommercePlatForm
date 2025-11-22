package com.yyblcc.ecommerceplatforms.service.impl;


import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yyblcc.ecommerceplatforms.annotation.UpdateBloomFilter;
import com.yyblcc.ecommerceplatforms.domain.DTO.ArticleDTO;
import com.yyblcc.ecommerceplatforms.domain.DTO.ArticleReviewDTO;
import com.yyblcc.ecommerceplatforms.domain.VO.ArticleVO;
import com.yyblcc.ecommerceplatforms.domain.po.Article;
import com.yyblcc.ecommerceplatforms.domain.po.PageBean;
import com.yyblcc.ecommerceplatforms.domain.po.Result;
import com.yyblcc.ecommerceplatforms.domain.query.ArticleQuery;
import com.yyblcc.ecommerceplatforms.exception.BusinessException;
import com.yyblcc.ecommerceplatforms.mapper.ArticleMapper;
import com.yyblcc.ecommerceplatforms.service.ArticleService;
import com.yyblcc.ecommerceplatforms.util.context.AuthContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ArticleServiceImplement extends ServiceImpl<ArticleMapper, Article> implements ArticleService {
    private final ArticleMapper articleMapper;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    @UpdateBloomFilter
    public Result createArticle(ArticleDTO articleDTO) {
        if (articleDTO == null) {
            throw new BusinessException("无文章信息");
        }
        Article article = new Article();
        BeanUtils.copyProperties(articleDTO, article);
        article.setStatus(0);
        article.setCreateTime(LocalDateTime.now());
        article.setUpdateTime(LocalDateTime.now());
        save(article);
        return Result.success("发布成功，请等待管理员审核");
    }

    @Override
    public Result deleteArticle(Long id) {
        Article article = query().eq("id", id).one();
        if (article == null) {
            throw new BusinessException("未找到文章信息");
        }
        removeById(id);
        stringRedisTemplate.keys("article:page:*").forEach(stringRedisTemplate::delete);
        return Result.success("删除成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result batchDeleteArticle(List<Long> ids) {
        if (CollUtil.isEmpty(ids)){
            throw new BusinessException("未找到对应文章信息");
        }
        removeBatchByIds(ids);
        stringRedisTemplate.keys("article:page:*").forEach(stringRedisTemplate::delete);
        return Result.success("批量删除成功");
    }

    @Override
    public Result updateArticle(ArticleDTO articleDTO) {
        if (articleDTO == null) {
            throw new BusinessException("无文章信息");
        }
        Article article = new Article();
        BeanUtils.copyProperties(articleDTO, article);
        article.setStatus(0);
        article.setUpdateTime(LocalDateTime.now());
        updateById(article);
        stringRedisTemplate.keys("article:page:*").forEach(stringRedisTemplate::delete);
        return Result.success("修改成功，请等待管理员审核");
    }

    @Override
    public Result pageArticle(ArticleQuery articleQuery) {
        String key = "article:page:" + articleQuery.getPage() + ":" + articleQuery.getPageSize();
        try{
            String cacheStr = stringRedisTemplate.opsForValue().get(key);
            if (cacheStr != null) {
                if (cacheStr.isEmpty()) {
                    return Result.success();
                }
                return Result.success(JSON.parseObject(cacheStr, PageBean.class));
            }
        }catch (Exception e){
            log.error(e.getMessage(),e);
        }
        Page<Article> articlePage = articleMapper.selectPage(new Page<>(articleQuery.getPage(),articleQuery.getPageSize()),
                new LambdaQueryWrapper<Article>()
                        .like(articleQuery.getArticleTitle()!=null,Article::getArticleTitle,articleQuery.getArticleTitle())
                        .orderByDesc(Article::getCreateTime)
                        .last("FOR UPDATE"));
        List<ArticleVO> voList = articlePage.getRecords().stream().map(this::convertArticleVO).toList();
        PageBean<ArticleVO> pageBean = new PageBean<>(articlePage.getTotal(),voList);
        stringRedisTemplate.opsForValue().set(key, JSON.toJSONString(pageBean), Duration.ofMinutes(15));
        return Result.success(pageBean);
    }

    @Override
    public Result reviewArticle(ArticleReviewDTO articleReviewDTO) {
        if (articleReviewDTO == null) {
            throw new BusinessException("未找到文章信息");
        }
        Long adminId = AuthContext.getUserId();
        Long articleId = articleReviewDTO.getId();
        Long authorId = articleReviewDTO.getAuthorId();
        Integer authorType = articleReviewDTO.getAuthorType();

        if (authorType.equals(1)){
            if (adminId.equals(authorId)){
                throw new BusinessException("不能审核自己的文章");
            }
        }

        Article article = query().eq("id", articleId).one();

        if (article == null){
            throw new BusinessException("未找到文章信息");
        }
        article.setStatus(articleReviewDTO.getStatus());
        updateById(article);
        return Result.success("审核完成");
    }

    @Override
    public Result getArticleDetail(Long id) {
        if (id == null) {
            throw new BusinessException("无相关文章信息");
        }
        Article article = query().eq("id", id).one();
        if (article == null){
            throw new BusinessException("未找到文章或文章被删除");
        }
        ArticleVO articleVO = convertArticleVO(article);
        return Result.success(articleVO);
    }

    private ArticleVO convertArticleVO(Article article){
        ArticleVO articleVO = new ArticleVO();
        BeanUtils.copyProperties(article,articleVO);
        return articleVO;
    }


}
