package com.yyblcc.ecommerceplatforms.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        WebMvcConfigurer.super.addCorsMappings(registry);
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:5173","http://192.168.1.225:5173/", "http://192.168.134.1:5173/", "http://192.168.1.225:5173/", "http://192.168.1.225:5173/")
                .allowCredentials(true)
                .allowedHeaders("*")
                .allowedMethods("POST", "GET", "OPTIONS","DELETE","PUT","PATCH");

    }
}
