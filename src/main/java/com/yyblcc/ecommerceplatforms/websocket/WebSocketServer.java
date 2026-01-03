package com.yyblcc.ecommerceplatforms.websocket;


import jakarta.websocket.server.ServerEndpoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@ServerEndpoint("/ws/{userId}/{userType}")
@Component
@Slf4j
@RequiredArgsConstructor
public class WebSocketServer {

}
