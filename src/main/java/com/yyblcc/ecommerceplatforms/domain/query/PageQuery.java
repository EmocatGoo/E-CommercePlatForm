package com.yyblcc.ecommerceplatforms.domain.query;

import lombok.Data;

@Data
public class PageQuery {
    private String keyword;
    private Integer page;
    private Integer pageSize;
}
