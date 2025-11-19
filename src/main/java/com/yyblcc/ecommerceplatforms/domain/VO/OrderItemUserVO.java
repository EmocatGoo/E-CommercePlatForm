package com.yyblcc.ecommerceplatforms.domain.VO;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OrderItemUserVO {
    private Long productId;
    private String productName;
    private String productImage;
    private Integer quantity;
    private BigDecimal price;
    private Integer isCommented;
    private Integer refundStatus;
}
