package com.yyblcc.ecommerceplatforms.util.chainofresponsibility;


import com.yyblcc.ecommerceplatforms.domain.po.User;

public interface VerifyHandler {
    CheckResult verify(User user);
    void setNextHandler(VerifyHandler nextHandler);
}
