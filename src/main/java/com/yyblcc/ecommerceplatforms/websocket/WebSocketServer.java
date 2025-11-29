package com.yyblcc.ecommerceplatforms.websocket;

import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/ws/{userId}")
@Component
@Slf4j
public class WebSocketServer {
    private static final ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        sessions.put(userId, session);
        log.info("用户 {} 已连接", userId);
    }

    @OnMessage
    public void onMessage(String message, @PathParam("userId") String userId) {
        log.info("用户 {} 发送了消息：{}", userId, message);
        sendMessage(userId,message);
    }

    public void sendMessage(String userId, String content) {
        Session session = sessions.get(userId);
        if (session != null) {
            session.getAsyncRemote().sendText(content);
        }
    }



}
