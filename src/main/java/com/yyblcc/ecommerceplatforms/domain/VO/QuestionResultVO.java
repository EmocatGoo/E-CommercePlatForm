package com.yyblcc.ecommerceplatforms.domain.VO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionResultVO {
    private Integer questionSize;
    private Integer correctCount;
    private Map<String,String> userAnswersVO;
    private Map<String,String> correctAnswersVO;
}
