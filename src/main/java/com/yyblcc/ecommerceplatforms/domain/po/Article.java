package com.yyblcc.ecommerceplatforms.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("tb_article")
public class Article {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String articleTitle;
    private String articleContent;
    //1管理员发布 2匠人发布
    private Integer authorType;
    private Long authorId;
    private String authorName;
    //1非遗新闻 2知识文章
    private Integer articleType;
    //0审核中 1已发布 2已下架
    private Integer status;
    private String coverImage;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}
