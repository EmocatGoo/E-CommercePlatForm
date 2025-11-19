package com.yyblcc.ecommerceplatforms.domain.VO;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class CreateOrderVO {
    private String orderSn;
    private BigDecimal totalAmount;
    private LocalDateTime createTime;
}
