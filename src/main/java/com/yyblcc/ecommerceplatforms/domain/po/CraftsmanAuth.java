package com.yyblcc.ecommerceplatforms.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.yyblcc.ecommerceplatforms.annotation.Sensitive;
import com.yyblcc.ecommerceplatforms.domain.Enum.SensitiveType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TableName(value = "tb_craftsman_auth", autoResultMap = true)
public class CraftsmanAuth {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long craftsmanId;

    private String realName;
    @Sensitive(type = SensitiveType.ID_CARD)
    private String idNumber;
    @Sensitive(type = SensitiveType.MOBILE_PHONE)
    private String phone;
    private String email;
    private String technique;
    private String introduction;
    private String handleCard;
    private String idCardFront;
    private String idCardBack;
    @TableField(value = "proof_images",
            typeHandler = JacksonTypeHandler.class)
    private List<String> proofImages;
    @TableField(value = "masterpiece_images",
            typeHandler = JacksonTypeHandler.class)
    private List<String> masterpieceImages;

    private LocalDateTime createTime;
    private Integer status;
    private String refuseReason;
    private String updateTime;
}
