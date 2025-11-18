package com.yyblcc.ecommerceplatforms.controller;

import com.yyblcc.ecommerceplatforms.domain.DTO.AddCartDTO;
import com.yyblcc.ecommerceplatforms.domain.DTO.ToggleCheckDTO;
import com.yyblcc.ecommerceplatforms.domain.VO.CartVO;
import com.yyblcc.ecommerceplatforms.domain.po.Result;
import com.yyblcc.ecommerceplatforms.service.CartService;
import com.yyblcc.ecommerceplatforms.util.context.AuthContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@Slf4j
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;


    /**
     * 加入购物车
     */
    @PostMapping("")
    public Result<?> add(@RequestBody @Validated AddCartDTO dto) {
        // 从 token 取
        Long userId = AuthContext.getUserId();
        cartService.addItem(userId, dto);
        return Result.success("加入成功");
    }

    /**
     * 勾选/取消勾选
     */
    @PutMapping("/check")
    public Result<?> toggleCheck(@RequestBody AddCartDTO dto) {
        Long userId = AuthContext.getUserId();
        cartService.toggleCheck(userId, dto.getProductId(), dto.getIsChecked());
        return Result.success();
    }

    /**
     * 获取我的购物车（含降价提醒）
     */
    @GetMapping("/list")
    public Result<CartVO> list() {
        Long userId = AuthContext.getUserId();
        return Result.success(cartService.getMyCart(userId));
    }
}
