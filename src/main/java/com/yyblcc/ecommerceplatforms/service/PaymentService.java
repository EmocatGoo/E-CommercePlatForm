package com.yyblcc.ecommerceplatforms.service;



import com.baomidou.mybatisplus.extension.service.IService;
import com.yyblcc.ecommerceplatforms.domain.po.Order;
import com.yyblcc.ecommerceplatforms.domain.po.Payment;
import com.yyblcc.ecommerceplatforms.domain.po.Result;

import java.math.BigInteger;
import java.util.List;

public interface PaymentService extends IService<Payment> {

    public Result<String> createPayment(List<String> orderSn) throws InterruptedException;

    public Result<String> queryPaymentStatus(List<String> orderSn);

    public void pendingOrder(Long userId, Order order);

    public boolean isOrderAlreadyPending(Long userId);

    public Result<String> refund(List<String> orderSn);


    Result<String> closeOrder(List<String> orderSn);
}
