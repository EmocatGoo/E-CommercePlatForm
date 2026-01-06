package com.yyblcc.ecommerceplatforms.controller;

import com.yyblcc.ecommerceplatforms.domain.DTO.AddCartDTO;
import com.yyblcc.ecommerceplatforms.domain.DTO.CartItemToggle;
import com.yyblcc.ecommerceplatforms.domain.DTO.ToggleCheckDTO;
import com.yyblcc.ecommerceplatforms.domain.VO.CartVO;
import com.yyblcc.ecommerceplatforms.domain.po.Result;
import com.yyblcc.ecommerceplatforms.service.CartService;
import com.yyblcc.ecommerceplatforms.util.StpKit;
import com.yyblcc.ecommerceplatforms.util.context.AuthContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Delete;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cart")
@Slf4j
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;


    /**
     * 加入购物车
     */
    @PostMapping
    public Result<?> add(@RequestBody @Validated AddCartDTO dto) {
        Long userId = StpKit .USER.getLoginIdAsLong();
        cartService.addItem(userId, dto);
        return Result.success("加入成功");
    }

    /**
     * 勾选/取消勾选
     */
    @PutMapping("/check")
    public Result<?> toggleCheck(@RequestBody AddCartDTO dto) {
        Long userId = StpKit .USER.getLoginIdAsLong();
        log.info("传入数据为：{}",dto);
        cartService.toggleCheck(userId, dto.getProductId(), dto.getChecked());
        return Result.success();
    }

    /**
     * 获取我的购物车（含降价提醒）
     */
    @GetMapping("/list")
    public Result<CartVO> list() {
        Long userId = StpKit .USER.getLoginIdAsLong();
        return Result.success(cartService.getMyCart(userId));
    }

    @DeleteMapping("/batch")
    public Result<?> delete(@RequestParam("productIds") List<Long> productIds) {
        log.info("传入的购物车项id集合为：{}",productIds);
        return cartService.batchdelete(productIds);
    }

    @DeleteMapping
    public Result<?> delete(@RequestParam("productId")Long productId) {
        log.info("删除的商品id为：{}",productId);
        return cartService.delete(productId);
    }

    @PostMapping("/toggleQuantity")
     public void toggleQuantity(@RequestBody CartItemToggle dto) {
        cartService.toggleQuantity(dto);
    }

}
