package com.yyblcc.ecommerceplatforms.util.chainofresponsibility;

import com.yyblcc.ecommerceplatforms.domain.po.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PasswordHandler implements VerifyHandler {
    private VerifyHandler nextHandler;

    @Override
    public CheckResult verify(User user) {
        log.debug("开始密码验证");
        
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            log.debug("密码为空");
            return new CheckResult(false, "密码不能为空");
        }
        
        String password = user.getPassword();
        log.debug("正在验证密码: {}", password);
        
        // 检查长度
        if (password.length() < 6) {
            log.debug("密码长度不足6位");
            return new CheckResult(false, "密码长度必须至少6位");
        }
        
        // 检查是否包含字母
        boolean hasLetter = password.matches(".*[a-zA-Z]+.*");
        log.debug("密码包含字母: {}", hasLetter);
        if (!hasLetter) {
            return new CheckResult(false, "密码必须包含字母");
        }
        
        // 检查是否包含数字
        boolean hasDigit = password.matches(".*\\d+.*");
        log.debug("密码包含数字: {}", hasDigit);
        if (!hasDigit) {
            return new CheckResult(false, "密码必须包含数字");
        }
        
        log.debug("密码验证通过");
        return nextHandler != null ? nextHandler.verify(user) : new CheckResult(true, "验证通过");
    }

    @Override
    public void setNextHandler(VerifyHandler nextHandler) {
        this.nextHandler = nextHandler;
    }
}
