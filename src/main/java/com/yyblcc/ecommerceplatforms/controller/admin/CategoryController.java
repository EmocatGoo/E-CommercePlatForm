package com.yyblcc.ecommerceplatforms.controller.admin;

import com.yyblcc.ecommerceplatforms.domain.po.Category;
import com.yyblcc.ecommerceplatforms.domain.po.Result;
import com.yyblcc.ecommerceplatforms.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 新增分类
     */
    @PostMapping
    public Result<?> addCategory(@RequestBody Category category) {
        boolean success = categoryService.save(category);
        return success ? Result.success("新增分类成功") : Result.error("新增分类失败");
    }

    /**
     * 删除分类
     */
    @DeleteMapping("/{id}")
    public Result<?> deleteCategory(@PathVariable Long id) {
        boolean success = categoryService.removeById(id);
        return success ? Result.success("删除分类成功") : Result.error("删除分类失败或分类不存在");
    }

    /**
     * 修改分类
     */
    @PutMapping
    public Result<?> updateCategory(@RequestBody Category category) {
        boolean success = categoryService.updateById(category);
        return success ? Result.success("修改分类成功") : Result.error("修改分类失败");
    }

    /**
     * 查询所有分类
     */
    @GetMapping
    public Result<?> getAllCategories() {
        List<Category> categories = categoryService.list();
        return Result.success(categories);
    }

    /**
     * 根据ID查询分类
     */
    @GetMapping("/{id}")
    public Result<?> getCategoryById(@PathVariable Long id) {
        Category category = categoryService.getById(id);
        if (category == null) {
            return Result.error("未找到该分类");
        }
        return Result.success(category);
    }
}
