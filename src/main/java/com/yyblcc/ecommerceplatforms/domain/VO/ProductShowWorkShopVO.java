package com.yyblcc.ecommerceplatforms.domain.VO;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class ProductShowWorkShopVO {
    private Long workshopId;
    private String workshopName;
    private String workshopLogo;
    private Long visitCount;
    private Integer collectionCount;
    private List<String> images;
}
