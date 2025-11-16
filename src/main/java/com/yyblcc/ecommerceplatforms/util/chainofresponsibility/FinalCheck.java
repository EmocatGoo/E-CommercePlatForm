package com.yyblcc.ecommerceplatforms.util.chainofresponsibility;

import com.yyblcc.ecommerceplatforms.domain.po.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FinalCheck {
    private VerifyHandler firstHandler;
    @Autowired
    private UsernameHandler usernameHandler;
    @Autowired
    private EmailHandler emailHandler;
    @Autowired
    private PhoneHandler phoneHandler;
    @Autowired
    private PasswordHandler passwordHandler;


    private void init() {
        // 构建责任链
        usernameHandler.setNextHandler(emailHandler);
        emailHandler.setNextHandler(phoneHandler);
        phoneHandler.setNextHandler(passwordHandler);
        passwordHandler.setNextHandler(null);
        this.firstHandler = usernameHandler;
    }

    public CheckResult check(User user) {
        init();
        if (user == null) {
            return new CheckResult(false, "用户信息不能为空");
        }
        log.debug("开始验证用户信息: {}", user.getUsername());
        CheckResult result = firstHandler.verify(user);
        log.debug("用户验证结果: {}", result.getMessage());
        return result;
    }
}
