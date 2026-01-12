package com.yyblcc.ecommerceplatforms.service;



import com.baomidou.mybatisplus.extension.service.IService;
import com.yyblcc.ecommerceplatforms.domain.DTO.RefundDTO;
import com.yyblcc.ecommerceplatforms.domain.po.Order;
import com.yyblcc.ecommerceplatforms.domain.po.Payment;
import com.yyblcc.ecommerceplatforms.domain.po.Result;

import java.math.BigInteger;
import java.util.List;

public interface PaymentService extends IService<Payment> {

    Result<String> createPayment(String orderGroupSn) throws InterruptedException;

    Result<String> queryPaymentStatus(String orderGroupSn);

    void pendingOrder(Long userId, Order order);

    boolean isOrderAlreadyPending(Long userId);

    Result<String> refund(RefundDTO dto);

    Result<String> closeOrder(String orderGroupSn);

}
