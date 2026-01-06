package com.yyblcc.ecommerceplatforms.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yyblcc.ecommerceplatforms.domain.DTO.CategoryDTO;
import com.yyblcc.ecommerceplatforms.domain.po.Category;
import com.yyblcc.ecommerceplatforms.domain.po.PageBean;
import com.yyblcc.ecommerceplatforms.domain.po.Result;
import com.yyblcc.ecommerceplatforms.domain.query.CategoryQuery;
import com.yyblcc.ecommerceplatforms.mapper.CategoryMapper;
import com.yyblcc.ecommerceplatforms.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.beans.ConstructorProperties;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImplement extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    private final CategoryMapper categoryMapper;
    @Override
    public Result<PageBean> pageCategory(CategoryQuery query) {
        Page<Category> categoryPage = categoryMapper.selectPage(
                new Page<>(query.getPage(), query.getPageSize()),
                new LambdaQueryWrapper<Category>()
                        .eq(query.getTag() != null, Category::getTag, query.getTag())
                        .like(query.getCategoryName() != null, Category::getCategoryName, query.getCategoryName())
                        .orderByAsc(Category::getCreateTime)
        );
        PageBean<Category> pageBean = new PageBean<>(categoryPage.getTotal(), categoryPage.getRecords());
        return Result.success(pageBean);
    }

    @Override
    public Result insertCategory(Category category) {
        if (category == null) {
            return Result.error("分类信息为空");
        }
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());
        if (save(category)) {
            return Result.success();
        }
        return Result.error("添加失败!");
    }

    @Override
    public Result deleteCategory(Long id) {
        if (id == null || query().eq("id",id).one() == null) {
            return Result.error("未找到对应分类!");
        }
        removeById(id);
        return Result.success("删除成功");
    }

    @Override
    public Result batchDeleteCategory(List<Long> ids) {
        for (Long id : ids) {
            if (query().eq("id",id).one() == null) {
                return Result.error("以下 ID 对应的记录不存在：" + id + "，无法进行删除操作");
            }
        }
        int count = categoryMapper.deleteByIds(ids);
        if (count > 0) {
            return Result.success("批量删除成功!");
        }

        return Result.error("批量删除失败!");
    }

    @Override
    public Result updateCategory(CategoryDTO categoryDTO) {
        Category category = query().eq("id",categoryDTO.getId()).one();
        if (category == null) {
            return Result.error("未找到对应分类信息!");
        }
        BeanUtils.copyProperties(categoryDTO,category);
        category.setUpdateTime(LocalDateTime.now());
        if (updateById(category)) {
            return Result.success("修改成功!");
        }
        return Result.error("修改失败!");
    }

    @Override
    public Result<?> selectHeritageArticleCategory() {
        List<Category> articleCategoryList = categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                .eq(Category::getTag, 4)
                .orderByDesc(Category::getCreateTime));
        return Result.success(articleCategoryList);
    }

    @Override
    public Result<?> selectCultureGameCategory() {
        List<Category>  cultureGameCategoryList = categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                .eq(Category::getTag, 2)
                .orderByDesc(Category::getCreateTime));
        return Result.success(cultureGameCategoryList);
    }

}
