package com.yyblcc.ecommerceplatforms.domain.query;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class ArticleQuery {
    private String articleTitle;
    @Min(1)
    private Integer page;
    @Min(1)
    private Integer pageSize;
}
