package com.yyblcc.ecommerceplatforms.service.impl;

import com.yyblcc.ecommerceplatforms.domain.document.CraftsmanDocument;
import com.yyblcc.ecommerceplatforms.domain.document.ProductDocument;
import com.yyblcc.ecommerceplatforms.domain.po.Result;
import com.yyblcc.ecommerceplatforms.service.CraftsmanEsService;
import com.yyblcc.ecommerceplatforms.service.ProductEsService;
import com.yyblcc.ecommerceplatforms.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchServiceImplement implements SearchService {
    private final CraftsmanEsService craftsmanEsService;
    private final ProductEsService productEsService;

    @Override
    public Result<List<ProductDocument>> productSearch(String keyword, int page, int size) {
        List<ProductDocument> products = productEsService.search(keyword, page, size);
        return Result.success(products);
    }

    @Override
    public Result<List<CraftsmanDocument>> craftsmanSearch(String keyword, int page, int size) {
        List<CraftsmanDocument> craftsmen = craftsmanEsService.search(keyword, page, size);
        return Result.success(craftsmen);
    }


}
