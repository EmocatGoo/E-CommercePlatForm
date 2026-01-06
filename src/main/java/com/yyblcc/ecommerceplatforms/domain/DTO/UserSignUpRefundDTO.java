package com.yyblcc.ecommerceplatforms.domain.DTO;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class UserSignUpRefundDTO {
    private String paySn;
    private List<Long> productIds;
    private String refundReason;
    //具体退款说明
    private String refundDesc;
    //具体退款商品图片（用户收货后查看）
    private List<String> refundImage;
    //1.仅退款 2.退货退款
    private Integer refundType;
}
