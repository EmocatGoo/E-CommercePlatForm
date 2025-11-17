package com.yyblcc.ecommerceplatforms.domain.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("tb_user_collect")
public class UserCollect {
    private Long id;
    private Long userId;
    private Long workShopId;
    private Integer status;
}
