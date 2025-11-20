package com.yyblcc.ecommerceplatforms.domain.Enum;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum OrderStatusEnum {
    PENDING(0,"待支付"),
    BERECEIPT(1,"待发货"),
    BEDISPATCH(2,"待收货"),
    BEEVALUATE(3,"待评价"),
    REFUND(4,"退款/售后"),
    CANCEL(5,"取消");

    @EnumValue
    private final int code;

    @JsonValue
    private final String desc;

    OrderStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

}
