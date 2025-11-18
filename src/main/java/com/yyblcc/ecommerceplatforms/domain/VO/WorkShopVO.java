package com.yyblcc.ecommerceplatforms.domain.VO;

import com.yyblcc.ecommerceplatforms.domain.po.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WorkShopVO {
    private Long id;
    private String workshopName;
    private String workshopLogo;
    private String coverImage;
    private String description;
    private String story;
    private String techniqueIntro;
    private String location;
    private Integer status;
    private Integer reviewStatus;

    // 匠人信息（关联查询）
    private String craftsmanName;
    private String craftsmanPhone;
    private String craftsmanEmail;

    // 数据
    private List<VideoVO> shortVideos;
    private Long visitCount;
    private Integer collectionCount;
    // 当前用户是否收藏
    private Boolean collected;

    // 商品（关联查询）
    private List<Product> products;
}
