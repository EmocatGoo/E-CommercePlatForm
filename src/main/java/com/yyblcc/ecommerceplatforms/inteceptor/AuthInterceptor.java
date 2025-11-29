package com.yyblcc.ecommerceplatforms.inteceptor;

import cn.dev33.satoken.stp.StpUtil;
import com.yyblcc.ecommerceplatforms.domain.Enum.RoleEnum;
import com.yyblcc.ecommerceplatforms.util.StpKit;
import com.yyblcc.ecommerceplatforms.util.context.AuthContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Slf4j
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        try {
            // 检查是否登录，遍历所有角色的StpLogic实例
            boolean isLogin = false;
            RoleEnum role = null;
            Long userId = null;
            
            // 检查用户登录
            if (StpKit.USER.isLogin()) {
                isLogin = true;
                role = (RoleEnum) StpKit.USER.getSession().get("ROLE");
                userId = StpKit.USER.getLoginIdAsLong();
            }
            // 检查管理员登录
            else if (StpKit.ADMIN.isLogin()) {
                isLogin = true;
                role = (RoleEnum) StpKit.ADMIN.getSession().get("ROLE");
                userId = StpKit.ADMIN.getLoginIdAsLong();
            }
            // 检查工匠登录
            else if (StpKit.CRAFTSMAN.isLogin()) {
                isLogin = true;
                role = (RoleEnum) StpKit.CRAFTSMAN.getSession().get("ROLE");
                userId = StpKit.CRAFTSMAN.getLoginIdAsLong();
            }

            if (!isLogin || role == null) {
                sendJson(response, 401, "请先登录");
                return false;
            }

            String uri = request.getRequestURI();

            if (!hasPermission(role, uri, request.getMethod())) {
                sendJson(response, 403, "权限不足");
                return false;
            }

            // 将用户信息存储到 AuthContext，供后续使用
            AuthContext.setRole(role);
            AuthContext.setUserId(userId);

            return true;
        } catch (Exception e) {
            log.error("认证拦截器异常", e);
            sendJson(response, 401, "认证失败");
            return false;
        }
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
            return !"/admin/add".equals(uri) || !"POST".equals(method);
        }
        return uri.startsWith("/goods/") ||
                uri.startsWith("/orders/") ||
                uri.startsWith("/article/") ||
                uri.startsWith("/activity/") ||
                uri.startsWith("/category");
    }

    // 匠人：只能管自己 + 上传商品 + 订单
    private boolean isCraftsmanPermission(String uri, String method) {
        return uri.startsWith("/craftsman/") ||
                uri.startsWith("/goods/upload") ||
                uri.startsWith("/orders/my") ||
                uri.startsWith("/category");
    }

    // 用户：个人中心 + 订单 + 活动报名
    private boolean isUserPermission(String uri) {
        return uri.startsWith("/user/") ||
                uri.startsWith("/orders/my") ||
                uri.startsWith("/activity/enroll") ||
                uri.startsWith("/goods/") ||
                uri.startsWith("/category");
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
