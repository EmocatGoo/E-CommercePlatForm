package com.yyblcc.ecommerceplatforms.domain.query;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class CategoryQuery {
    private String categoryName;
    private Integer tag;
    @Min(1)
    private Integer page;
    @Min(1)
    private Integer pageSize;
}
