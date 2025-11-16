package com.yyblcc.ecommerceplatforms.domain.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WorkShopDTO {
    //工作室名称
    private String workshopName;
    //工作室描述
    private String description;
    //工作室地址
    private String location;
    //工作室logo
    private String workshopLogo;
    //工作室创建时间
    private LocalDateTime createTime;
}
