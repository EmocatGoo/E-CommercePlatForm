package com.yyblcc.ecommerceplatforms.domain.VO;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatSessionVO {
    private String id;
    private Long userId;
    private Long craftsmanId;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private Integer userUnreadCount;
    private Integer craftsmanUnreadCount;
    private String userAvatar;
    private String userName;
    private String craftsmanAvatar;
    private String craftsmanName;
}
