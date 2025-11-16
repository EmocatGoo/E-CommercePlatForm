package com.yyblcc.ecommerceplatforms.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("tb_workshop")
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
    //工作室状态
    private Integer status;
    //工作室地址
    private String location;
    //工作室logo
    private String workshopLogo;
    //工作室创建时间
    private LocalDateTime createTime;
    //申请后的审核状态
    private Integer reviewStatus;

    @TableLogic
    private Integer isDeleted;
}
