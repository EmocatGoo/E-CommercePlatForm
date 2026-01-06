package com.yyblcc.ecommerceplatforms.util;

import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.StpUtil;

/**
 * Sa-Token多账号体系工具类
 * 为每种角色创建独立的StpLogic实例，实现会话隔离
 */
public class StpKit {

    /**
     * 普通用户登录逻辑
     */
    public static final StpLogic USER = new StpLogic("user");

    /**
     * 管理员登录逻辑
     */
    public static final StpLogic ADMIN = new StpLogic("admin");

    /**
     * 工匠登录逻辑
     */
    public static final StpLogic CRAFTSMAN = new StpLogic("craftsman");

    /**
     * 默认登录逻辑（保持与原有代码兼容）
     */
    public static final StpLogic DEFAULT = StpUtil.stpLogic;

}
