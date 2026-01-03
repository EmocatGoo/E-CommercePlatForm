package com.yyblcc.ecommerceplatforms.domain.VO;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ArticleVO {
    private Long id;
    private String articleTitle;
    private String articleContent;
    private Long authorId;
    private String authorName;
    private Integer authorType;
    //1非遗新闻 2知识文章 3文化遗产
    private Integer articleType;
    //1已发布 0下架
    private Integer status;
    private Integer reviewStatus;
    private String refuseReason;
    private String coverImage;

    private Integer viewCount;
    private Integer likeCount;
    private Integer favoriteCount;

    private Boolean userLikeStatus;
    private Boolean userFavoriteStatus;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
