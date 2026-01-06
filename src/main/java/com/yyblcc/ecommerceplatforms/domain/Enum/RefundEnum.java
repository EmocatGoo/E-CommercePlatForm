package com.yyblcc.ecommerceplatforms.domain.Enum;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum RefundEnum {
    APPLY(0,"申请中"),
    AGREE(1,"匠人同意"),
    REFUSE(2,"匠人拒绝"),
    INTERPRET(3,"平台介入"),
    SUCCESS(4,"退款成功"),
    CLOSE(5,"退款关闭"),
    REBACK(6,"已撤回"),

    //refund Type
    REFUNDONLY(1,"仅退款"),
    BACKGOODS(2,"退货退款"),

    //refund channel
    RETURN(1,"原路返回"),
    BALANCE(2,"余额"),
    MANUAL(3,"手动打款");

    @EnumValue
    private Integer code;
    @JsonValue
    private String desc;
    RefundEnum(Integer code,String desc){
        this.code = code;
        this.desc = desc;
    }
}
