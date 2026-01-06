package com.yyblcc.ecommerceplatforms.domain.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderCommentDTO {
    //订单id
    @NotNull
    private Long orderId;

    //订单号
    @NotNull
    private String orderSn;

    //用户id
    @NotNull
    private Long userId;

    //匠人id
    @NotNull
    private Long craftsmanId;

    //评分
    @NotNull
    private Integer score;

    //内容
    private String content;

    //买家秀
    private String images;

    //是否匿名
    private Integer anonymous;

    //是否显示
    private Integer show;

    //评价视频
    private String videoUrl;

    //匠人回复内容
    private String replyContent;

    //匠人回复时间
    private String replyTime;
}
