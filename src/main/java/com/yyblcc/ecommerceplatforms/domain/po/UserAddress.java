package com.yyblcc.ecommerceplatforms.domain.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("tb_user_address")
public class UserAddress {
    //主键
    @TableId(type = IdType.AUTO)
    private Long id;
    //用户id
    private Long userId;
    //收货人姓名
    private String receiverName;
    //收货人手机号
    private String receiverPhone;
    //省份
    private String province;
    //市
    private String city;
    //区/县
    private String district;
    //详细地址
    private String detailAddress;
    //是否为默认地址
    private Integer isDefault;
    //创建时间
    private LocalDateTime createTime;
    //更新时间
    private LocalDateTime updateTime;

    @TableLogic
    //逻辑删除
    private Integer isDeleted;
}
