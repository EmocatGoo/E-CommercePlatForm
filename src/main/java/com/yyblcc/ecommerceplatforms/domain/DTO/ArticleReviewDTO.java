package com.yyblcc.ecommerceplatforms.domain.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ArticleReviewDTO {
    private Long id;
    //1管理员发布 2匠人发布
    private Integer authorType;
    private Long authorId;
    private Integer status;
    private String refuseReason;
}
