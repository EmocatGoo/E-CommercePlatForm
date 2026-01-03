package com.yyblcc.ecommerceplatforms.domain.query;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class ArticleQuery {
    private String articleTitle;
    private Integer articleReviewStatus;
    private Long categoryId;
    private Integer articleType;
    @Min(1)
    private Integer page;
    @Min(1)
    private Integer pageSize;
}
