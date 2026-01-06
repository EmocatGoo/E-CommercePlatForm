package com.yyblcc.ecommerceplatforms.util.id;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaySnGenerator {
    private final SnowflakeIdGenerator snowflakeIdGenerator;
    private static final String PAY_SN_PREFIX = "MPAY";
    private static final int TARGET_LENGTH = 20;
    private static final int SN_SUFFIX_LENGTH = TARGET_LENGTH - PAY_SN_PREFIX.length();

    public String generatePaySn(Long userId) {
        String id = String.valueOf(snowflakeIdGenerator.nextId());

        StringBuilder suffixBuilder = new StringBuilder();

        if(id.length() > SN_SUFFIX_LENGTH){
            suffixBuilder.append(id.substring(id.length() - SN_SUFFIX_LENGTH));
        }else {
            int zerosToAdd = SN_SUFFIX_LENGTH - id.length();
            for (int i = 0; i < zerosToAdd; i++) {
                suffixBuilder.append("0");
            }
            suffixBuilder.append(id);
        }
        return PAY_SN_PREFIX + userId + suffixBuilder.toString();
    }
}
