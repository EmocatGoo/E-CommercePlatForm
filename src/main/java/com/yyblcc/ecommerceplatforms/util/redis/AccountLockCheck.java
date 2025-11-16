package com.yyblcc.ecommerceplatforms.util.redis;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.yyblcc.ecommerceplatforms.domain.po.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class AccountLockCheck {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final String ACCOUNT_FAIL_COUNT_KEY = "account:fail:count";
    private static final String ACCOUNT_LOCK_KEYPREFIX = "account:lock:";
    private static final int MAX_FAIL_COUNT = 5;
    private static final Duration LOCK_TTL = Duration.ofMinutes(1);
    private static final Duration FAIL_COUNT_TTL = Duration.ofMinutes(10);

    public Result<?> incrementAndCheckLock(String identifier){
        if (StringUtils.isBlank(identifier)){
            return Result.error("用户名或密码错误!");
        }

        // 先检查是否已锁定
        if (isAccountLocked(identifier)){
            return Result.error("账号已被锁定,请稍后再试!");
        }

        Long failCount = stringRedisTemplate.opsForHash().increment(
                ACCOUNT_FAIL_COUNT_KEY,identifier,1
        );

        if (failCount == null){
            failCount = 1L;
            stringRedisTemplate.expire(ACCOUNT_FAIL_COUNT_KEY,FAIL_COUNT_TTL);
        }

        if (failCount >= MAX_FAIL_COUNT){
            lockAccount(identifier);
            clearFailCount(identifier);
            return Result.error("密码错误已达 5 次，账号锁定 1 分钟");
        }else {
            // 修复：设置失败计数key的过期时间，不是锁定key
            stringRedisTemplate.expire(ACCOUNT_FAIL_COUNT_KEY,FAIL_COUNT_TTL);
            return Result.error("用户名或密码错误!");
        }
    }

    public boolean isAccountLocked(String... identifiers) {
        for (String identifier : identifiers){
            if (StringUtils.isNotBlank(identifier)){
                String lockKey = ACCOUNT_LOCK_KEYPREFIX + identifier;
                Boolean hasKey = stringRedisTemplate.opsForHash().hasKey(lockKey, identifier);
                if (hasKey != null && hasKey){
                    return true;
                }
            }
        }
        return false;
    }

    public void clearFailCount(String identifier) {
        if (StringUtils.isNotBlank(identifier)){
            stringRedisTemplate.opsForHash().delete(ACCOUNT_FAIL_COUNT_KEY,identifier);
        }
    }

    public void lockAccount(String identifier) {
        if (StringUtils.isNotBlank(identifier)){
            String lockKey = ACCOUNT_LOCK_KEYPREFIX +  identifier;
            stringRedisTemplate.opsForHash().put(lockKey, identifier, "1");
            stringRedisTemplate.expire(lockKey,LOCK_TTL);
        }
    }
}
