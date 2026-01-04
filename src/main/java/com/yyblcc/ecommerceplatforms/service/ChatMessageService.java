package com.yyblcc.ecommerceplatforms.service;

import com.yyblcc.ecommerceplatforms.domain.po.ChatMessage;

import java.util.List;

public interface ChatMessageService {
    List<ChatMessage> listBySession(String sessionId);
    void saveMessage(ChatMessage message);

}
