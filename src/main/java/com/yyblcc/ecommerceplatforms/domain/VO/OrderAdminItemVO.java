package com.yyblcc.ecommerceplatforms.domain.VO;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OrderAdminItemVO {
    //商品id
    private Long productId;
    //商品名称
    private String productName;
    //商品图片
    private String productImage;
    //数量
    private Integer quantity;
    //结账时的单价
    private BigDecimal price;
    //小计
    private BigDecimal totalAmount;
    //匠人实收
    private BigDecimal craftsmanAmount;
    //退款状态
    private Integer refundStatus;
}
