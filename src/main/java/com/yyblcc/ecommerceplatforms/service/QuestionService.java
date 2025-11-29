package com.yyblcc.ecommerceplatforms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yyblcc.ecommerceplatforms.domain.DTO.QuestionDTO;
import com.yyblcc.ecommerceplatforms.domain.DTO.QuestionRecordDTO;
import com.yyblcc.ecommerceplatforms.domain.po.Question;
import com.yyblcc.ecommerceplatforms.domain.po.QuestionRecord;
import com.yyblcc.ecommerceplatforms.domain.po.Result;
import com.yyblcc.ecommerceplatforms.domain.query.PageQuery;

import java.util.List;

public interface QuestionService extends IService<Question> {
    Result listQuestion(PageQuery query);

    Result createQuestion(QuestionDTO questionDTO);

    Result updateQuestion(Question question);

    Result batchDeleteQuestions(List<Long> ids);

    Result removeQuestionById(Long id);

    Result startQuiz(Integer count, Long categoryId);

    Result submitQuiz(List<QuestionRecordDTO> records);
}
