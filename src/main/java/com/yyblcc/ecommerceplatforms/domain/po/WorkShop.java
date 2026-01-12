package com.yyblcc.ecommerceplatforms.domain.po;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.yyblcc.ecommerceplatforms.domain.VO.VideoVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@TableName(value = "tb_workshop",autoResultMap = true)
public class WorkShop {
    @TableId(type = IdType.AUTO)
    //主键，匠人工作室id
    private Long id;
    //匠人id
    private Long craftsmanId;

    //工作室名称
    private String workshopName;

    //工作室描述
    private String description;

    //工作室地址
    private String location;

    //工作室logo
    private String workshopLogo;

    //工作室封面
    private String coverImage;

    //技艺介绍
    private String techniqueIntro;

    //短视频
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<VideoVO> shortVideos;

    //创作故事
    private String story;

    //工作室创建时间
    private LocalDateTime createTime;

    //申请后的审核状态
    private Integer reviewStatus;

    //工作室状态
    private Integer status;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> masterpieceCollection;

    //访问量
    private Long visitCount;

    //收藏量
    private Integer collectionCount;

    @TableLogic
    private Integer isDeleted;
}
