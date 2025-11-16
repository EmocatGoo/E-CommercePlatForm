package com.yyblcc.ecommerceplatforms.controller.common;

import com.yyblcc.ecommerceplatforms.service.BloomFilterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 通用控制器，提供一些管理接口
 */
@RestController
@RequestMapping("/common")
public class CommonController {
    
    @Autowired
    private BloomFilterService bloomFilterService;

    /**
     * 手动重新初始化布隆过滤器（管理接口）
     */
    @PostMapping("/bloom-filter/reinit")
    public ResponseEntity<String> reinitBloomFilter() {
        try {
            bloomFilterService.initializeBloomFilter();
            return ResponseEntity.ok("布隆过滤器重新初始化成功");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("布隆过滤器重新初始化失败: " + e.getMessage());
        }
    }
    
    /**
     * 添加单个ID到布隆过滤器
     */
    @PostMapping("/bloom-filter/add/{id}")
    public ResponseEntity<String> addToBloomFilter(@PathVariable Object id) {
        try {
            bloomFilterService.addIdToBloomFilter(id);
            return ResponseEntity.ok("ID添加到布隆过滤器成功");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("ID添加到布隆过滤器失败: " + e.getMessage());
        }
    }
}
