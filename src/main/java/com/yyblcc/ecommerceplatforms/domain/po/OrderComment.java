package com.yyblcc.ecommerceplatforms.domain.po;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@TableName("tb_order_comment")
public class OrderComment {
    private Long id;
    private Long orderId;
    private String orderSn;
    private Long orderItemId;
    private Long userId;
    private Long craftsmanId;
    private Long productId;
    private Integer score;
    private String content;
    private List<String> images;
    private Integer isAnonymous;
    private Integer isShow;
    private String videoUrl;
    private String replyContent;
    private LocalDateTime replyTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}
