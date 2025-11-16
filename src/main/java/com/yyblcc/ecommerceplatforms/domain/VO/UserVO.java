package com.yyblcc.ecommerceplatforms.domain.VO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserVO {
    private Long userId;
    private String username;
    private String nickname;
    private String phone;
    private String email;
    private Long defaultAddressId;
    private Integer sex;
    private String avatar;
    private Integer status;
    private String rejectReason;
    private LocalDateTime createTime;
}
