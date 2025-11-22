package com.yyblcc.ecommerceplatforms.domain.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 活动发布，修改DTO
 */
@Data
public class EventDTO {

    private Long id;

    @NotBlank(message = "标题不能为空")
    private String title;

    private String coverImage;

    private String eventDetail;

    private String description;

    @NotBlank(message = "活动类型不能为空")
    // 0线上活动 1线下活动
    private Integer eventType;

    private Integer status;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String location;

    private Integer maxPeople;

    private Long organizerId;
}
