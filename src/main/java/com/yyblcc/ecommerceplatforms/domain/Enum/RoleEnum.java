package com.yyblcc.ecommerceplatforms.domain.Enum;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum RoleEnum {
    SUPER_ADMIN(0, "SUPER_ADMIN"),
    ADMIN(1, "ADMIN"),
    CRAFTSMAN(2, "CRAFTSMAN"),
    USER(3, "USER");

    @EnumValue          // 存入数据库的值
    private final int code;

    @JsonValue          // 返回给前端的值（可自定义）
    private final String desc;

    RoleEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static RoleEnum of(int code) {
        for (RoleEnum role : values()) {
            if (role.code == code){
                return role;
            }
        }
        return null;
    }
}