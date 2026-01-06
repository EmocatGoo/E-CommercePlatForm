package com.yyblcc.ecommerceplatforms.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("chat_message_read")
public class ChatMessageRead {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long messageId;

    private Integer readerType;

    private Long readerId;

    private LocalDateTime readTime;
}
