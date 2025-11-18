package com.yyblcc.ecommerceplatforms.controller.common;

import com.yyblcc.ecommerceplatforms.domain.DTO.*;
import com.yyblcc.ecommerceplatforms.domain.VO.ProductDetailVO;
import com.yyblcc.ecommerceplatforms.domain.VO.ProductListVO;
import com.yyblcc.ecommerceplatforms.domain.po.PageBean;
import com.yyblcc.ecommerceplatforms.domain.po.Result;
import com.yyblcc.ecommerceplatforms.domain.query.ProductQuery;
import com.yyblcc.ecommerceplatforms.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product")
@Slf4j
public class ProductController {

    @Autowired
    private ProductService productService;

    /**
     * 分页查询商品列表（管理员后台用）
     */
    @GetMapping("/page")
    public Result<PageBean> adminPage(@RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(productService.adminPage(page,pageSize));
    }

    /**
     * 审核商品（通过 / 拒绝）
     */
    @PutMapping("/review")
    public Result review(ProductDTO reviewDTO) {
        // status: 1-审核通过 2-审核拒绝 0-下架
        productService.review(reviewDTO);
        return Result.success();
    }

    /**
     * 管理员强制下架商品
     */
    @PutMapping("/offline")
    public Result offline(@RequestParam Long productId) {
        productService.changeStatus(productId, 0);
        return Result.success();
    }

    /**
     * 批量删除商品（逻辑删除）
     */
    @DeleteMapping("/batch")
    public Result batchDelete(@RequestBody List<Long> ids) {
        productService.removeByIds(ids);
        return Result.success();
    }

    /**
     * 匠人提交商品上架申请
     */
    @PostMapping("/save")
    public Result save(@RequestBody @Validated ProductDTO dto) {
        // 从登录信息中获取匠人ID
//        Long craftsmanId = AuthContext.getUserId();
        //TODO 当前为测试模式，记得修改！！！
        Long craftsmanId = 2L;
        productService.craftsmanSave(dto, craftsmanId);
        return Result.success("提交成功，待管理员审核");
    }

    /**
     * 匠人修改自己的商品（仅限待审核或已驳回的商品）
     */
    @PutMapping("/update")
    public Result update(@RequestBody @Validated ProductDTO dto) {
//        Long craftsmanId = AuthContext.getUserId();
        //TODO 当前为测试模式，记得修改！！！
        Long craftsmanId = 2L;
        productService.craftsmanUpdate(dto, craftsmanId);
        return Result.success();
    }

    /**
     * 匠人查看自己的商品列表
     */
    @GetMapping("/my/page")
    public Result<PageBean> myPage(@RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "10") Integer pageSize) {
//        Long craftsmanId = AuthContext.getUserId();
        //TODO 当前为测试模式，记得修改！！！
        Long craftsmanId = 2L;
        return productService.myPage(page, pageSize, craftsmanId);
    }

    /**
     * 匠人下架自己的商品（已上架状态才允许）
     */
    @PutMapping("/my/offline")
    public Result myOffline(@RequestParam Long productId) {
//        Long craftsmanId = AuthContext.getUserId();
        //TODO 当前为测试模式，记得修改！！！
        Long craftsmanId = 2L;
        productService.craftsmanOffline(productId, craftsmanId);
        return Result.success();
    }

    /**
     * 前台商品详情页（最重要的页面！要富含文化背景）
     */
    @GetMapping("/detail/{id}")
    public Result<ProductDetailVO> detail(@PathVariable Long id) {
        return Result.success(productService.getDetail(id));
    }

    /**
     * 前台用户端商品列表页（分类筛选 + 推荐）
     */
    @GetMapping("/list")
    public Result<PageBean<ProductListVO>> list(ProductQuery search) {
        return Result.success(productService.frontPage(search));
    }

    /**
     * 首页推荐商品（热门非遗好物）
     */
    @GetMapping("/recommend")
    public Result<List<ProductListVO>> recommend(@RequestParam(defaultValue = "8") Integer size) {
        return Result.success(productService.recommend(size));
    }

    /**
     * 根据匠人ID获取其所有上架商品（进入匠人工作室页面时使用）
     */
    @GetMapping("/by-craftsman/{craftsmanId}")
    public Result<List<ProductListVO>> byCraftsman(@PathVariable Long craftsmanId) {
        return Result.success(productService.listByCraftsman(craftsmanId));
    }
}
