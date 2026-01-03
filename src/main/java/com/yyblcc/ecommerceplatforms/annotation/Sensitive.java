package com.yyblcc.ecommerceplatforms.annotation;

import com.yyblcc.ecommerceplatforms.domain.Enum.SensitiveType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Sensitive {
    SensitiveType type();
}

