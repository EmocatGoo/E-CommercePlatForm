package com.yyblcc.ecommerceplatforms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yyblcc.ecommerceplatforms.domain.DTO.*;
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

    Result signUpWorkShop(Long craftsmanId, WorkShopDTO workShopDTO);

    Result<?> getProfile(Long craftsmanId);

    Result<?> updatePassword(PasswordDTO passwordDTO, HttpServletRequest request);

    Result<?> updateCraftsmanReviewStatus(Integer reviewStatus, Long id);

    Result resetPassword(Long craftsmanId);

    Result nameSelect(CraftsmanQuery craftsmanQuery);

    Result setWorkShopStatus(Long craftsmanId, Integer status);

    Result<?> checkCraftsmanInfo(String username,String phone);

    Result<?> checkEmail(String email);

    Result signUpAuth(CraftsmanAuthDTO craftsmanAuthDTO);
}
