package com.yyblcc.ecommerceplatforms.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
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
@TableName("tb_category")
public class Category {
    //主键
    @TableId(type = IdType.AUTO)
    private Long id;
    //分类名称
    private String categoryName;
    //分类创建时间
    private LocalDateTime createTime;
    //分类更新时间
    private LocalDateTime updateTime;
}
