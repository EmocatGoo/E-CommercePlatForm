package com.yyblcc.ecommerceplatforms.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_chat_session")
public class ChatSession {
    
    @TableId(type = IdType.ASSIGN_ID)
    private String id; // 会话ID，格式：userId_craftsmanId
    
    private Long userId; // 用户ID
    
    private Long craftsmanId; // 匠人ID
    
    private String lastMessage; // 最后一条消息内容
    
    private LocalDateTime lastMessageTime; // 最后一条消息时间
    
    private Integer userUnreadCount; // 用户未读消息数
    
    private Integer craftsmanUnreadCount; // 匠人未读消息数
    
    private LocalDateTime createTime; // 会话创建时间
}