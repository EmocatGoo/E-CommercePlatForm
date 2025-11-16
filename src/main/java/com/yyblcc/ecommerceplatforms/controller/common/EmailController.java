package com.yyblcc.ecommerceplatforms.controller.common;

import com.yyblcc.ecommerceplatforms.service.EmailService;
import com.yyblcc.ecommerceplatforms.service.VerifyCodeService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/email")
@RequiredArgsConstructor
public class EmailController {
    @Autowired
    private EmailService emailService;
    @Autowired
    private VerifyCodeService verifyCodeService;

    @PostMapping("/send-code")
    public ResponseEntity<?> sendCode(@RequestParam @Email String email) {
        try{
            String code = verifyCodeService.generateAndSend(email);
            emailService.sendVerifyCode(email,code);
            return ResponseEntity.ok(Map.of("message","验证码已发送"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error",e.getMessage()));
        }
    }

    @PostMapping("verify")
    public ResponseEntity<?> verify(
            @RequestParam @Email String email,
            @RequestParam String code,
            HttpSession session) {
        if (verifyCodeService.validate(email, code)){
            session.setAttribute("user", email);
            return ResponseEntity.ok(Map.of(
                    "message","验证成功",
                    "data", email,
                    "response-code",1
            ));
        }
        return ResponseEntity.badRequest().body(Map.of("error","验证码错误或已过期"));
    }
}
