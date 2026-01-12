package com.yyblcc.ecommerceplatforms.controller;

import com.yyblcc.ecommerceplatforms.domain.VO.GlobalSearchResultVO;
import com.yyblcc.ecommerceplatforms.domain.document.CraftsmanDocument;
import com.yyblcc.ecommerceplatforms.domain.document.ProductDocument;
import com.yyblcc.ecommerceplatforms.domain.po.Result;
import com.yyblcc.ecommerceplatforms.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
@Slf4j
public class SearchController {
    private final SearchService searchService;

    @GetMapping("/craftsman")
    public Result<List<CraftsmanDocument>> craftsmanSearch(@RequestParam String keyword,
                                                           @RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "10") int size) {
        log.info("ES index = {},{}", CraftsmanDocument.class.getAnnotation(Document.class).indexName(), ProductDocument.class.getAnnotation(Document.class).indexName());
        log.info("Search keyword={}", keyword);
        return searchService.craftsmanSearch(keyword, page, size);
    }

    @GetMapping("/product")
    public Result<List<ProductDocument>> productSearch(@RequestParam String keyword,
                                               @RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "10") int size) {
        log.info("ES index = {},{}", CraftsmanDocument.class.getAnnotation(Document.class).indexName(), ProductDocument.class.getAnnotation(Document.class).indexName());
        log.info("Search keyword={}", keyword);
        return searchService.productSearch(keyword, page, size);
    }

}
