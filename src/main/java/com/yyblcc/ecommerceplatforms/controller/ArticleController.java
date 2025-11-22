package com.yyblcc.ecommerceplatforms.controller;

import com.yyblcc.ecommerceplatforms.domain.DTO.ArticleDTO;
import com.yyblcc.ecommerceplatforms.domain.DTO.ArticleReviewDTO;
import com.yyblcc.ecommerceplatforms.domain.po.Result;
import com.yyblcc.ecommerceplatforms.domain.query.ArticleQuery;
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


}
