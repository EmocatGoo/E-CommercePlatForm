package com.yyblcc.ecommerceplatforms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yyblcc.ecommerceplatforms.domain.DTO.WorkShopDTO;
import com.yyblcc.ecommerceplatforms.domain.VO.WorkShopVO;
import com.yyblcc.ecommerceplatforms.domain.po.PageBean;
import com.yyblcc.ecommerceplatforms.domain.po.Result;
import com.yyblcc.ecommerceplatforms.domain.po.WorkShop;
import com.yyblcc.ecommerceplatforms.domain.query.PageQuery;

import java.util.List;

public interface WorkShopService extends IService<WorkShop> {
    Result reviewWorkshop(Long workshopId, Integer status);

    Result adminSetWorkShopStatus(Long workshopId, Integer status);

    Result pageWorkShop(PageQuery query);

    Result getWorkShopByCraftsmanId(Long craftsmanId);

    Result selectWorkShopName(String workshopName);

    Result signUpWorkShop(Long craftsmanId, WorkShopDTO workShopDTO);

    Result getWorkShopDetail(Long id);

    Result visitWorkShop(Long id);

    Result setWorkShopStatus(Long craftsmanId, Integer status);

    Result updateWorkShop(WorkShopDTO workShopDTO);

    Result collectWorkShop(Long workShopId);

    Result getWorkShopStatus(Long craftsmanId);

    void viewWorkShop(Long id);

    Result<PageBean<WorkShopVO>> frontPage(PageQuery query);

    Result<Boolean> checkCollect(Long workShopId);

    Result<String> updateMasterPieces(List<String> masterPieces);
}
