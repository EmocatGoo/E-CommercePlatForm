package com.yyblcc.ecommerceplatforms.domain.DTO;

import lombok.Data;

@Data
public class ChatSendDTO {
    private String sessionId;

    private Integer senderType;
    private Long senderId;

    private Integer receiverType;
    private Long receiverId;

    private String content;
}
