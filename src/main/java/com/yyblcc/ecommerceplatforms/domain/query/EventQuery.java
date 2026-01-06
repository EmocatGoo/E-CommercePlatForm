package com.yyblcc.ecommerceplatforms.domain.query;

import jakarta.validation.constraints.Min;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventQuery {
    private String title;
    private Integer eventType;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    @Min(1)
    private Integer page;
    @Min(1)
    private Integer pageSize;
}
