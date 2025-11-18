package com.yyblcc.ecommerceplatforms.service;

import com.yyblcc.ecommerceplatforms.domain.po.*;
import com.yyblcc.ecommerceplatforms.mapper.*;
import com.yyblcc.ecommerceplatforms.util.redis.CacheClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class BloomFilterService {

    private final CacheClient cacheClient;
    private final UserMapper userMapper;
    private final AdminMapper adminMapper;
    private final CraftsmanMapper craftsmanMapper;
    private final WorkShopMapper workShopMapper;
    private final UserAddressMapper userAddressMapper;
    private final UserCollectMapper userCollectMapper;


    /**
     * 初始化布隆过滤器，将所有实体ID添加到过滤器中
     */
    public void initializeBloomFilter() {
        try {
            log.info("开始初始化布隆过滤器...");
            
            // 添加用户ID
            List<User> userList = userMapper.selectList(null);
            userList.forEach(user -> cacheClient.addToBloomFilter(user.getId()));
            log.info("已添加 {} 个用户ID到布隆过滤器", userList.size());
            
            // 添加管理员ID
            List<Admin> adminList = adminMapper.selectList(null);
            adminList.forEach(admin -> cacheClient.addToBloomFilter(admin.getId()));
            log.info("已添加 {} 个管理员ID到布隆过滤器", adminList.size());
            
            // 添加工匠ID
            List<Craftsman> craftsmanList = craftsmanMapper.selectList(null);
            craftsmanList.forEach(craftsman -> cacheClient.addToBloomFilter(craftsman.getId()));
            log.info("已添加 {} 个工匠ID到布隆过滤器", craftsmanList.size());
            
            // 添加工作坊ID
            List<WorkShop> workShopList = workShopMapper.selectList(null);
            workShopList.forEach(workShop -> cacheClient.addToBloomFilter(workShop.getId()));
            log.info("已添加 {} 个工作坊ID到布隆过滤器", workShopList.size());

            //添加地址ID
            List<UserAddress> userAddressList = userAddressMapper.selectList(null);
            userAddressList.forEach(userAddress -> cacheClient.addToBloomFilter(userAddress.getId()));
            log.info("已添加 {} 个地址ID到布隆过滤器", userAddressList.size());

            List<UserCollect> userCollectList = userCollectMapper.selectList(null);
            userCollectList.forEach(userCollect -> cacheClient.addToBloomFilter(userCollect.getId()));
            log.info("已添加 {} 个收藏ID到布隆过滤器", userCollectList.size());

            log.info("布隆过滤器初始化完成");
        } catch (Exception e) {
            log.error("布隆过滤器初始化失败", e);
            throw new RuntimeException("布隆过滤器初始化失败", e);
        }
    }
    
    /**
     * 添加单个ID到布隆过滤器
     */
    public void addIdToBloomFilter(Object id) {
        try {
            cacheClient.addToBloomFilter((Long) id);
            log.debug("已添加ID {} 到布隆过滤器", id);
        } catch (Exception e) {
            log.error("添加ID {} 到布隆过滤器失败", id, e);
            throw new RuntimeException("添加ID到布隆过滤器失败", e);
        }
    }

}