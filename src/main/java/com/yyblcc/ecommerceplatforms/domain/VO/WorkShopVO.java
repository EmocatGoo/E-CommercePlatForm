package com.yyblcc.ecommerceplatforms.domain.VO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WorkShopVO {
    private Long id;
    private String workshopName;
    private String craftsmanName;
    private String craftsmanPhone;
    private String craftsmanEmail;
    private String location;
    private String workshopLogo;
    private Integer status;
    private Integer reviewStatus;
}
