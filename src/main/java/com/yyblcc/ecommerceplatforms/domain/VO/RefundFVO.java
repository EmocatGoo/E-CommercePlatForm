package com.yyblcc.ecommerceplatforms.domain.VO;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RefundFVO {
    private String paySn;
    private List<RefundVO> refundVOList;
}
