package com.yyblcc.ecommerceplatforms.domain.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRegisterDTO {
    private String username;
    private String phone;
    private String password;
    private String email;
}
