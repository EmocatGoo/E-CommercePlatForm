package com.yyblcc.ecommerceplatforms.domain.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// 用于更新购物车数量的消息
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCartQuantityMessage {
    private Long userId;
    private Long productId;
    private Long cartId;
    private Integer quantity;
    private Integer checked;
}
