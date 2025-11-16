package com.yyblcc.ecommerceplatforms.domain.VO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CraftsmanVO {
    private Long id;
    private String username;
    private String name;
    private String phone;
    private String avatar;
    private String introduction;
    private String email;
    private String workshopName;
    private Integer reviewStatus;
    private Integer status;
    private LocalDateTime createTime;
}
