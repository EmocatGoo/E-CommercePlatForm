package com.yyblcc.ecommerceplatforms.domain.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OrderReviewDTO {
    @NotBlank
    private String orderSn;
    @NotBlank
    private Integer status;

    private String rejectReason;
}
