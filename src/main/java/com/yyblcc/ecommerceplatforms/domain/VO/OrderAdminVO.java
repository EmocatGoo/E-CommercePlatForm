package com.yyblcc.ecommerceplatforms.domain.VO;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderAdminVO {
    private Long id;
    // 订单号
    private String orderSn;
    // 买家ID
    private Long userId;
    // 买家手机号
    private String userPhone;
    // 匠人ID
    private Long craftsmanId;
    // 匠人姓名
    private String craftsmanName;
    // 匠人手机号
    private String craftsmanPhone;
    // 订单总金额
    private BigDecimal totalAmount;
    // 匠人实收
    private BigDecimal craftsmanAmount;
    // 订单状态（中文：待付款/已完成）
    private Integer orderStatus;
    // 支付状态
    private Integer payStatus;
    // 支付方式
    private Integer paymentMethod;
    // 支付时间
    private LocalDateTime payTime;
    // 收货人
    private String consignee;
    // 收货手机号（脱敏）
    private String phone;
    // 完整收货地址
    private String shippingAddress;
    // 物流公司
    private String expressCompany;
    // 物流单号（可点击查）
    private String expressNo;
    // 下单时间
    private LocalDateTime createTime;
    private String remark;
    // 订单明细
    private List<OrderItemAdminVO> items;
}
