package com.yyblcc.ecommerceplatforms.domain.VO;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderUserVO {
    private Long orderId;
    private BigDecimal totalAmount;
    private List<String> orderSn;
    private String orderGroupSn;
    private Integer orderStatus;
    private LocalDateTime createTime;
    private LocalDateTime payTime;
    private Integer paymentMethod;
    private String consignee;
    private String shippingAddress;
    private String phone;
    private String expressCompany;
    private String expressNo;

    private List<OrderItemUserVO> items;
}
