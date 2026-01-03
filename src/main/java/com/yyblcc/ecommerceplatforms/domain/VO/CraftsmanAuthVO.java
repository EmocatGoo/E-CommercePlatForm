package com.yyblcc.ecommerceplatforms.domain.VO;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CraftsmanAuthVO {
    private Long craftsmanId;
    private String realName;
    private String idNumber;
    private String phone;
    private String email;
    private String technique;
    private String introduction;
    private String handleCard;
    private String idCardFront;
    private String idCardBack;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> proofImages;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> masterpieceImages;
}
