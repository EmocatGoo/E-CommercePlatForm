package com.yyblcc.ecommerceplatforms.controller;

import com.alipay.easysdk.factory.Factory;
import com.alipay.easysdk.payment.common.models.AlipayTradeFastpayRefundQueryResponse;
import com.yyblcc.ecommerceplatforms.domain.po.Result;
import com.yyblcc.ecommerceplatforms.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pay")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping("/QR")
    public Result<String> QRCode(@RequestBody List<String> orderSn) throws InterruptedException {
        return paymentService.createPayment(orderSn);
    }

    @PostMapping("/status/query")
    public Result<?> queryStatus(@RequestBody List<String> orderSn) {
        return paymentService.queryPaymentStatus(orderSn);
    }

    @GetMapping("/repay")
    public Result<?> queryOrderIsPending(String paySn) {
        return null;
    }

    @PostMapping("/refund")
    public Result<String> refund(@RequestBody List<String> orderSn) throws Exception {
        return paymentService.refund(orderSn);
    }

    @GetMapping("/refundQuery")
    public AlipayTradeFastpayRefundQueryResponse refundQuery(String orderSn) throws Exception {
        return Factory.Payment.Common().queryRefund(orderSn, orderSn);
    }

    @PostMapping("/close")
    public Result<String> closeOrder(@RequestBody List<String> orderSn) {
        return paymentService.closeOrder(orderSn);
    }

}
