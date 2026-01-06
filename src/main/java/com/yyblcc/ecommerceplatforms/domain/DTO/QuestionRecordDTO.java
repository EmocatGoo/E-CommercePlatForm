package com.yyblcc.ecommerceplatforms.domain.DTO;

import lombok.Data;

import java.util.List;

@Data
public class QuestionRecordDTO {
    private Long userId;
    private Long questionId;
    private String answer;
}
