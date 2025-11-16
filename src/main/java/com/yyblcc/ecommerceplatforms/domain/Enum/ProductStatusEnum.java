package com.yyblcc.ecommerceplatforms.domain.Enum;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum ProductStatusEnum {
    PENDING(0, "待审核"),
    APPROVE(1,"审核通过"),
    LISTED(1, "上架"),
    REJECTED(2, "审核拒绝"),
    OFFLINE(0, "下架");

    @EnumValue
    // 存入数据库的值
    private final Integer code;
    @JsonValue
    // 返回给前端的值
    private final String desc;

    ProductStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
