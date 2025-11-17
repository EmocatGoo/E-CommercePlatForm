package com.yyblcc.ecommerceplatforms.controller.common;

import com.yyblcc.ecommerceplatforms.domain.DTO.CategoryDTO;
import com.yyblcc.ecommerceplatforms.domain.po.Category;
import com.yyblcc.ecommerceplatforms.domain.po.Result;
import com.yyblcc.ecommerceplatforms.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Delete;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
@RequiredArgsConstructor
@Slf4j
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 管理员分页查看分类列表
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public Result pageCategory(@RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("查询的页面为:{},页面大小为:{}", page, pageSize);
        return categoryService.pageCategory(page,pageSize);
    }

    /**
     * 平台管理员添加分类
     * @param category
     * @return
     */
    @PostMapping("/add")
    public Result addCategory(@RequestBody @Validated Category category) {
        log.info("新增分类信息如下：{}", category);
        return categoryService.insertCategory(category);
    }

    /**
     * 删除单个分类
     * @param id
     * @return
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteCategory(@PathVariable Long id) {
        log.info("删除的分类ID为：{}",id);
        return categoryService.deleteCategory(id);
    }

    /**
     * 批量删除分类
     * @param ids
     * @return
     */
    @DeleteMapping("/batch")
    public Result batchDeleteCategory(@RequestParam List<Long> ids) {
        log.info("删除的分类id集合为：{}",ids);
        return categoryService.batchDeleteCategory(ids);
    }

    /**
     * 更新分类
     * @param categoryDTO
     * @return
     */
    @PutMapping("/update")
    public Result updateCategory(@RequestBody CategoryDTO  categoryDTO) {
        log.info("分类修改的内容如下：{}", categoryDTO);
        return categoryService.updateCategory(categoryDTO);
    }
}
