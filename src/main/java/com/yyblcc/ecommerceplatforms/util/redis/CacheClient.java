package com.yyblcc.ecommerceplatforms.util.redis;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class CacheClient {
    private final StringRedisTemplate stringRedisTemplate;
    private final RedissonClient redissonClient;
    private final ThreadPoolTaskExecutor executor;
    private final BloomFilter<String> bloomFilter;


    public CacheClient(StringRedisTemplate stringRedisTemplate, RedissonClient redissonClient) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.redissonClient = redissonClient;

        this.executor = new ThreadPoolTaskExecutor();

        this.executor.setCorePoolSize(4);
        this.executor.setMaxPoolSize(8);
        this.executor.setQueueCapacity(100);
        executor.initialize();

        this.bloomFilter = BloomFilter.create(
                Funnels.stringFunnel(Charset.forName("UTF-8")), 10000, 0.01);
    }

    public void set(String key, Object value, Long ttl, TimeUnit timeUnit){
        Long extraTime = RandomUtil.randomLong(0,5);
        Long TTL = ttl + extraTime;
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value),TTL,timeUnit);
    }

    public <R, ID> Optional<R> query(String keyPrefix, ID id, Class<R> type, Function<ID,R> dbFallback, Long ttl, TimeUnit timeUnit){
        String key = keyPrefix + id;

        if (!bloomFilter.mightContain(String.valueOf(id))) {
            log.warn("拦截到非法id: {}",id);
            return Optional.absent();
        }
        //查询redis缓存
        String jsonStr = stringRedisTemplate.opsForValue().get(key);
        if (jsonStr != null) {
            if ("null".equals(jsonStr)) {
                return Optional.absent();
            }
            return Optional.of(JSONUtil.toBean(jsonStr, type));
        }

        //使用redisson加锁防止缓存击穿
        RLock lock = redissonClient.getLock("lock:" + key);
        boolean isLocked = false;
        try{
            isLocked = lock.tryLock(100,5000,TimeUnit.MILLISECONDS);
            if(!isLocked){
                Thread.sleep(50);
                return query(keyPrefix, id, type, dbFallback, ttl, timeUnit);
            }
            //再次确认redis缓存
            String cacheStr = stringRedisTemplate.opsForValue().get(key);
            if (cacheStr != null) {
                if ("null".equals(cacheStr)) {
                    return Optional.absent();
                }
                return Optional.of(JSONUtil.toBean(cacheStr, type));
            }
            //查询数据库中是否有消息
            R result = dbFallback.apply(id);
            if (result == null){
                stringRedisTemplate.opsForValue().set(key,"null",2,TimeUnit.MINUTES);
                return Optional.absent();
            }

            executor.submit(() -> set(key,result,ttl,timeUnit));
            return Optional.of(result);

        }catch (Exception e){
            log.error("缓存查询异常",e);
            return Optional.absent();
        }finally {
            if(isLocked){
                lock.unlock();
            }
        }
    }

    //将合法id加入到布隆过滤器
    public void addToBloomFilter(Long id){
        bloomFilter.put(String.valueOf(id));
    }
}
