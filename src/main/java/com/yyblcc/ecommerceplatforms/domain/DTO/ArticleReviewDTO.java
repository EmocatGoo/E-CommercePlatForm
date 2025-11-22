package com.yyblcc.ecommerceplatforms.domain.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ArticleReviewDTO {
    @NotBlank(message = "文章id不能为空")
    private Long id;
    @NotBlank(message = "作者身份权限不能为空")
    //1管理员发布 2匠人发布
    private Integer authorType;
    @NotBlank(message = "作者id不能为空")
    private Long authorId;
    @NotBlank(message = "状态不能为空")
    private Integer status;
}
