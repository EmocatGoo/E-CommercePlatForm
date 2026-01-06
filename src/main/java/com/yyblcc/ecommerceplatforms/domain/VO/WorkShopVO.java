package com.yyblcc.ecommerceplatforms.domain.VO;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.yyblcc.ecommerceplatforms.domain.po.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
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
    private LocalDateTime createTime;

    // 匠人信息（关联查询）
    private String craftsmanName;
    private String craftsmanPhone;
    private String craftsmanEmail;


    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<VideoVO> shortVideos;
    // 作品集
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> masterpieceCollection;
    private Long visitCount;
    private Integer collectionCount;

    // 商品（关联查询）
    private List<ProductVO> products;
}
