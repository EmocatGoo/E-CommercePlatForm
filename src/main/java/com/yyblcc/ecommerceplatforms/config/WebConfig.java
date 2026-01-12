package com.yyblcc.ecommerceplatforms.config;

import com.yyblcc.ecommerceplatforms.inteceptor.AuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                // 拦截所有
                .addPathPatterns("/**")
                // 放行公开路径
                .excludePathPatterns(
                        // 登录相关
                        "/admin/login", "/craftsman/login", "/user/login",
                        "/user/register", "/craftsman/apply", "/user/logout",
                        "/craftsman/save",
                        "/craftsman/logout", "/admin/logout","/craftsman/check",
                        "/craftsman/check-email","/user/check", "/user/check-email",
                        "/email/**","/search/**","/chat/modelInStream","/chat/modelInStream-notLogin",
                        "/craftsman/front-page", "/workshop/front-page",

                        // 用户相关
                        "/product/by-craftsman","/product/recommend",
                        "/product/list", "/product/detail/**",
                        "/by-craftsman/**",

                        // 首页 & 静态资源
                        "/", "/home", "/index.html",
                        "/common/**",

                        // 静态资源
                        "/static/**", "/css/**", "/js/**", "/images/**",

                        // Swagger & 错误
                        "/swagger-ui/**", "/v3/api-docs/**", "/error"
                );
    }
}
