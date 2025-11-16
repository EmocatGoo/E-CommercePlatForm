package com.yyblcc.ecommerceplatforms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yyblcc.ecommerceplatforms.domain.DTO.CraftsmanAuthDTO;
import com.yyblcc.ecommerceplatforms.domain.po.Result;

public interface CraftsmanAuthService extends IService<CraftsmanAuthDTO> {
    Result pageReview(Integer page, Integer pageSize);
}
