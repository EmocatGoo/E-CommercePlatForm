package com.yyblcc.ecommerceplatforms.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yyblcc.ecommerceplatforms.domain.DTO.CraftsmanAuthDTO;
import com.yyblcc.ecommerceplatforms.domain.po.PageBean;
import com.yyblcc.ecommerceplatforms.domain.po.Result;
import com.yyblcc.ecommerceplatforms.mapper.CraftsmanAuthMapper;
import com.yyblcc.ecommerceplatforms.service.CraftsmanAuthService;
import org.springframework.stereotype.Service;

@Service
public class CraftsmanAuthServiceImplement extends ServiceImpl<CraftsmanAuthMapper, CraftsmanAuthDTO> implements CraftsmanAuthService {
    private final CraftsmanAuthMapper craftsmanAuthMapper;

    public CraftsmanAuthServiceImplement(CraftsmanAuthMapper craftsmanAuthMapper) {
        this.craftsmanAuthMapper = craftsmanAuthMapper;
    }

    @Override
    public Result pageReview(Integer page, Integer pageSize) {
        Page<CraftsmanAuthDTO> craftsmanAuthPage = craftsmanAuthMapper.selectPage(new Page<>(page, pageSize), null);
        PageBean<CraftsmanAuthDTO> pageBean = new PageBean<>(craftsmanAuthPage.getTotal(),craftsmanAuthPage.getRecords());
        return Result.success(pageBean);
    }



}
