package com.yyblcc.ecommerceplatforms.util.chainofresponsibility;

import com.yyblcc.ecommerceplatforms.domain.po.User;
import com.yyblcc.ecommerceplatforms.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EmailHandler implements VerifyHandler {
    private VerifyHandler nextHandler;
    @Autowired
    private UserService userService;

    @Override
    public CheckResult verify(User user) {
        String regex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        if (user.getEmail() == null || !user.getEmail().matches(regex)) {
            return new CheckResult(false, "邮箱不合法！");
        }
        User exist = userService.query().eq("email", user.getEmail()).one();
        if (exist != null) {
            return new CheckResult(false,"此邮箱已被注册");
        }
        return nextHandler != null ? nextHandler.verify(user) : new CheckResult(true, "验证通过");
    }

    @Override
    public void setNextHandler(VerifyHandler nextHandler) {
        this.nextHandler = nextHandler;
    }
}
