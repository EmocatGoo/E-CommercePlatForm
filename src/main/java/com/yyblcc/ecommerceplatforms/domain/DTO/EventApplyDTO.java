package com.yyblcc.ecommerceplatforms.domain.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EventApplyDTO {
    @NotBlank
    private Long eventId;
    private String realName;
    @NotBlank
    private String phone;
    private String idNumber;
}
