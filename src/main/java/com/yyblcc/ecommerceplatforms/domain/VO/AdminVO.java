package com.yyblcc.ecommerceplatforms.domain.VO;

import com.yyblcc.ecommerceplatforms.domain.Enum.RoleEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdminVO {
    private Long id;
    private String username;
    private String name;
    private String avatar;
    private RoleEnum role;
    private String token;  // SaToken 令牌，用于前端认证
}
