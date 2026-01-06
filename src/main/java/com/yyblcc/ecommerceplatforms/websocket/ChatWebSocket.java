package com.yyblcc.ecommerceplatforms.websocket;

import com.alibaba.fastjson.JSON;
import com.yyblcc.ecommerceplatforms.domain.DTO.ChatSendDTO;
import com.yyblcc.ecommerceplatforms.domain.VO.ChatSessionVO;
import com.yyblcc.ecommerceplatforms.domain.po.ChatMessage;
import com.yyblcc.ecommerceplatforms.domain.po.ChatSession;
import com.yyblcc.ecommerceplatforms.service.ChatMessageService;
import com.yyblcc.ecommerceplatforms.service.ChatSessionService;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@ServerEndpoint("/ws/chat/{actorType}/{actorId}")
@Component
public class ChatWebSocket {
    // 静态注入，用于WebSocket实例访问Spring Bean
    private static ChatMessageService chatMessageService;
    private static ChatSessionService chatSessionService;

    // 无参构造函数，用于Tomcat创建WebSocket实例
    public ChatWebSocket() {
    }

    // Spring自动注入，将ChatMessageService赋值给静态变量
    @Autowired
    public void setChatMessageService(ChatMessageService chatMessageService) {
        ChatWebSocket.chatMessageService = chatMessageService;
    }

    // Spring自动注入，将ChatSessionService赋值给静态变量
    @Autowired
    public void setChatSessionService(ChatSessionService chatSessionService) {
        ChatWebSocket.chatSessionService = chatSessionService;
    }

    @OnOpen
    public void onOpen(Session session,
                       @PathParam("actorType") Integer type,
                       @PathParam("actorId") Long id) {
        WsSessionManager.add(type, id, session);
    }

    @OnMessage
    public void onMessage(String text) throws Exception {
        ChatSendDTO dto = JSON.parseObject(text, ChatSendDTO.class);

        // 创建或获取会话

        ChatSessionVO sessionVO;
        if (dto.getSenderType() == 1) {
            // 用户发送给匠人
            sessionVO = chatSessionService.createOrGetSession(dto.getSenderId(), dto.getReceiverId());
        } else {
            // 匠人发送给用户
            sessionVO = chatSessionService.createOrGetSession(dto.getReceiverId(), dto.getSenderId());
        }
        ChatSession session = new ChatSession();
        BeanUtils.copyProperties(sessionVO, session);
        // 更新会话的最后一条消息
        chatSessionService.updateLastMessage(session.getId(), dto.getContent());

        // 增加接收方的未读消息数
        chatSessionService.incrementUnreadCount(session.getId(), dto.getSenderType());

        // 创建消息对象
        ChatMessage message = new ChatMessage();
        BeanUtils.copyProperties(dto, message);
        message.setSessionId(session.getId());
        message.setStatus(0);
        message.setCreateTime(LocalDateTime.now());

        // 保存消息
        chatMessageService.saveMessage(message);

        // 发送消息给接收方
        Session receiver = WsSessionManager.get(
                dto.getReceiverType(),
                dto.getReceiverId()
        );

        if (receiver != null) {
            receiver.getBasicRemote().sendText(JSON.toJSONString(message));
        }
    }

    @OnClose
    public void onClose(@PathParam("actorType") Integer type,
                        @PathParam("actorId") Long id) {
        WsSessionManager.remove(type, id);
    }
}
