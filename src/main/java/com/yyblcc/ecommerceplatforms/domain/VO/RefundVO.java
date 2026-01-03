package com.yyblcc.ecommerceplatforms.domain.VO;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundVO {
    private String refundSn;
    private String orderSn;
    private String paySn;
    private Long userId;
    private Long craftsmanId;
    private String craftsmanName;
    private String productName;
    private String productImage;
    private Integer quantity;
    private BigDecimal refundAmount;
    private String applyReason;
    private String applyDesc;
    private List<String> applyImages;

    private Integer refundStatus;
    private Integer refundType;
    private Integer refundChannel;
    private Integer isGoodsReturned;
    private String returnExpressCompany;
    private String returnExpressNo;

    private LocalDateTime agreeTime;
    private String refuseReason;
    private LocalDateTime refundTime;

    private Long platformHandleBy;
    private LocalDateTime createTime;

}
