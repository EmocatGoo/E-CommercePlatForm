package com.yyblcc.ecommerceplatforms.domain.query;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CraftsmanQuery {
    private String name;
    @Min(1)
    private Integer page;
    @Min(1)
    private Integer pageSize;
}
