package com.yyblcc.ecommerceplatforms.config;

import jakarta.servlet.http.HttpSession;
import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;

public class GetHttpSessionConfig extends ServerEndpointConfig.Configurator {
    @Override
    public void modifyHandshake(ServerEndpointConfig serverEndpointConfig, HandshakeRequest request, HandshakeResponse response) {
        HttpSession httpSession = (HttpSession) request.getHttpSession();

        // 将 httpSession 对象保存起来，存到 ServerEndpointConfig 对象中
        // 在 ChatEndpoint 类的 onOpen 方法就能通过 EndpointConfig 对象获取在这里存入的数据
        serverEndpointConfig.getUserProperties().put(HttpSession.class.getName(), httpSession);
    }

}
