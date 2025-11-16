package com.yyblcc.ecommerceplatforms.domain.VO;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductListVO {
    private Long id;
    private String productName;
    private BigDecimal price;
    private String imageUrl;
    private String craftsmanName;
    private String culturalBackground;
}
