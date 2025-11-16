package com.yyblcc.ecommerceplatforms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yyblcc.ecommerceplatforms.domain.po.Result;
import com.yyblcc.ecommerceplatforms.domain.po.WorkShop;

public interface WorkShopService extends IService<WorkShop> {
    Result reviewWorkshop(Long workshopId, Integer status);

    Result banWorkshop(Long workshopId);

    Result pageWorkShop(Integer page, Integer pageSize);

    Result getWorkShopByCraftsmanId(Long craftsmanId);

    Result selectWorkShopName(String workshopName);
}
