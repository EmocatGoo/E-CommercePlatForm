package com.yyblcc.ecommerceplatforms.domain.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EventApplyDTO {
    @NotNull
    private Long eventId;
    private String realName;
    @NotBlank
    private String phone;
    private String idCard;
}
