package com.yyblcc.ecommerceplatforms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yyblcc.ecommerceplatforms.domain.DTO.*;
import com.yyblcc.ecommerceplatforms.domain.VO.ProductListVO;
import com.yyblcc.ecommerceplatforms.domain.VO.ProductStatisticVO;
import com.yyblcc.ecommerceplatforms.domain.po.PageBean;
import com.yyblcc.ecommerceplatforms.domain.po.Product;
import com.yyblcc.ecommerceplatforms.domain.po.Result;
import com.yyblcc.ecommerceplatforms.domain.query.ProductQuery;

import java.util.List;

public interface ProductService extends IService<Product> {
    PageBean adminPage(ProductQuery query);
    void review(ProductDTO reviewDTO);
    void changeStatus(Long productId, Integer status);
    boolean removeByIds(List<Long> ids);
    void craftsmanSave(ProductDTO dto, Long craftsmanId);
    Result<?> craftsmanUpdate(ProductDTO dto, Long craftsmanId);
    Result<PageBean> myPage(ProductQuery query);
    Result<?> craftsmanOffline(Long productId, Long craftsmanId);
    ProductListVO getDetail(Long id);
    PageBean<ProductListVO> frontPage(ProductQuery search);
    Result<List<ProductListVO>> recommend(Integer size);
    List<ProductListVO> listByCraftsman(Long craftsmanId);

    Result favorite(Long productId);

    Result<List<ProductListVO>> getMyFavorite();

    Result like(Long productId);

    Result<List<ProductListVO>> getMyLike(Long productId);


    Result updateProduct(ProductDTO productDTO);

    Result<?> getProductWorkShop(Long productId);

    Result<List<ProductListVO>> referRecommend(Long productId);

    Result<List<ProductStatisticVO>> getHotProduct();
}
