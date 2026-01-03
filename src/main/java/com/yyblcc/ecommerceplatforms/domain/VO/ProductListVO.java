package com.yyblcc.ecommerceplatforms.domain.VO;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductListVO {
    private Long id;
    private String productName;
    private BigDecimal price;
    private List<String> imageUrl;
    private Integer saleCount;
    private String culturalBackground;
    private String description;
    private String craftsmanName;
    private String craftsmanIntro;
    private String workshopName;
    private Boolean favoriteByCurrentUser;
}
