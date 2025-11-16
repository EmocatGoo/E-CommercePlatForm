package com.yyblcc.ecommerceplatforms.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.yyblcc.ecommerceplatforms.mapper") // 你的 mapper 包路径
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 关键：添加分页拦截器
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.PHOENIX));
        return interceptor;
    }
}
