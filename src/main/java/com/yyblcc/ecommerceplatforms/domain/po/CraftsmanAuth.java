package com.yyblcc.ecommerceplatforms.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_craftsman_auth")
public class CraftsmanAuth {
    @TableId(type = IdType.AUTO)
    private Long id;
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
    private List<String> proofImages;
    private List<String> masterpieceImages;

}
