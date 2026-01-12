package com.yyblcc.ecommerceplatforms.domain.DTO;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserPageGroupDTO {
    private String orderGroupSn;

    private LocalDateTime lastestTime;
}
