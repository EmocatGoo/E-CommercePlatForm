package com.yyblcc.ecommerceplatforms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yyblcc.ecommerceplatforms.domain.DTO.CategoryDTO;
import com.yyblcc.ecommerceplatforms.domain.po.Category;
import com.yyblcc.ecommerceplatforms.domain.po.Result;
import com.yyblcc.ecommerceplatforms.domain.query.CategoryQuery;

import java.util.List;

public interface CategoryService extends IService<Category> {
    Result pageCategory(CategoryQuery query);

    Result insertCategory(Category category);

    Result deleteCategory(Long id);

    Result batchDeleteCategory(List<Long> ids);

    Result updateCategory(CategoryDTO categoryDTO);
}
