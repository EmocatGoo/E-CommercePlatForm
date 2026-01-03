package com.yyblcc.ecommerceplatforms.domain.query;

import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
public class RefundQuery {
    private String orderSn;

    private Integer orderStatus;

    private Long craftsmanId;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime beginTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @Min(1)
    private Integer page;

    @Min(1)
    private Integer pageSize;
}
