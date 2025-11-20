package com.yyblcc.ecommerceplatforms.domain.Enum;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum PayStatusEnum {
    PENDING(0,"待支付"),
    PAYED(1,"已支付"),
    CANCEL(2,"已取消");

    @EnumValue
    private final int code;

    @JsonValue
    private final String desc;

    PayStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

}
