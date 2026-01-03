package com.yyblcc.ecommerceplatforms.domain.DTO;

import lombok.Data;

@Data
public class CartItemToggle {
    private Long productId;
    private Long cartId;
    private Integer quantity;
}
