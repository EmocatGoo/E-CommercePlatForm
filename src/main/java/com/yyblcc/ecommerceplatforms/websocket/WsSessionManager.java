package com.yyblcc.ecommerceplatforms.websocket;

import jakarta.websocket.Session;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WsSessionManager {
    private static final Map<String, Session> ONLINE = new ConcurrentHashMap<>();
    public static String key(Integer type, Long id) {
        return type + "_" + id;
    }

    public static void add(Integer type, Long id, Session session) {
        ONLINE.put(key(type, id), session);
    }

    public static Session get(Integer type, Long id) {
        return ONLINE.get(key(type, id));
    }

    public static void remove(Integer type, Long id) {
        ONLINE.remove(key(type, id));
    }
}
