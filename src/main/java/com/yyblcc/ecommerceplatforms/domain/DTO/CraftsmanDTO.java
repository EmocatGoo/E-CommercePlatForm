package com.yyblcc.ecommerceplatforms.domain.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CraftsmanDTO {
    private String name;
    private String username;
    private String password;
    private String email;
    private String idNumber;
    private String skillCategory;
    private String phone;
    private String bio;
    private String avatar;
    private LocalDate updateTime;
}
