package com.yyblcc.ecommerceplatforms.service;

import com.yyblcc.ecommerceplatforms.domain.document.ProductDocument;
import com.yyblcc.ecommerceplatforms.domain.po.Product;

import java.util.List;

public interface ProductEsService {
    void saveOrUpdate(Product product);
    void deleteById(Long productId);
    List<ProductDocument> search(String keyword, int page, int size);
}
