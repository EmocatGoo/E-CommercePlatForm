package com.yyblcc.ecommerceplatforms.controller;

import com.alipay.easysdk.factory.Factory;
import com.alipay.easysdk.payment.common.models.AlipayTradeFastpayRefundQueryResponse;
import com.yyblcc.ecommerceplatforms.domain.DTO.RefundDTO;
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
    public Result<String> QRCode(@RequestBody String orderGroupSn) throws InterruptedException {
        return paymentService.createPayment(orderGroupSn);
    }

    @PostMapping("/status/query")
    public Result<?> queryStatus(@RequestBody String orderGroupSn) {
        return paymentService.queryPaymentStatus(orderGroupSn);
    }

    @GetMapping("/repay")
    public Result<?> queryOrderIsPending(String paySn) {
        return null;
    }

    /*
    退款单个商品
     */
    @PostMapping("/refund")
    public Result<String> refund(@RequestBody RefundDTO dto) throws Exception {
        return paymentService.refund(dto);
    }

    @GetMapping("/refundQuery")
    public AlipayTradeFastpayRefundQueryResponse refundQuery(String orderSn) throws Exception {
        return Factory.Payment.Common().queryRefund(orderSn, orderSn);
    }

    @PostMapping("/close")
    public Result<String> closeOrder(@RequestBody String orderGroupSn) {
        return paymentService.closeOrder(orderGroupSn);
    }

}
