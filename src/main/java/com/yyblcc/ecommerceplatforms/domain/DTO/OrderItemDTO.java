package com.yyblcc.ecommerceplatforms.domain.DTO;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemDTO {
    private Long productId;
//    private String productName;
//    private String productImage;
//    private BigDecimal productPrice;
    private Integer quantity;
}
