package com.yyblcc.ecommerceplatforms.aspect;

import com.yyblcc.ecommerceplatforms.annotation.UpdateBloomFilter;
import com.yyblcc.ecommerceplatforms.domain.po.*;
import com.yyblcc.ecommerceplatforms.service.BloomFilterService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Set;

/**
 * 布隆过滤器AOP切面
 * 在实体保存后自动将ID添加到布隆过滤器
 */
@Aspect
@Component
@Slf4j
public class BloomFilterAspect {

    @Autowired
    private BloomFilterService bloomFilterService;

    // 需要添加到布隆过滤器的实体类型
    private static final Set<Class<?>> BLOOM_FILTER_ENTITIES = Set.of(
            User.class, Admin.class, Craftsman.class, WorkShop.class,
            UserCollect.class,Product.class,Order.class,OrderItem.class,
            UserAddress.class, Event.class
    );

    /**
     * 拦截带有@UpdateBloomFilter注解的方法
     */
    @AfterReturning(value = "@annotation(updateBloomFilter)", returning = "result")
    public void afterAnnotatedMethod(JoinPoint joinPoint, UpdateBloomFilter updateBloomFilter, Object result) {
        if (!updateBloomFilter.enabled()) {
            return;
        }
        
        handleMethodResult(joinPoint, result);
    }

    /**
     * 拦截MyBatis-Plus的save方法（针对指定实体）
     */
    @AfterReturning(value = "execution(* com.baomidou.mybatisplus.extension.service.IService.save(..))", returning = "result")
    public void afterSave(JoinPoint joinPoint, Object result) {
        if (result instanceof Boolean && (Boolean) result) {
            handleMethodResult(joinPoint, result);
        }
    }

    /**
     * 拦截MyBatis-Plus的insert方法（针对指定实体）
     */
    @AfterReturning(value = "execution(* com.baomidou.mybatisplus.core.mapper.BaseMapper.insert(..))", returning = "result")
    public void afterInsert(JoinPoint joinPoint, Object result) {
        if (result instanceof Integer && (Integer) result > 0) {
            handleMethodResult(joinPoint, result);
        }
    }

    /**
     * 处理方法执行结果
     */
    private void handleMethodResult(JoinPoint joinPoint, Object result) {
        Object[] args = joinPoint.getArgs();
        if (args.length > 0) {
            Object entity = args[0];
            if (isBloomFilterEntity(entity)) {
                addEntityIdToBloomFilter(entity);
            }
        }
    }

    /**
     * 检查是否为需要添加到布隆过滤器的实体
     */
    private boolean isBloomFilterEntity(Object entity) {
        return BLOOM_FILTER_ENTITIES.contains(entity.getClass());
    }

    /**
     * 通过反射获取实体ID并添加到布隆过滤器
     */
    private void addEntityIdToBloomFilter(Object entity) {
        try {
            // 尝试获取getId方法
            Method getIdMethod = entity.getClass().getMethod("getId");
            Object id = getIdMethod.invoke(entity);
            
            if (id != null) {
                bloomFilterService.addIdToBloomFilter(id);
                log.info("已将新增实体 {} 的ID {} 添加到布隆过滤器", 
                        entity.getClass().getSimpleName(), id);
            } else {
                log.warn("实体 {} 的ID为空，无法添加到布隆过滤器", 
                        entity.getClass().getSimpleName());
            }
        } catch (Exception e) {
            log.error("无法获取实体 {} 的ID或添加到布隆过滤器失败: {}", 
                    entity.getClass().getSimpleName(), e.getMessage());
        }
    }
}