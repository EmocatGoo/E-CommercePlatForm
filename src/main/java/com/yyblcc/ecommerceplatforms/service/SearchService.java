package com.yyblcc.ecommerceplatforms.service;

import com.yyblcc.ecommerceplatforms.domain.VO.GlobalSearchResultVO;
import com.yyblcc.ecommerceplatforms.domain.document.CraftsmanDocument;
import com.yyblcc.ecommerceplatforms.domain.document.ProductDocument;
import com.yyblcc.ecommerceplatforms.domain.po.Result;

import java.util.List;

public interface SearchService {
    Result<List<ProductDocument>> productSearch(String keyword, int page, int size);
    Result<List<CraftsmanDocument>> craftsmanSearch(String keyword, int page, int size);
}
