package com.yyblcc.ecommerceplatforms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yyblcc.ecommerceplatforms.domain.DTO.AddCartDTO;
import com.yyblcc.ecommerceplatforms.domain.VO.CartVO;
import com.yyblcc.ecommerceplatforms.domain.po.Cart;
import com.yyblcc.ecommerceplatforms.domain.po.Result;

import java.util.List;

public interface CartService extends IService<Cart> {
    void addItem(Long userId, AddCartDTO dto);

    void toggleCheck(Long userId, Long productId, Boolean checked);

    CartVO getMyCart(Long userId);

    Result<?> batchdelete(List<Long> productIds);

    Result<?> delete(Long productId);
}
