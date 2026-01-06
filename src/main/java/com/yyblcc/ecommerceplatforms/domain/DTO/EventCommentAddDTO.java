package com.yyblcc.ecommerceplatforms.domain.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class EventCommentAddDTO {

    @NotBlank(message = "活动id不能为空")
    private Long eventId;

    @NotBlank(message = "评论内容不能为空")
    @Size(max = 2000, message = "评论最多2000字")
    private String content;

    private Long parentCommentId;
    //被回复者的用户id
    private Long replyToUserId;
    //被回复者的用户nickname
    private String replyToUsername;
    //评论的图片列表，多张用逗号隔开
    private List<String> imageUrl;
}
