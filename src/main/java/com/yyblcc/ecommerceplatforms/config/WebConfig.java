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
                        "/user/register", "/craftsman/apply","/user/logout",
                        "/craftsman/logout","/category/*",

                        // 首页 & 商品 & 文章 & 活动
                        "/", "/home", "/index.html",
                        "/goods/**", "/article/**", "/activity/**",
                        "/common",

                        // 静态资源
                        "/static/**", "/css/**", "/js/**", "/images/**",

                        // Swagger & 错误
                        "/swagger-ui/**", "/v3/api-docs/**", "/error"
                );
    }
}
