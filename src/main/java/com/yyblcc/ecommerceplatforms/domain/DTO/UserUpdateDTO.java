package com.yyblcc.ecommerceplatforms.domain.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserUpdateDTO {
    private String name;
    private String email;
    private String phone;
    private String idNumber;
    private String avatar;
}
