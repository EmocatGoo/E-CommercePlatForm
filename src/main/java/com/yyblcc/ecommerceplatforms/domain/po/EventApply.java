package com.yyblcc.ecommerceplatforms.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yyblcc.ecommerceplatforms.annotation.Sensitive;
import com.yyblcc.ecommerceplatforms.domain.Enum.SensitiveType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("tb_event_apply")
public class EventApply {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long eventId;
    private Long userId;
    private String realName;
    @Sensitive(type = SensitiveType.MOBILE_PHONE)
    private String phone;
    @Sensitive(type = SensitiveType.ID_CARD)
    private String idCard;
    private Integer status;
    private LocalDateTime signTime;
    private LocalDateTime createTime;
    @TableLogic
    private Integer isDeleted;
}
