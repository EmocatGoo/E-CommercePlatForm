package com.yyblcc.ecommerceplatforms.domain.po;

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
@TableName("tb_product_comment_like")
public class ProductCommentLike {
    private Long id;
    private Long productId;
    private Long commentId;
    private Long userId;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    private Integer status;

    @TableLogic
    private Integer isDeleted;
}
