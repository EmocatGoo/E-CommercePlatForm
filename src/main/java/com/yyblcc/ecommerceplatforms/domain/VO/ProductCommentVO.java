package com.yyblcc.ecommerceplatforms.domain.VO;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductCommentVO {
    private Long id;
    private Long productId;
    private Long rootId;
    private Long userId;
    private String content;
    private Integer likeCount;
    private Integer replyCount;
    private LocalDateTime createTime;
    private Integer depth;
    private Integer isDeleted;

    // 用户信息（实际项目会关联 user 表，这里直接脱敏返回）
    private Long authorId;
    private String authorNickname;
    private String authorAvatar;

    // 当前登录用户是否已点赞该评论
    private Integer likedByCurrentUser;

    // 被回复人的信息（楼中楼里显示“回复 @张三”）
    private Long replyToUserId;
    private String replyToUsername;

    // 子评论（楼中楼展开时返回）
    private List<ProductCommentVO> children;

    // 支持发图时返回
    private List<String> imageUrls;

}
