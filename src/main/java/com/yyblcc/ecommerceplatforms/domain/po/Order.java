package com.yyblcc.ecommerceplatforms.domain.po;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("tb_orders")
public class Order {
    //主键
    private Long id;
    //订单号
    private String orderSn;
    //下单用户id
    private Long userId;
    //匠人id
    private Long craftsmanId;
    //订单状态 0待支付 1待发货 2待收货 3待评价 4退款/售后
    private Integer orderStatus;
    //支付状态 0未支付 1已支付 2已取消
    private Integer payStatus;
    //订单总金额
    private BigDecimal totalAmount;
    //匠人实际收入
    private BigDecimal craftsmanAmount;
    //平台抽成（默认每件商品单价5% 累计）
    private BigDecimal platformCommission;
    //支付方式 0支付宝 1微信支付 2余额支付
    private Integer payMethod;
    //支付时间
    private LocalDateTime payTime;
    //收货地址
    private String shippingAddress;
    //收货人
    private String consignee;
    //收货人手机号码
    private String phone;
    //物流公司名称
    private String expressCompany;
    //物流号
    private String expressNo;
    //订单结单时间
    private LocalDateTime checkoutTime;
    //订单取消时间
    private LocalDateTime cancelTime;
    //订单取消原因
    private String cancelReason;
    //备注
    private String remark;
    //是否评论
    private Integer isCommented;
    //订单创建时间
    private LocalDateTime createTime;
    //订单更新时间
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;

}
