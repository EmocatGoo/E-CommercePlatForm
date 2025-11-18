package com.yyblcc.ecommerceplatforms.domain.VO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemVO {
    private Long productId;
    private String productName;
    private String image;
    private BigDecimal priceAtAdd;
    private BigDecimal currentPrice;
    private Integer quantity;
    private Boolean checked;
    private BigDecimal savePrice;

}
