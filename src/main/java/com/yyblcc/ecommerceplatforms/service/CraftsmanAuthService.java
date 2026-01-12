package com.yyblcc.ecommerceplatforms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yyblcc.ecommerceplatforms.domain.DTO.CraftsmanAuthDTO;
import com.yyblcc.ecommerceplatforms.domain.po.CraftsmanAuth;
import com.yyblcc.ecommerceplatforms.domain.po.PageBean;
import com.yyblcc.ecommerceplatforms.domain.po.Result;
import com.yyblcc.ecommerceplatforms.domain.query.CraftsmanAuthQuery;
import com.yyblcc.ecommerceplatforms.domain.query.PageQuery;

public interface CraftsmanAuthService extends IService<CraftsmanAuth> {

    Result<PageBean<CraftsmanAuth>> pageReview(CraftsmanAuthQuery query);
}
