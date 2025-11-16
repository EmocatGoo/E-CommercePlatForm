package com.yyblcc.ecommerceplatforms.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    private static final String FROM =  "\"登陆系统\" <3165069523@qq.com>";

    public void sendVerifyCode(String to,String code) throws Exception{
        String html = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 20px auto; padding: 20px; border: 1px solid #eee; border-radius: 8px;">
                    <h2 style="color: #2c3e50;">邮箱登录验证码</h2>
                    <p>您正在使用 <strong>%s</strong> 进行验证。</p>
                    <div style="background: #f8f9fa; padding: 15px; text-align: center; font-size: 28px; letter-spacing: 5px; margin: 20px 0;">
                        <strong style="color: #e74c3c;">%s</strong>
                    </div>
                    <p>验证码有效期 <strong>5 分钟</strong>，请勿泄露~</p>
                    <hr>
                    <small style="color: #95a5a6;">此邮件由系统自动发送，请勿回复。</small>
                </div>
                """.formatted(to, code);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true,"UTF-8");
        helper.setFrom(FROM);
        helper.setTo(to);
        helper.setSubject("【登录验证码】请查收");
        helper.setText(html, true);
        mailSender.send(message);
    }
}
