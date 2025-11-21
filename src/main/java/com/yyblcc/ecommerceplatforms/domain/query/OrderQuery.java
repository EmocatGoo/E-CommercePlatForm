package com.yyblcc.ecommerceplatforms.domain.query;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class OrderQuery {
    private String orderSn;
    private Integer orderStatus;
    @Min(1)
    private Integer page;
    @Min(1)
    private Integer pageSize;
}
