package com.yyblcc.ecommerceplatforms.domain.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductDTO {
    private Long id;
    private String productName;
    private BigDecimal price;
    private String description;
    private Integer categoryId;
    private Integer stock;
    private List<String> imageUrl;
    private String culturalBackground;
    private Integer status;
    private String rejectReason;
}
