package com.yyblcc.ecommerceplatforms.domain.DTO;

import com.yyblcc.ecommerceplatforms.domain.po.OptionItem;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class QuestionDTO {
    @NotBlank
    private String title;
    private List<OptionItem> options;
    @NotBlank
    private String answer;
    private Integer questionType;

    private Long categoryId;
}
