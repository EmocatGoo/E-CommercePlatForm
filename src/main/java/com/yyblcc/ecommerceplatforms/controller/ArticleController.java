package com.yyblcc.ecommerceplatforms.controller;

import com.yyblcc.ecommerceplatforms.domain.DTO.ArticleDTO;
import com.yyblcc.ecommerceplatforms.domain.DTO.ArticleReviewDTO;
import com.yyblcc.ecommerceplatforms.domain.VO.ArticleVO;
import com.yyblcc.ecommerceplatforms.domain.po.PageBean;
import com.yyblcc.ecommerceplatforms.domain.po.Result;
import com.yyblcc.ecommerceplatforms.domain.query.ArticleQuery;
import com.yyblcc.ecommerceplatforms.domain.query.PageQuery;
import com.yyblcc.ecommerceplatforms.service.ArticleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/article")
@RequiredArgsConstructor
@Slf4j
public class ArticleController {
    private final ArticleService articleService;

    @PostMapping
    public Result create(@RequestBody ArticleDTO articleDTO) {
        log.info("新增文章,{}", articleDTO);
        return articleService.createArticle(articleDTO);
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        log.info("删除文章的id为：{}",id);
        return articleService.deleteArticle(id);
    }

    @DeleteMapping("/batch")
    public Result delete(@RequestParam List<Long> ids) {
        log.info("批量删除文章：{}",ids);
        return articleService.batchDeleteArticle(ids);
    }

    @PutMapping
    public Result update(@RequestBody ArticleDTO articleDTO) {
        log.info("修改文章:{}",articleDTO);
        return articleService.updateArticle(articleDTO);
    }

    @GetMapping("/page")
    public Result page(ArticleQuery articleQuery) {
        log.info("分页查询文章信息，第{}页，页大小为：{}",articleQuery.getPage(),articleQuery.getPageSize());
        return articleService.pageArticle(articleQuery);
    }

    @GetMapping("/craftsman-article")
    public Result pageCraftsmanArticle(PageQuery query) {
        log.info("分页查询作者文章信息，第{}页，页大小为：{}",query.getPage(),query.getPageSize());
        return articleService.pageCraftsmanArticle(query);
    }

    @PutMapping("/review")
    public Result review(@RequestBody ArticleReviewDTO articleReviewDTO) {
        log.info("审核文章：{}",articleReviewDTO);
        return articleService.reviewArticle(articleReviewDTO);
    }

    @GetMapping("/{id}")
    public Result getArticleDetail(@PathVariable Long id) {
        log.info("获取文章具体信息");
        return articleService.getArticleDetail(id);
    }

    @PutMapping("/setArticleStatus")
    public Result setArticleStatus(@RequestBody ArticleReviewDTO dto){
        log.info("上架/下架文章:{}",dto);
        return articleService.setArticleStatus(dto);
    }

    @PutMapping("/updateCover")
    public Result updateCover(@RequestParam Long id, @RequestParam String cover) {
        log.info("更新文章封面");
        return articleService.updateCover(id, cover);
    }

    @GetMapping("/selectByViewCount")
    public Result<List<ArticleVO>> getKnowledgeArticleByViewCount() {
        log.info("获取热门非遗文化文章");
        return articleService.getKnowledgeArticleByViewCount();
    }

    @GetMapping("/selectHeritage")
    public Result<PageBean<ArticleVO>> getAllHeritage(ArticleQuery query){
        log.info("获取所有文化遗产文章信息");
        return articleService.getAllHeritage(query);
    }

    @PostMapping("/view")
    public void viewArticle(@RequestParam Long articleId){
        log.info("文章id:{}浏览量+1",articleId);
        articleService.viewArticle(articleId);
    }

    @PostMapping("/like")
    public Result<String> toggleArticleLike(@RequestParam Long articleId){
        log.info("点赞请求");
        return articleService.toggleArticleLike(articleId);
    }

    @PostMapping("/favorite")
    public Result<String> toggleArticleFavorite(@RequestParam Long articleId){
        log.info("收藏请求");
        return articleService.toggleArticleFavorite(articleId);
    }

    @GetMapping("/selectAllArticles")
     public Result<PageBean<ArticleVO>> selectAllArticles(ArticleQuery query){
        log.info("获取所有文章");
        return articleService.selectAllArticles(query);
    }

}
