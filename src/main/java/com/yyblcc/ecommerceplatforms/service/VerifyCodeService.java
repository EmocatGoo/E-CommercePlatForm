package com.yyblcc.ecommerceplatforms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Random;

@Service
public class VerifyCodeService {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Value("${app.verify-code.length}")
    private Integer length;

    @Value("${app.verify-code.expire-seconds}")
    private Long expireSeconds;

    @Value("${app.verify-code.send-limit-seconds}")
    private Long sendLimitSeconds;

    private final Random random = new Random();

    public String generateAndSend(String email){
        String limitKey = "veryfy:limit:" + email;
        if (stringRedisTemplate.hasKey(limitKey).equals(true)) {
            throw new RuntimeException("请勿频繁操作，1分钟后重试");
        }

        String code = String.format("%06d", random.nextInt(10000));
        String codeKey = "veryfy:code:" + email;

        stringRedisTemplate.opsForValue().set(codeKey,code, Duration.ofSeconds(expireSeconds));
        stringRedisTemplate.opsForValue().set(limitKey,"1", Duration.ofSeconds(sendLimitSeconds));

        return code;
    }

    public boolean validate(String email, String code){
        String cacheStr = stringRedisTemplate.opsForValue().get("veryfy:code:" + email);
        if (cacheStr != null && cacheStr.equals(code)) {
            stringRedisTemplate.delete("veryfy:code:" + email);
            return true;
        }
        return false;
    }

}
