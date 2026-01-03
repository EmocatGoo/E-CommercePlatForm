package com.yyblcc.ecommerceplatforms.domain.VO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventVO {
    private Long id;
    private String title;
    private String description;
    private String eventDetail;
    private String coverImage;
    private Integer status;
    private Integer eventType;
    private Integer maxPeople;
    private Integer peopleNum;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime createTime;
    private String location;

    private Boolean userApplied;
}
