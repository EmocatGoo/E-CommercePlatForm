package com.yyblcc.ecommerceplatforms.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yyblcc.ecommerceplatforms.domain.DTO.CraftsmanAuthDTO;
import com.yyblcc.ecommerceplatforms.domain.po.CraftsmanAuth;
import com.yyblcc.ecommerceplatforms.domain.po.PageBean;
import com.yyblcc.ecommerceplatforms.domain.po.Result;
import com.yyblcc.ecommerceplatforms.mapper.CraftsmanAuthMapper;
import com.yyblcc.ecommerceplatforms.service.CraftsmanAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CraftsmanAuthServiceImplement extends ServiceImpl<CraftsmanAuthMapper, CraftsmanAuth> implements CraftsmanAuthService {
    private final CraftsmanAuthMapper craftsmanAuthMapper;

    @Override
    public Result pageReview(Integer page, Integer pageSize) {
        Page<CraftsmanAuth> craftsmanAuthPage = craftsmanAuthMapper.selectPage(new Page<>(page, pageSize), null);
        PageBean<CraftsmanAuth> pageBean = new PageBean<>(craftsmanAuthPage.getTotal(),craftsmanAuthPage.getRecords());
        log.info("craftsmanPage Data:{}",craftsmanAuthPage.getRecords());
        return Result.success(pageBean);
    }





}
