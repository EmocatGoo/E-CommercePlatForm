package com.yyblcc.ecommerceplatforms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yyblcc.ecommerceplatforms.domain.DTO.*;
import com.yyblcc.ecommerceplatforms.domain.VO.CraftsmanVO;
import com.yyblcc.ecommerceplatforms.domain.VO.ProductListVO;
import com.yyblcc.ecommerceplatforms.domain.VO.ProductVO;
import com.yyblcc.ecommerceplatforms.domain.po.Craftsman;
import com.yyblcc.ecommerceplatforms.domain.DTO.CraftsmanAuthDTO;
import com.yyblcc.ecommerceplatforms.domain.po.PageBean;
import com.yyblcc.ecommerceplatforms.domain.po.Result;
import com.yyblcc.ecommerceplatforms.domain.query.CraftsmanQuery;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface CraftsmanService extends IService<Craftsman> {
    Result<?> saveCraftsman(CraftsmanDTO craftsmanDTO);

    Result<?> updateCraftsmanStatus(Integer status,Long id);

    Result<?> updateCraftsman(CraftsmanDTO craftsmanDTO, HttpServletRequest request);

    Result<?> remove(Long id);

    Result<?> batchRemove(List<Long> ids);

    Result<?> getCraftsman();

    Result<PageBean> page(CraftsmanQuery craftsmanQuery);

    Result login(LoginDTO loginDTO, HttpServletRequest request);

    Result<?> getProfile(Long craftsmanId);

    Result<?> updatePassword(PasswordDTO passwordDTO, HttpServletRequest request);

    Result<?> updateCraftsmanReviewStatus(CraftsmanReviewDTO dto);

    Result resetPassword(Long craftsmanId);

    Result nameSelect(CraftsmanQuery craftsmanQuery);

    Result<?> checkCraftsmanInfo(String username,String phone);

    Result<?> checkEmail(String email);

    Result signUpAuth(CraftsmanAuthDTO craftsmanAuthDTO);

    Result<?> updateAvatar(String avatar);

    Result<PageBean<CraftsmanVO>> frontPage(CraftsmanQuery query);

    Result<List<ProductListVO>> selectReferenceProduct(Long craftsmanId);
}
