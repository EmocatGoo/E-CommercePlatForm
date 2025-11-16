package com.yyblcc.ecommerceplatforms.domain.DTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminDTO {
    private String username;
    private String avatar;
    private String name;
}
