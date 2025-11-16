package com.yyblcc.ecommerceplatforms.util.chainofresponsibility;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yyblcc.ecommerceplatforms.domain.po.User;
import com.yyblcc.ecommerceplatforms.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UsernameHandler implements VerifyHandler {
    private VerifyHandler nextHandler;

    @Autowired
    private UserMapper userMapper;

    @Override
    public CheckResult verify(User user) {
        // 用户名格式验证
        if (user.getUsername() == null ||
                !user.getUsername().matches("^[a-zA-Z0-9_]{4,15}$")) {
            return new CheckResult(false, "用户名至少为4个字符！最大长度不超过15个字符");
        }

        // 用户名查重验证
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername,user.getUsername()));
        if (count > 0) {
            return new CheckResult(false, "用户名已存在！");
        }

        // 如果当前验证通过，继续下一个验证
        return nextHandler != null ? nextHandler.verify(user) : new CheckResult(true, "验证通过");
    }

    @Override
    public void setNextHandler(VerifyHandler nextHandler) {
        this.nextHandler = nextHandler;
    }
}
