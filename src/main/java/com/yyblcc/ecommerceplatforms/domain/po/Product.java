package com.yyblcc.ecommerceplatforms.domain.po;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author YuYiBlackcat
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName(value = "tb_product",autoResultMap = true)
public class Product {
    //主键
    @TableId(type = IdType.AUTO)
    private Long id;

    //商品名称
    private String productName;

    //商品价格
    private BigDecimal price;

    //商品描述
    private String description;

    //匠人id
    private Long craftsmanId;

    //分类id
    private Long categoryId;

    //商品上架下架状态
    private Integer status;

    //商品库存
    private Integer stock;

    //商品审核状态
    private Integer reviewStatus;

    //商品拒绝原因
    private String rejectReason;

    //商品图片
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> imageUrl;

    //商品文化背景
    private String culturalBackground;

    //点赞数
    private Integer likeCount;

    //收藏数
    private Integer favoriteCount;

    //售卖数量
    private Integer saleCount;

    //创建时间
    private LocalDateTime createTime;

    //更新时间
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;

}
