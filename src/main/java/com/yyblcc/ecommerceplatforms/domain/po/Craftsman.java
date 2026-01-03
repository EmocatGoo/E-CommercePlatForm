package com.yyblcc.ecommerceplatforms.domain.po;

import com.baomidou.mybatisplus.annotation.*;
import com.yyblcc.ecommerceplatforms.annotation.Sensitive;
import com.yyblcc.ecommerceplatforms.domain.Enum.SensitiveType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_craftsman")
public class Craftsman {
    //主键
    @TableId(type = IdType.AUTO)
    private Long id;
    //匠人名称
    private String name;
    //匠人身份证号码
    @Sensitive(type = SensitiveType.ID_CARD)
    private String idNumber;
    //匠人用户名
    private String username;
    //匠人账号密码
    private String password;
    //匠人手机号码
    @Sensitive(type = SensitiveType.MOBILE_PHONE)
    private String phone;
    //匠人邮箱
    private String email;
    //个人简介
    private String introduction;
    //技艺类型
    private String technique;
    //认证状态
    private Integer reviewStatus;
    //认证表id
    private Long authId;
    //帐号状态
    private Integer status;
    //工作室名称
    private Long workshopId;
    //工作室名称
    @TableField(exist = false)
    private String workshopName;
    //拒绝原因
    private String rejectReason;
    //头像路径
    private String avatar;
    //创建时间
    private LocalDateTime createTime;
    //更新时间
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}
