package com.yyblcc.ecommerceplatforms.domain.VO;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderCraftsmanVO {
    private String orderSn;                 // 订单号
    private String userNickname;            // 买家昵称（脱敏：苏*匠）


    private BigDecimal craftsmanAmount;     // 匠人实收金额（最关心！）
    private BigDecimal totalAmount;         // 订单总金额（参考）

    private Integer orderStatus;             // 状态：待发货/已完成
    private LocalDateTime payTime;          // 支付时间（知道啥时候有钱）

    private String consignee;               // 收货人
    private String phone;                   // 收货人手机号（脱敏：138****1234）
    private String shippingAddress;         // 完整地址

    private String expressCompany;          // 物流公司
    private String expressNo;               // 物流单号（自己填）

    private LocalDateTime createTime;
    private String remark;                  // 用户备注（很重要！用户可能说“送礼”）

    private List<OrderItemCraftsmanVO> items;
}
