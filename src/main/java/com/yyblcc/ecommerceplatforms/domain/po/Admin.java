package com.yyblcc.ecommerceplatforms.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yyblcc.ecommerceplatforms.domain.Enum.RoleEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("tb_admin")
public class Admin {
    //主键
    @TableId(type = IdType.AUTO)
    private Long id;
    //管理员姓名
    private String name;
    //管理员账号
    private String username;
    //管理员密码
    private String password;
    //管理员权限
    private RoleEnum role;
    //管理员账号状态
    private Integer status;
    //注册时间
    private LocalDateTime createTime;
    //头像路径
    private String avatar;
    //是否注销账号
    @TableLogic
    private Integer isDeleted;
}
