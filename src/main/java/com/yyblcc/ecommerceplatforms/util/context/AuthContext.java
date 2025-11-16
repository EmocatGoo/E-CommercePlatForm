package com.yyblcc.ecommerceplatforms.util.context;

import com.yyblcc.ecommerceplatforms.domain.Enum.RoleEnum;

public class AuthContext {
    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<RoleEnum> ROLE = new ThreadLocal<>();

    public static void setUserId(Long id) { USER_ID.set(id); }
    public static Long getUserId() { return USER_ID.get(); }

    public static void setRole(RoleEnum role) { ROLE.set(role); }
    public static RoleEnum getRole() { return ROLE.get(); }

    public static void clear() {
        USER_ID.remove();
        ROLE.remove();
    }
}
