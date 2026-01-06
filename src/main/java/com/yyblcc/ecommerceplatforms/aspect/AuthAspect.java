package com.yyblcc.ecommerceplatforms.aspect;

import com.yyblcc.ecommerceplatforms.annotation.RequireRole;
import com.yyblcc.ecommerceplatforms.domain.Enum.RoleEnum;
import com.yyblcc.ecommerceplatforms.domain.po.Result;
import com.yyblcc.ecommerceplatforms.util.context.AuthContext;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Slf4j
@Aspect
@Component
public class AuthAspect {

    @Pointcut("@annotation(requireRole)")
    public void pointcut(RequireRole requireRole) {}

    @Around("pointcut(requireRole)")
    public Object around(ProceedingJoinPoint joinPoint, RequireRole requireRole) throws Throwable {
        RoleEnum role = AuthContext.getRole();
        Long userId = AuthContext.getUserId();
        
        if (role == null || userId == null) {
            return Result.error("请先登录");
        }

        String[] requiredRoles = requireRole.value();
        String userRole = role.name();

        log.info("用户角色:{},需要的角色:{}", userRole, requiredRoles);

        if (!Arrays.stream(requiredRoles).anyMatch(requiredRole -> 
            requiredRole.equals(userRole) || "SUPER_ADMIN".equals(userRole))) {
            return Result.error("权限不足");
        }
        return joinPoint.proceed();
    }
}
