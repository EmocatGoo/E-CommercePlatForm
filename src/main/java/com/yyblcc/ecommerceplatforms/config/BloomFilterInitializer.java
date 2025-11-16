package com.yyblcc.ecommerceplatforms.config;

import com.yyblcc.ecommerceplatforms.service.BloomFilterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 应用启动时自动初始化布隆过滤器
 */
@Component
@Slf4j
public class BloomFilterInitializer implements ApplicationRunner {
    
    @Autowired
    private BloomFilterService bloomFilterService;
    
    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("应用启动，开始初始化布隆过滤器...");
        bloomFilterService.initializeBloomFilter();
        log.info("布隆过滤器初始化完成");
    }
}