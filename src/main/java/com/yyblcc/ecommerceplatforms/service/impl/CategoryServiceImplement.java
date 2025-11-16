package com.yyblcc.ecommerceplatforms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yyblcc.ecommerceplatforms.domain.po.Category;
import com.yyblcc.ecommerceplatforms.mapper.CategoryMapper;
import com.yyblcc.ecommerceplatforms.service.CategoryService;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImplement extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
}
