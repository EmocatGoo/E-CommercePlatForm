package com.yyblcc.ecommerceplatforms.domain.VO;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductVO {
    private Long id;
    private String productName;
    private String description;
    private BigDecimal price;
    private List<String> imageUrl;
    private String craftsmanName;
    private String culturalBackground;
    private Integer favoriteCount;
    private Integer likeCount;
    private Integer saleCount;
}
