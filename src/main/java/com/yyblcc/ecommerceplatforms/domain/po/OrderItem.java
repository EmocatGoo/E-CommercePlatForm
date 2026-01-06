package com.yyblcc.ecommerceplatforms.domain.po;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.github.yulichang.annotation.Table;
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
@TableName(value = "tb_order_item")
public class OrderItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    //订单表id
    private Long orderId;
    //订单号
    private String orderSn;
    //买家id
    private Long userId;
    //匠人id
    private Long craftsmanId;
    //商品id
    private Long productId;
    //商品名称
    private String productName;
    //商品图片
    private String productImage;
    //商品单价
    private BigDecimal price;
    //商品数量
    private Integer quantity;
    //买家实付总价
    private BigDecimal totalAmount;
    //匠人实际收入
    private BigDecimal craftsmanAmount;
    //平台抽成
    private BigDecimal platformCommission;
    //是否评价
    private Integer isCommented;
    //评价表id
    private Long commentId;
    //退款状态
    private Integer refundStatus;
    //退款金额
    private BigDecimal refundAmount;
    //创建时间
    private LocalDateTime createTime;
    //更新时间
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;

}
