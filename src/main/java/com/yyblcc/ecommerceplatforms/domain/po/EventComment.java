package com.yyblcc.ecommerceplatforms.domain.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("tb_event_comment")
public class EventComment {
    @TableId(type = IdType.AUTO)
    private Long id;
    //活动id
    private Long eventId;
    //根评论id
    private Long rootId;
    //评论的用户id
    private Long userId;
    //评论内容
    private String content;
    //评论图片列表
    private List<String> imageUrl;
    //点赞数量
    private Integer likeCount;
    //回复数量（子孙评论数）
    private Integer replyCount;
    //路径枚举
    private String path;
    //层级差
    private Integer depth;


    private LocalDateTime createTime;
    @TableLogic
    private Integer isDeleted;

    @TableField(exist = false)
    private List<ProductComment> children;

}
