package com.yyblcc.ecommerceplatforms.domain.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderStatsuDTO {
    @NotNull
    private Integer status;
    @NotBlank
    private String paySn;
    private String expressNo;
    private String expressCompany;
}
