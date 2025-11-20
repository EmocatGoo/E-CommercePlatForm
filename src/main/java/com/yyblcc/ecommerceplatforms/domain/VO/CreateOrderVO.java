package com.yyblcc.ecommerceplatforms.domain.VO;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CreateOrderVO {
    private List<String> orderSn;
    private BigDecimal totalAmount;
    private LocalDateTime createTime;
}
