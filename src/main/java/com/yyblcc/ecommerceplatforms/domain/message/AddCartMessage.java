package com.yyblcc.ecommerceplatforms.domain.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddCartMessage implements Serializable {
    private Long userId;
    private Long productId;
    private Long craftsmanId;
    private String productName;
    private String productImage;
    private BigDecimal priceAtAdd;
    private Integer quantity;
    private boolean checked;
}
