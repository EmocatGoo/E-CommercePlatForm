package com.yyblcc.ecommerceplatforms.controller;

import com.yyblcc.ecommerceplatforms.domain.DTO.*;
import com.yyblcc.ecommerceplatforms.domain.VO.ProductListVO;
import com.yyblcc.ecommerceplatforms.domain.VO.ProductStatisticVO;
import com.yyblcc.ecommerceplatforms.domain.po.PageBean;
import com.yyblcc.ecommerceplatforms.domain.po.Result;
import com.yyblcc.ecommerceplatforms.domain.query.ProductQuery;
import com.yyblcc.ecommerceplatforms.service.ProductService;
import com.yyblcc.ecommerceplatforms.util.StpKit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product")
@Slf4j
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 分页查询商品列表（管理员后台用）
     */
    @GetMapping("/page")
    public Result<PageBean> adminPage(ProductQuery query) {
        return Result.success(productService.adminPage(query));
    }

    @PutMapping("/{id}")
    public Result updateProduct(@RequestBody @Validated ProductDTO productDTO){
        log.info("修改商品信息:{}", productDTO);
        return productService.updateProduct(productDTO);
    }

    /**
     * 审核商品（通过 / 拒绝）
     */
    @PutMapping("/review")
    public Result review(@RequestBody ProductDTO reviewDTO) {
        // status: 1-审核通过 2-审核拒绝 0-下架
        productService.review(reviewDTO);
        return Result.success();
    }

    /**
     * 管理员强制下架商品
     */
    @PutMapping("/offline")
    public Result offline(@RequestParam("productId") Long productId) {
        productService.changeStatus(productId, 0);
        return Result.success();
    }

    @PutMapping("/upline")
    public Result upline(@RequestParam("productId") Long productId) {
        productService.changeStatus(productId, 1);
        return Result.success();
    }

    /**
     * 批量删除商品（逻辑删除）
     */
    @DeleteMapping("/batch")
    public Result batchDelete(@RequestBody List<Long> ids) {
        productService.removeByIds(ids);
        stringRedisTemplate.keys("product:page:*").forEach(stringRedisTemplate::delete);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable("id") Long id) {
        productService.removeById(id);
        stringRedisTemplate.keys("product:page:*").forEach(stringRedisTemplate::delete);
        return Result.success();
    }

    /**
     * 匠人提交商品上架申请
     */
    @PostMapping("/save")
    public Result save(@RequestBody @Validated ProductDTO dto) {
        Long craftsmanId = StpKit.CRAFTSMAN.getLoginIdAsLong();
        productService.craftsmanSave(dto, craftsmanId);
        return Result.success("提交成功，待管理员审核");
    }

    /**
     * 匠人修改自己的商品（仅限待审核或已驳回的商品）
     */
    @PutMapping("/update")
    public Result update(@RequestBody @Validated ProductDTO dto) {
        Long craftsmanId = StpKit.CRAFTSMAN.getLoginIdAsLong();
        return productService.craftsmanUpdate(dto, craftsmanId);
    }

    /**
     * 匠人查看自己的商品列表
     */
    @GetMapping("/my/page")
    public Result<PageBean> myPage(ProductQuery query) {
        log.info("匠人获取商品信息:{}", query);
        return productService.myPage(query);
    }

    /**
     * 匠人下架自己的商品（已上架状态才允许）
     */
    @PutMapping("/my/productStatus")
    public Result myOffline(@RequestParam("productId") Long productId) {
        Long craftsmanId = StpKit.CRAFTSMAN.getLoginIdAsLong();
        return productService.craftsmanOffline(productId, craftsmanId);
    }

    /**
     * 前台商品详情页
     */
    @GetMapping("/detail/{id}")
    public Result<ProductListVO> detail(@PathVariable Long id) {
        return Result.success(productService.getDetail(id));
    }

    /**
     * 前台用户端商品列表页
     */
    @GetMapping("/list")
    public Result<PageBean<ProductListVO>> list(ProductQuery search) {
        return Result.success(productService.frontPage(search));
    }

    /**
     * 首页推荐商品（热门非遗好物）
     */
    @GetMapping("/recommend")
    public Result<List<ProductListVO>> recommend(@RequestParam(defaultValue = "4") Integer size) {
        return productService.recommend(size);
    }

    /**
     * 根据匠人ID获取其所有上架商品
     */
    @GetMapping("/by-craftsman/{craftsmanId}")
    public Result<List<ProductListVO>> byCraftsman(@PathVariable Long craftsmanId) {
        return Result.success(productService.listByCraftsman(craftsmanId));
    }

    /**
     * 用户点赞商品
     * @param productId
     * @return
     */
    @PostMapping("/like")
    public Result like(@RequestParam Long productId) {
        return productService.like(productId);
    }

    /**
     * 用户收藏商品
     * @param productId
     * @return
     */
    @PostMapping("/favorite")
    public Result collect(@RequestParam Long productId) {
        return productService.favorite(productId);
    }

    /**
     * 用户获取我收藏的商品
     * @return
     */
    @GetMapping("/myFavorite")
    public Result<List<ProductListVO>> myFavorite() {
        return productService.getMyFavorite();
    }

    /**
     * 用户获取我点赞的商品
     * @param productId
     * @return
     */
    @GetMapping("/myLike")
    public Result<List<ProductListVO>> myLike(@RequestParam Long productId) {
        return productService.getMyLike(productId);
    }

    @GetMapping("/studioInfo")
    public Result<?> getProductWorkShop(@RequestParam Long productId){
        log.info("productId={}", productId);
        return productService.getProductWorkShop(productId);
    }

    @GetMapping("/referRecommend")
    public Result<List<ProductListVO>> referRecommend(@RequestParam Long productId) {
        return productService.referRecommend(productId);
    }

    @GetMapping("/hot")
    public Result<List<ProductStatisticVO>> getHotProduct() {
        return productService.getHotProduct();
    }

}
