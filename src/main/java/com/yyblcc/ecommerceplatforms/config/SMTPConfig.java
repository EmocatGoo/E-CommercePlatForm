package com.yyblcc.ecommerceplatforms.config;

import java.util.Map;

public class SMTPConfig {

    private final Map<String, SmtpServer> smtpMap = Map.of(
            "qq.com",       new SmtpServer("smtp.qq.com", 587, true, false),
            "gmail.com",    new SmtpServer("smtp.gmail.com", 587, true, false),
            "outlook.com",  new SmtpServer("smtp-mail.outlook.com", 587, true, false),
            "hotmail.com",  new SmtpServer("smtp-mail.outlook.com", 587, true, false),
            "163.com",      new SmtpServer("smtp.163.com", 465, false, true),
            "126.com",      new SmtpServer("smtp.126.com", 465, false, true),
            "yeah.net",     new SmtpServer("smtp.yeah.net", 465, false, true)
    );

    public static class SmtpServer{
        private final String host;
        private final int port;
        private final boolean starttls;
        private final boolean ssl;

        public SmtpServer(String host, int port, boolean starttls, boolean ssl) {
            this.host = host;
            this.port = port;
            this.starttls = starttls;
            this.ssl = ssl;
        }
    }

    public SmtpServer getSmtpServer(String email) {
        String domain = email.substring(email.indexOf("@") + 1).toLowerCase();
        return smtpMap.getOrDefault(domain, new SmtpServer("smtp." + domain, 587, true, false));
    }

}
