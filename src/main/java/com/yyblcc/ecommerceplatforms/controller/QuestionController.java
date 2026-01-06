package com.yyblcc.ecommerceplatforms.controller;

import com.yyblcc.ecommerceplatforms.domain.DTO.QuestionDTO;
import com.yyblcc.ecommerceplatforms.domain.DTO.QuestionRecordDTO;
import com.yyblcc.ecommerceplatforms.domain.po.Question;
import com.yyblcc.ecommerceplatforms.domain.po.QuestionRecord;
import com.yyblcc.ecommerceplatforms.domain.po.Result;
import com.yyblcc.ecommerceplatforms.domain.query.PageQuery;
import com.yyblcc.ecommerceplatforms.domain.query.QuestionQuery;
import com.yyblcc.ecommerceplatforms.service.QuestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Delete;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/question")
@RequiredArgsConstructor
@Slf4j
public class QuestionController {
    private final QuestionService questionService;

    @GetMapping("/page")
    public Result page(QuestionQuery query) {
        log.info("条件分页查询信息:{}", query);
        return questionService.listQuestion(query);
    }

    @PostMapping()
    public Result create(@RequestBody @Validated QuestionDTO questionDTO) {
        log.info("添加新题目信息:{}", questionDTO);
        return questionService.createQuestion(questionDTO);
    }

    @PutMapping
    public Result update(@RequestBody @Validated Question question) {
        log.info("更新题目信息:{}", question);
        return questionService.updateQuestion(question);
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        log.info("删除题目信息:{}", id);
        return questionService.removeQuestionById(id);
    }

    @DeleteMapping("/batch")
    public Result deleteBatch(@RequestBody @Validated List<Long> ids) {
        log.info("批量删除题目信息:{}", ids);
        return questionService.batchDeleteQuestions(ids);
    }

    @GetMapping("/start")
    public Result startQuiz(@RequestParam Integer count,@RequestParam Long categoryId){
        log.info("开始 quiz,题目数量：{}", count);
        return questionService.startQuiz(count,categoryId);
    }

    @PostMapping("/submit")
    public Result submitQuiz(@RequestBody List<QuestionRecordDTO> records){
        log.info("提交 quiz,题目数量：{}", records.size());
        return questionService.submitQuiz(records);
    }
}
