package com.yyblcc.ecommerceplatforms.domain.VO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class EventApplyVO {
    //活动申请部分
    private Long eventId;
    private String realName;
    private String phone;
    private String idCard;
    private Integer status;
    private LocalDateTime signTime;
    //活动内容部分
    private String description;
    private String title;
    private Integer eventType;
    private String location;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String coverImage;
    private Integer eventStatus;

}
