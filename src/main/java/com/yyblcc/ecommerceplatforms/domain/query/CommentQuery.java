package com.yyblcc.ecommerceplatforms.domain.query;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class CommentQuery {
    private String keyWordComment;
    @Min(1)
    private Integer page;
    @Min(1)
    private Integer pageSize;
}
