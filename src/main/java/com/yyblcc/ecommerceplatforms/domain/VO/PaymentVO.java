package com.yyblcc.ecommerceplatforms.domain.VO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentVO {
    private String paymentSn;
    private String qrCodeBase64;
}
