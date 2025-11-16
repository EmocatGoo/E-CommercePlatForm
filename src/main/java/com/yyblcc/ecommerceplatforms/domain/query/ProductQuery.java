package com.yyblcc.ecommerceplatforms.domain.query;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductQuery {
    private String keyword;
    private Integer categoryId;
    @Min(1)
    private Integer page;
    @Min(1)
    private Integer pageSize;
}
