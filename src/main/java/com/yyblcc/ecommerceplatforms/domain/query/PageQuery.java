package com.yyblcc.ecommerceplatforms.domain.query;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PageQuery {
    private String keyword;
    private Integer status;
    private Integer page;
    private Integer pageSize;

    private LocalDateTime beginTime;

    private LocalDateTime endTime;
}
