package com.yyblcc.ecommerceplatforms.domain.Enum;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum OrderStatusEnum {
    PENDING(0,"待支付"),
    DISPATCH(1,"待发货"),
    RECEIPT(2,"待收货"),
    BEEVALUATED(3,"待评价"),
    REFUND(4,"退款/售后"),
    CANCEL(5,"取消"),
    EVALUATED(6,"已评价");

    @EnumValue
    private final int code;

    @JsonValue
    private final String desc;

    OrderStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

}
