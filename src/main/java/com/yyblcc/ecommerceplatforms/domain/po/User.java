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
@TableName("tb_user")
public class User {
    //主键
    @TableId(type = IdType.AUTO)
    private Long id;
    //默认收货地址id
    private Long defaultAddressId;
    //用户名
    private String username;
    //密码
    private String password;
    //用户昵称
    private String nickname;
    //用户头像路径
    private String avatar;
    //用户邮箱
    private String email;
    //用户手机号码
    private String phone;
    //用户身份证号码
    private String idNumber;
    //用户姓名
    private String name;
    //用户性别
    private Integer sex;
    //用户账号状态
    private Integer status;
    //账号封禁原因
    private String rejectReason;
    //账号注册时间
    private LocalDateTime createTime;

    @TableLogic
    //逻辑删除
    private Integer isDeleted;
}
