package com.yyblcc.ecommerceplatforms.controller;

import com.google.protobuf.Empty;
import com.yyblcc.ecommerceplatforms.domain.DTO.EmailDTO;
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

    private final EmailService emailService;
    private final VerifyCodeService verifyCodeService;

    @PostMapping("/send-code")
    public ResponseEntity<?> sendCode(@RequestBody EmailDTO dto) {
        String email = dto.getEmail();
        try{
            String code = verifyCodeService.generateAndSend(email);
            emailService.sendVerifyCode(email,code);
            return ResponseEntity.ok(Map.of("code",1,"message","验证码已发送"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error",e.getMessage()));
        }
    }

    @PostMapping("verify")
    public ResponseEntity<?> verify(
            @RequestBody EmailDTO dto,
            HttpSession session) {
        String email = dto.getEmail();
        String code = dto.getVerifycode();
        if (verifyCodeService.validate(email, code)){
            session.setAttribute("user", email);
            return ResponseEntity.ok(Map.of(
                    "message","验证成功",
                    "data", email,
                    "code",1
            ));
        }
        return ResponseEntity.badRequest().body(Map.of("message","验证码错误或已过期","code",0,"data","errorInfo"));
    }
}
