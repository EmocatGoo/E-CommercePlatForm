package com.yyblcc.ecommerceplatforms.domain.DTO;

import lombok.Data;

@Data
public class CraftsmanReviewDTO {
    private Long authId;
    private Long craftsmanId;
    private Integer status;
    private String rejectReason;
}
