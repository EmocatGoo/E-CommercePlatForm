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

    private String authorNickname;
    private String authorAvatar;

    private Boolean likedByCurrentUser;

    // 被回复人的信息（楼中楼里显示“回复 @张三”）
    private Long replyToUserId;
    private String replyToUsername;

    // 子评论（楼中楼展开时返回）
    private List<ProductCommentVO> children;

    private List<String> imageUrl;

}
