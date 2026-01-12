package com.yyblcc.ecommerceplatforms.domain.DTO;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemDTO {
    // 商品id
    private Long productId;

    // 商品名称
    private String productName;

    // 商品图片
    private String productImage;

    // 商品价格
    private BigDecimal price;

    // 商品数量
    private Integer quantity;
}
