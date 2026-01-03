package com.yyblcc.ecommerceplatforms.domain.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName(value = "tb_order_refund", autoResultMap = true)
public class Refund {
    @TableId
    private Long id;

    private String refundSn;
    private Long orderId;
    private String orderSn;
    private Long orderItemId;
    private Long userId;
    private Long craftsmanId;
    private Long productId;
    private String productName;
    private String productImage;
    private Integer quantity;
    private BigDecimal refundAmount;
    private String applyReason;
    private String applyDesc;
    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
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
    private LocalDateTime updateTime;

    private String paySn;

    @TableLogic
    private Integer isDeleted;

}
