package com.yyblcc.ecommerceplatforms.domain.DTO;

import lombok.Data;

@Data
public class ArticleDTO {
    private Long id;
    private String articleTitle;
    private String articleContent;
    private Long categoryId;
    //1管理员发布 2匠人发布
    private Integer authorType;
    private Long authorId;
    private String authorName;
    //1非遗新闻 2知识文章
    private Integer articleType;
    private String coverImage;
}
