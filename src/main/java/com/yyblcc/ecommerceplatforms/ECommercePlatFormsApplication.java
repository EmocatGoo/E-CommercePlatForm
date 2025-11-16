package com.yyblcc.ecommerceplatforms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ECommercePlatFormsApplication {

    public static void main(String[] args) {
        SpringApplication.run(ECommercePlatFormsApplication.class, args);
    }

}
