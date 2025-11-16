package com.yyblcc.ecommerceplatforms.domain.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserStatusDTO {
    private Long userId;
    private Integer status;
    private String rejectReason;
}
