package com.yyblcc.ecommerceplatforms.util.chainofresponsibility;

import com.yyblcc.ecommerceplatforms.domain.po.User;
import com.yyblcc.ecommerceplatforms.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PhoneHandler implements VerifyHandler {
    private VerifyHandler nextHandler;

    @Autowired
    private UserService userService;

    @Override
    public CheckResult verify(User user) {
        if (user.getPhone() == null ||
            !user.getPhone().matches("^1[3456789]\\d{9}$")) {
            return new CheckResult(false, "手机号码不合法!");
        }
        User exist = userService.query().eq("phone", user.getPhone()).one();
        if (exist != null) {
            return new CheckResult(false,"手机号已被注册!");
        }

        return nextHandler != null ? nextHandler.verify(user) : new CheckResult(true, "验证通过");
    }

    @Override
    public void setNextHandler(VerifyHandler nextHandler) {
        this.nextHandler = nextHandler;
    }
}
