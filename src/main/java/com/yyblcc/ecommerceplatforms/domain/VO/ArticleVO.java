package com.yyblcc.ecommerceplatforms.domain.VO;

import lombok.Data;

@Data
public class ArticleVO {
    private Long id;
    private String articleTitle;
    private String articleContent;
    private String authorName;
    //1非遗新闻 2知识文章
    private Integer articleType;
    private String coverImage;
}
