package com.yyblcc.ecommerceplatforms.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
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
@TableName("tb_payment")
public class Payment {
    @TableId(type = IdType.AUTO)
    private Long id;
    //支付单号
    private String mergePaySn;
    //支付用户id
    private Long userId;
    //合计支付金额
    private BigDecimal totalAmount;
    //子订单数量
    private Integer orderCount;
    //支付状态
    private Integer payStatus;
    //支付创建时间
    private LocalDateTime createTime;
    //支付完成时间
    private LocalDateTime payTime;
    //支付过期时间
    private LocalDateTime expireTime;
    //交易号
    private String tradeNo;

    @TableLogic
    private Integer isDeleted;
}
