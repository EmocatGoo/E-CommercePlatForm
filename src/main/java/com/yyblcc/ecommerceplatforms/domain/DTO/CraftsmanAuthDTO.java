package com.yyblcc.ecommerceplatforms.domain.DTO;

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
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class CraftsmanAuthDTO {
    @NotBlank
    private Long craftsmanId;

    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    @NotBlank(message = "身份证号不能为空")
    private String idNumber;

    @NotBlank(message = "手机号码不能为空")
    private String phone;

    @NotBlank(message = "邮箱不能为空")
    private String email;

    @NotBlank(message = "技艺分类不能为空")
    private String technique;

    @NotBlank(message = "个人简介不能为空")
    private String introduction;

    @NotBlank(message = "手持身份证照为必传项")
    private String handleCard;

    @NotBlank(message = "身份证正面必传")
    private String idCardFront;

    @NotBlank(message = "身份证反面必传")
    private String idCardBack;

    @NotEmpty(message = "请至少上传三张佐证材料照片")
    @Size(min = 3, message = "请至少上传三张佐证材料照片")
    private List<String> proofImages;

    @NotEmpty(message = "请至少上传三张代表作品图片")
    @Size(min = 3, message = "请至少上传三张代表作品图片")
    private List<String> masterpieceImages;

}
