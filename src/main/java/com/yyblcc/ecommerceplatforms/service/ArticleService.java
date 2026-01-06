package com.yyblcc.ecommerceplatforms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yyblcc.ecommerceplatforms.domain.DTO.ArticleDTO;
import com.yyblcc.ecommerceplatforms.domain.DTO.ArticleReviewDTO;
import com.yyblcc.ecommerceplatforms.domain.VO.ArticleVO;
import com.yyblcc.ecommerceplatforms.domain.po.Article;
import com.yyblcc.ecommerceplatforms.domain.po.PageBean;
import com.yyblcc.ecommerceplatforms.domain.po.Result;
import com.yyblcc.ecommerceplatforms.domain.query.ArticleQuery;
import com.yyblcc.ecommerceplatforms.domain.query.PageQuery;

import java.util.List;

public interface ArticleService extends IService<Article> {
    Result createArticle(ArticleDTO articleDTO);

    Result deleteArticle(Long id);

    Result batchDeleteArticle(List<Long> ids);

    Result updateArticle(ArticleDTO articleDTO);

    Result pageArticle(ArticleQuery articleQuery);

    Result reviewArticle(ArticleReviewDTO articleReviewDTO);

    Result getArticleDetail(Long id);

    Result updateCover(Long id, String cover);

    Result pageCraftsmanArticle(PageQuery query);

    Result setArticleStatus(ArticleReviewDTO dto);

    Result<List<ArticleVO>> getKnowledgeArticleByViewCount();

    Result<PageBean<ArticleVO>> getAllHeritage(ArticleQuery query);

    void viewArticle(Long articleId);

    Result<String> toggleArticleLike(Long articleId);

    Result<String> toggleArticleFavorite(Long articleId);

    Result<PageBean<ArticleVO>> selectAllArticles(ArticleQuery query);
}
