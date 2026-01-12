package com.yyblcc.ecommerceplatforms.util.id;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 订单号生成器
 * 生成前缀为NH，长度为20的订单号
 */
@Component
@RequiredArgsConstructor
public class OrderGroupSnGenerator {
    private final SnowflakeIdGenerator snowflakeIdGenerator;
    private static final String ORDER_SN_PREFIX = "OG";
    private static final int TARGET_LENGTH = 20;
    private static final int SN_SUFFIX_LENGTH = TARGET_LENGTH - ORDER_SN_PREFIX.length();

    /**
     * 生成订单号
     * @return 格式为NH+18位数字的订单号
     */
    public String generateOrderGroupSn() {
        long id = snowflakeIdGenerator.nextId();
        String idStr = String.valueOf(id);
        
        // 确保后缀部分长度为18位
        StringBuilder suffixBuilder = new StringBuilder();
        
        if (idStr.length() > SN_SUFFIX_LENGTH) {
            // 如果ID过长，截取后18位
            suffixBuilder.append(idStr.substring(idStr.length() - SN_SUFFIX_LENGTH));
        } else {
            // 如果ID不足18位，前面补0
            int zerosToAdd = SN_SUFFIX_LENGTH - idStr.length();
            for (int i = 0; i < zerosToAdd; i++) {
                suffixBuilder.append('0');
            }
            suffixBuilder.append(idStr);
        }
        
        return ORDER_SN_PREFIX + suffixBuilder.toString();
    }
}