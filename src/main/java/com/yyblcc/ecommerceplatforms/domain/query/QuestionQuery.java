package com.yyblcc.ecommerceplatforms.domain.query;

import lombok.Data;

@Data
public class QuestionQuery {
    private String keyword;
    private Integer questionType;
    private Integer page;
    private Integer pageSize;
}
