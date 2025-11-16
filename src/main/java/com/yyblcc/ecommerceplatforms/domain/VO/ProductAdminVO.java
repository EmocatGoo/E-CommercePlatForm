package com.yyblcc.ecommerceplatforms.domain.VO;

import com.yyblcc.ecommerceplatforms.domain.po.Product;
import lombok.Data;

@Data
public class ProductAdminVO extends Product {
    private String craftsmanName;
    private String categoryName;
}
