package com.yyblcc.ecommerceplatforms.controller;

import com.alibaba.fastjson.JSONObject;
import com.yyblcc.ecommerceplatforms.service.BloomFilterService;

import java.util.HashMap;
import java.util.Map;

import com.yyblcc.ecommerceplatforms.domain.po.Result;
import com.yyblcc.ecommerceplatforms.util.http.HttpClientUtils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 通用控制器，提供一些管理接口
 */
@RestController
@RequestMapping("/common")
@Slf4j
public class CommonController {
    
    @Autowired
    private BloomFilterService bloomFilterService;

    @Value("${gaode.key}")
    private String AMAP_KEY;

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

    @GetMapping("/map/regeo")
    public Result<Map<String,Object>> reverseGeocode(@RequestParam("lng") String lng, @RequestParam("lat") String lat){
        try{
            String url = "https://restapi.amap.com/v3/geocode/regeo";
            Map<String,String> params = new HashMap<>();
            params.put("key",AMAP_KEY);
            params.put("location",lng+","+lat);
            params.put("extensions", "all");
            params.put("batch", "false");
            String resp = HttpClientUtils.doGet(url, params);
            JSONObject json = JSONObject.parseObject(resp);
            if (json != null && "1".equals(json.getString("status"))){
                JSONObject regeocode = json.getJSONObject("regeocode");
                JSONObject comp = regeocode.getJSONObject("addressComponent");
                Map<String,Object> map = new HashMap<>();
                map.put("province",comp.getString("province"));

                Object citiObject = comp.get("city");
                String city = citiObject instanceof String ? citiObject.toString() : comp.getString("province");
                map.put("city",city);
                map.put("district",comp.getString("district"));
                map.put("formattedAddress",regeocode.getString("formatted_address"));
                return Result.success(map);
            }
            String info = json != null ? json.getString("info") : "逆地理解析请求失败";
            return Result.error(info);
        }catch (Exception e){
            log.error("逆地理解析调用失败",e);
            return Result.error("逆地理解析调用失败");
        }
    } 
}
