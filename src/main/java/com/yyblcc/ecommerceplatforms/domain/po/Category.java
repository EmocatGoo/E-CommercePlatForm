package com.yyblcc.ecommerceplatforms.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("tb_category")
public class Category {
    //主键
    @TableId(type = IdType.AUTO)
    private Long id;
    //分类名称
    @NotBlank
    private String categoryName;
    @NotBlank
    //分类标签
    private Integer tag;
    //分类描述
    private String description;
    //分类创建时间
    private LocalDateTime createTime;
    //分类更新时间
    private LocalDateTime updateTime;

    //分类逻辑删除标识
    @TableLogic
    private Integer isDeleted;
}
