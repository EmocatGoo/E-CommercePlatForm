package com.yyblcc.ecommerceplatforms.domain.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("tb_cart_item")
public class CartItem {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("cart_id")
    private Long cartId;

    @TableField("user_id")
    private Long userId;

    @TableField("product_id")
    private Long productId;

    @TableField("craftsman_id")
    private Long craftsmanId;

    private String productName;

    private String productImage;

    private BigDecimal price;

    private Integer stock;

    private Integer quantity;

    @TableField("is_checked")
    private Integer checked;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

}
