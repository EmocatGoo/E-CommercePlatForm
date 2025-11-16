package com.yyblcc.ecommerceplatforms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yyblcc.ecommerceplatforms.domain.DTO.*;
import com.yyblcc.ecommerceplatforms.domain.VO.ProductDetailVO;
import com.yyblcc.ecommerceplatforms.domain.VO.ProductListVO;
import com.yyblcc.ecommerceplatforms.domain.po.PageBean;
import com.yyblcc.ecommerceplatforms.domain.po.Product;
import com.yyblcc.ecommerceplatforms.domain.po.Result;
import com.yyblcc.ecommerceplatforms.domain.query.ProductQuery;

import java.util.List;

public interface ProductService extends IService<Product> {
    PageBean adminPage(Integer page, Integer pageSize);
    void review(ProductDTO reviewDTO);
    void changeStatus(Long productId, Integer status);
    boolean removeByIds(List<Long> ids);
    void craftsmanSave(ProductDTO dto, Long craftsmanId);
    void craftsmanUpdate(ProductDTO dto, Long craftsmanId);
    Result<PageBean> myPage(Integer page, Integer pageSize, Long craftsmanId);
    void craftsmanOffline(Long productId, Long craftsmanId);
    ProductDetailVO getDetail(Long id);
    PageBean<ProductListVO> frontPage(ProductQuery search);
    List<ProductListVO> recommend(Integer size);
    List<ProductListVO> listByCraftsman(Long craftsmanId);
}
