package com.yyblcc.ecommerceplatforms.util.redis;

import com.yyblcc.ecommerceplatforms.domain.po.Product;
import com.yyblcc.ecommerceplatforms.mapper.ProductMapper;
import com.yyblcc.ecommerceplatforms.service.ArticleRankService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class InitialProductStock implements CommandLineRunner {

    private final ProductMapper productMapper;
    private static final String PRODUCT_STOCK_KEY = "product:stock";
    private static final String PRODUCT_STOCK_HASH_KEY = "product:";
    private final StringRedisTemplate stringRedisTemplate;
    private final ArticleRankService articleRankService;
    @Override
    public void run(String... args) throws Exception {
        List<Product> products = productMapper.selectList(null);

        products.forEach(product -> {
            stringRedisTemplate.opsForHash().put(PRODUCT_STOCK_KEY,PRODUCT_STOCK_HASH_KEY + product.getId(),product.getStock().toString());
        });

        new Thread(() -> {
            try {
                Thread.sleep(5000);
                articleRankService.initZSetFromDB();
                log.info("热门文章ZSet初始化完成");
            } catch (Exception e) {
                log.error("ZSet初始化失败", e);
            }
        }).start();
    }



}
