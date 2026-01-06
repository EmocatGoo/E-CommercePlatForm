package com.yyblcc.ecommerceplatforms.domain.VO;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OrderItemCraftsmanVO {
    private Long productId;
    private String productName;
    private String productImage;
    private Integer quantity;               // 买了几件
    private BigDecimal price;               // 下单单价
    private BigDecimal craftsmanAmount;     // 这件匠人实收
    private Integer isCommented;            // 是否已评价（提醒回复）
    private String commentContent;
}
