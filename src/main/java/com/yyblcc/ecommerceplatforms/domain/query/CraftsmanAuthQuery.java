package com.yyblcc.ecommerceplatforms.domain.query;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CraftsmanAuthQuery {
    private String keyword;
    private Integer status;
    private String technique;

    private LocalDateTime beginTime;
    private LocalDateTime endTime;
    private Integer page;
    private Integer pageSize;

}
