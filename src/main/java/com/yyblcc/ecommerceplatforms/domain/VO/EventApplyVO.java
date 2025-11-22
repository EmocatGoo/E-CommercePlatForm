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
public class EventApplyVO {
    private Long eventId;
    private String realName;
    private String phone;
    private Integer status;
    private LocalDateTime signTime;
}
