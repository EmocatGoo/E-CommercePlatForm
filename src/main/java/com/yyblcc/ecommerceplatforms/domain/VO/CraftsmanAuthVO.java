package com.yyblcc.ecommerceplatforms.domain.VO;

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
    private List<String> proofImages;
    private List<String> masterpieceImages;

}
