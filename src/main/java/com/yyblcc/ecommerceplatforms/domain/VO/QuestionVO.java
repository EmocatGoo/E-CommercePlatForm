package com.yyblcc.ecommerceplatforms.domain.VO;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuestionVO {
    private Long id;
    private String title;
    private String options;
    private Integer questionType;
}
