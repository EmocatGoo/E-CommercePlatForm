package com.yyblcc.ecommerceplatforms.domain.DTO;

import lombok.Data;

import java.util.List;

@Data
public class RefundDTO {
    //订单号
    private String orderSn;

    //商品id
    private Long productId;

    //退款商品数量
    private Integer refundProductCount;
}
