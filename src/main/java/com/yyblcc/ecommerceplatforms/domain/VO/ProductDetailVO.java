package com.yyblcc.ecommerceplatforms.domain.VO;

import lombok.Data;

@Data
public class ProductDetailVO extends ProductListVO {
    private String description;
    private String craftsmanIntro;
    private String workshopName;
}
