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
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        try {
            String uri = request.getRequestURI();
            String method = request.getMethod();
            
            // 检查是否是公开的GET请求（不需要登录）
            if (isPublicGetRequest(uri, method)) {
                log.info("公开GET请求，放行: {} {}", method, uri);
                return true;
            }
            
            // 检查是否登录，遍历所有角色的StpLogic实例
            boolean isLogin = false;
            RoleEnum role = null;
            Long userId = null;
            
            // 检查用户登录
            if (StpKit.USER.isLogin()) {
                isLogin = true;
                role = (RoleEnum) StpKit.USER.getSession().get("ROLE");
                userId = StpKit.USER.getLoginIdAsLong();
                log.info("用户登录检测: userId={}, role={}", userId, role);
            }
            // 检查管理员登录
            else if (StpKit.ADMIN.isLogin()) {
                isLogin = true;
                role = (RoleEnum) StpKit.ADMIN.getSession().get("ROLE");
                userId = StpKit.ADMIN.getLoginIdAsLong();
                log.info("管理员登录检测: userId={}, role={}", userId, role);
            }
            // 检查工匠登录
            else if (StpKit.CRAFTSMAN.isLogin()) {
                isLogin = true;
                role = (RoleEnum) StpKit.CRAFTSMAN.getSession().get("ROLE");
                userId = StpKit.CRAFTSMAN.getLoginIdAsLong();
                log.info("匠人登录检测: userId={}, role={}", userId, role);
            }

            if (!isLogin || role == null) {
                log.warn("未登录或角色为空: uri={}, method={}", uri, method);
                sendJson(response, 401, "请先登录");
                return false;
            }

            if (!hasPermission(role, uri, method)) {
                log.warn("权限不足: role={}, uri={}, method={}", role, uri, method);
                sendJson(response, 403, "权限不足");
                return false;
            }

            // 将用户信息存储到 AuthContext，供后续使用
            AuthContext.setRole(role);
            AuthContext.setUserId(userId);
            
            log.info("认证通过: role={}, userId={}, uri={}, method={}", role, userId, uri, method);
            return true;
        } catch (Exception e) {
            log.error("认证拦截器异常", e);
            sendJson(response, 401, "认证失败");
            return false;
        }
    }
    
    /**
     * 检查是否是公开的GET请求（不需要登录）
     */
    private boolean isPublicGetRequest(String uri, String method) {
        if (!"GET".equals(method)) {
            return false;
        }
        
        // 公开的GET请求路径
        return uri.startsWith("/category") ||
               uri.startsWith("/product") ||
               uri.startsWith("/article") ||
               uri.startsWith("/event") ||
               uri.startsWith("/goods");
    }

    private boolean hasPermission(RoleEnum role, String uri, String method) {
        return switch (role) {
            // 超级管理员：所有权限
            case SUPER_ADMIN -> true;
            // 普通管理员
            case ADMIN -> isAdminPermission(uri, method);
            // 匠人
            case CRAFTSMAN -> isCraftsmanPermission(uri, method);
            // 用户
            case USER -> isUserPermission(uri, method);

            default -> false;
        };
    }

    // 普通管理员：不能新增管理员，可以管理商品、订单、文章、活动、分类
    private boolean isAdminPermission(String uri, String method) {
        // 管理员相关操作
        if (uri.startsWith("/admin/")) {
            return !"/admin/save".equals(uri) || !"POST".equals(method);
        }
        // 管理员上传接口
        if (uri.startsWith("/oss/upload/")) {
            return true;
        }
        // 可以管理商品、订单、文章、活动、分类、产品
        return uri.startsWith("/product/") ||
                uri.startsWith("/order/") ||
                uri.startsWith("/article/") ||
                uri.startsWith("/event/") ||
                uri.startsWith("/category");
    }

    // 匠人：只能管自己 + 上传商品 + 订单 + 查看分类
    private boolean isCraftsmanPermission(String uri, String method) {
        // 分类只能查看，不能修改
        if (uri.startsWith("/category")) {
            return "GET".equals(method);
        }
        // 匠人上传接口（头像、封面）
        if (uri.startsWith("/oss/")) {
            return true;
        }
        return uri.startsWith("/craftsman/") ||
                uri.startsWith("/workshop/") ||
                uri.startsWith("/order/") ||
                uri.startsWith("/product/") ||
                uri.startsWith("/article") ;
    }

    // 用户：个人中心 + 订单 + 活动报名 + 查看商品和分类
    private boolean isUserPermission(String uri, String method) {
        // 分类和商品只能查看，不能修改
        if (uri.startsWith("/category") ||
                uri.startsWith("/product/") ||
                uri.startsWith("/article/") ||
                uri.startsWith("/workshop/") ||
                uri.startsWith("/craftsman")) {
            return "GET".equals(method) || "POST".equals(method);
        }
        // 用户上传接口（头像）
        if (uri.startsWith("/oss/upload/") || uri.startsWith("/cart/") || uri.startsWith("/cart") || uri.startsWith("/event/apply")) {
            return true;
        }
        return uri.startsWith("/user/") ||
                uri.startsWith("/order/") ||
                uri.startsWith("/pay/") ||
                uri.startsWith("/comment") ||
                uri.startsWith("/oss/") ||
                uri.startsWith("/question/");
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
