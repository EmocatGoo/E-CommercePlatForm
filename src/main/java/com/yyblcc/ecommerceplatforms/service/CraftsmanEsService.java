package com.yyblcc.ecommerceplatforms.service;

import com.yyblcc.ecommerceplatforms.domain.document.CraftsmanDocument;
import com.yyblcc.ecommerceplatforms.domain.po.Craftsman;

import java.util.List;

public interface CraftsmanEsService {

    /** 新增或更新索引 */
    void saveOrUpdate(Craftsman craftsman);

    /** 从索引中删除 */
    void deleteById(Long craftsmanId);

    /** 关键词搜索 */
    List<CraftsmanDocument> search(String keyword, int page, int size);
}
