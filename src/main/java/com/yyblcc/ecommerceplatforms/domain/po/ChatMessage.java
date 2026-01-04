package com.yyblcc.ecommerceplatforms.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("chat_message")
public class ChatMessage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String sessionId;

    private Integer senderType;

    private Long senderId;

    private Integer receiverType;

    private Long receiverId;

    private String content;

    private Integer msgType; // 1=文本

    private Integer status; // 0未读 1已读

    private LocalDateTime createTime;
}
