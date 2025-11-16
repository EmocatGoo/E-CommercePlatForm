package com.yyblcc.ecommerceplatforms.inteceptor;

import com.yyblcc.ecommerceplatforms.domain.Enum.RoleEnum;
import com.yyblcc.ecommerceplatforms.util.context.AuthContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Slf4j
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("ROLE") == null) {
            sendJson(response, 401, "请先登录");
            return false;
        }

        RoleEnum role = (RoleEnum) session.getAttribute("ROLE");
        String uri = request.getRequestURI();

        if (!hasPermission(role, uri, request.getMethod())) {
            sendJson(response, 403, "权限不足");
            return false;
        }

        AuthContext.setRole(role);
        AuthContext.setUserId((Long) session.getAttribute("USER_ID"));

        return true;
    }

    private boolean hasPermission(RoleEnum role, String uri, String method) {
        return switch (role) {
            // 超级管理员
            case SUPER_ADMIN -> true;
            // 普通管理员
            case ADMIN -> isAdminPermission(uri, method);
            // 匠人
            case CRAFTSMAN -> isCraftsmanPermission(uri, method);
            // 用户
            case USER -> isUserPermission(uri);

            default -> false;
        };
    }

    // 普通管理员：不能新增管理员
    private boolean isAdminPermission(String uri, String method) {
        if (uri.startsWith("/admin/")) {
            if (uri.equals("/admin/add") && "POST".equals(method)) {
                return false; // 禁止新增管理员
            }
            return true;
        }
        return uri.startsWith("/goods/") ||
                uri.startsWith("/orders/") ||
                uri.startsWith("/article/") ||
                uri.startsWith("/activity/");
    }

    // 匠人：只能管自己 + 上传商品 + 订单
    private boolean isCraftsmanPermission(String uri, String method) {
        return uri.startsWith("/craftsman/") ||
                uri.startsWith("/goods/upload") ||
                uri.startsWith("/orders/my");
    }

    // 用户：个人中心 + 订单 + 活动报名
    private boolean isUserPermission(String uri) {
        return uri.startsWith("/user/") ||
                uri.startsWith("/orders/my") ||
                uri.startsWith("/activity/enroll") ||
                uri.startsWith("/goods/");
    }

    private void sendJson(HttpServletResponse response, int code, String msg) throws IOException {
        response.setStatus(code);
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().write("{\"code\":" + code + ",\"msg\":\"" + msg + "\"}");
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        AuthContext.clear();
    }
}
