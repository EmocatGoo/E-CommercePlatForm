package com.yyblcc.ecommerceplatforms.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommonConfig {
    @Bean
    public RedissonClient redissonClient() {
        RedissonClient redissonClient = Redisson.create();
        return redissonClient;
    }
}
