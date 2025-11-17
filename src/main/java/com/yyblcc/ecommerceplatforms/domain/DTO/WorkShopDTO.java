package com.yyblcc.ecommerceplatforms.domain.DTO;

import com.yyblcc.ecommerceplatforms.domain.VO.VideoVO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WorkShopDTO {
    //工作室名称
    @NotBlank
    private String workshopName;
    //工作室描述
    private String description;
    //工作室地址
    private String location;
    //工作室logo
    private String workshopLogo;

    private String coverImage;

    @NotBlank
    private String story;
    @NotBlank
    private String techniqueIntro;

    private List<VideoVO> shorVideos;

}
