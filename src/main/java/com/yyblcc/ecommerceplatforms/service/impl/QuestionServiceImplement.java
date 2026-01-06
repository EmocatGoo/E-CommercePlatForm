package com.yyblcc.ecommerceplatforms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yyblcc.ecommerceplatforms.annotation.UpdateBloomFilter;
import com.yyblcc.ecommerceplatforms.domain.DTO.QuestionDTO;
import com.yyblcc.ecommerceplatforms.domain.DTO.QuestionRecordDTO;
import com.yyblcc.ecommerceplatforms.domain.VO.QuestionResultVO;
import com.yyblcc.ecommerceplatforms.domain.VO.QuestionVO;
import com.yyblcc.ecommerceplatforms.domain.po.PageBean;
import com.yyblcc.ecommerceplatforms.domain.po.Question;
import com.yyblcc.ecommerceplatforms.domain.po.QuestionRecord;
import com.yyblcc.ecommerceplatforms.domain.po.Result;
import com.yyblcc.ecommerceplatforms.domain.query.PageQuery;
import com.yyblcc.ecommerceplatforms.domain.query.QuestionQuery;
import com.yyblcc.ecommerceplatforms.mapper.QuestionMapper;
import com.yyblcc.ecommerceplatforms.mapper.QuestionRecordMapper;
import com.yyblcc.ecommerceplatforms.service.QuestionService;
import com.yyblcc.ecommerceplatforms.util.StpKit;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestionServiceImplement extends ServiceImpl<QuestionMapper, Question> implements QuestionService {
    private final QuestionMapper questionMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final QuestionRecordMapper questionRecordMapper;
    @Override
    public Result listQuestion(QuestionQuery query) {
        boolean isCondition = query.getKeyword() != null && !query.getKeyword().isEmpty() || query.getQuestionType() != null;
        String key = "question:page:"+query.getPage()+":"+query.getPageSize();
        if (!isCondition){
            try{
                String jsonStr = stringRedisTemplate.opsForValue().get(key);
                if (jsonStr != null){
                    if (jsonStr.isEmpty()){
                        return Result.success();
                    }
                    return Result.success(JSON.parseObject(jsonStr, PageBean.class));
                }
            } catch (RuntimeException e) {
                throw new RuntimeException(e);
            }
        }
        Page<Question> questions = questionMapper.selectPage(new Page<>(query.getPage(), query.getPageSize()),
                new LambdaQueryWrapper<Question>()
                        .eq(query.getQuestionType() != null, Question::getQuestionType, query.getQuestionType())
                        .like(query.getKeyword()!= null, Question::getTitle, query.getKeyword())
                        .orderByAsc(Question::getCreateTime));
        PageBean pageBean = new PageBean<>(questions.getTotal(), questions.getRecords());
        if (!isCondition){
            stringRedisTemplate.opsForValue().set(key, JSON.toJSONString(pageBean), Duration.ofMinutes(10));
        }
        return Result.success(pageBean);
    }

    @Override
    @UpdateBloomFilter
    public Result createQuestion(QuestionDTO questionDTO) {
        Question question = new Question();
        if (questionDTO == null){
            return Result.error("参数错误");
        }
        BeanUtils.copyProperties(questionDTO, question);
        question.setOptions(JSON.toJSONString(questionDTO.getOptions()));
        question.setCreateTime(LocalDateTime.now());
        int row = questionMapper.insert(question);
        if (row > 0){
            try{
                stringRedisTemplate.keys("question:page:*").forEach(stringRedisTemplate::delete);
            } catch (RuntimeException e) {
                throw new RuntimeException(e);
            }
            return Result.success("添加成功");
        }
        return Result.error("添加失败");
    }

    @Override
    public Result updateQuestion(Question question) {
        if (question == null){
            return Result.error("参数错误");
        }
        int row = questionMapper.updateById(question);
        if (row > 0){
            stringRedisTemplate.keys("question:page:*").forEach(stringRedisTemplate::delete);
            return Result.success();
        }
        return Result.error("修改失败");
    }

    @Override
    public Result batchDeleteQuestions(List<Long> ids) {
        if (ids == null || ids.isEmpty()){
            return Result.error("参数错误");
        }
        int row = questionMapper.deleteBatchIds(ids);
        if (row > 0){
            stringRedisTemplate.keys("question:page:*").forEach(stringRedisTemplate::delete);
            return Result.success();
        }
        return Result.error("删除失败");
    }

    @Override
    public Result removeQuestionById(Long id) {
        if (id == null){
            return Result.error("参数错误");
        }
        int row = questionMapper.deleteById(id);
        if (row > 0){
            stringRedisTemplate.keys("question:page:*").forEach(stringRedisTemplate::delete);
            return Result.success();
        }
        return Result.error("删除失败");
    }

    @Override
    public Result startQuiz(Integer count, Long categoryId) {
        Long userId = StpKit.USER.getLoginIdAsLong();
        if (userId == null) {
            return Result.error("用户未登录");
        }
        List<Long> questionIds = questionMapper.selectList(new LambdaQueryWrapper<Question>()
                        .eq(Question::getCategoryId, categoryId))
                .stream()
                .map(Question::getId)
                .collect(Collectors.toCollection(ArrayList::new));
        if (CollUtil.isEmpty(questionIds)){
            return Result.error("未找到题目列表");
        }
        Collections.shuffle(questionIds);
        if (questionIds.size() < count){
            count = questionIds.size();
        }
        List<QuestionVO> questionVOs = questionMapper.selectBatchIds(questionIds.subList(0, count)).stream()
                .map(this::convertToVO)
                .toList();
        return Result.success(questionVOs);
    }

    @Override
    public Result submitQuiz(List<QuestionRecordDTO> records) {
        Long userId = StpKit.USER.getLoginIdAsLong();
        StringBuilder userSubmitAnswer = new StringBuilder();
        for (int i = 0; i < records.size(); i++) {
            QuestionRecordDTO record = records.get(i);
            if (!userId.equals(record.getUserId())) {
                throw new RuntimeException("用户信息不一致");
            }
            userSubmitAnswer.append(record.getAnswer());
            if (i < records.size() - 1) {
                userSubmitAnswer.append(",");
            }
        }
        List<Long> questionIds = records.stream()
                .map(QuestionRecordDTO::getQuestionId)
                .toList();
        QuestionRecord record = QuestionRecord.builder()
                .userId(userId)
                .questionIds(questionIds)
                .answer(userSubmitAnswer.toString())
                .createTime(LocalDateTime.now())
                .build();
        questionRecordMapper.insert(record);
        AtomicInteger correctCount = new AtomicInteger();

        Map<Long,String> correctAnswers;
        List<Question> questions = questionMapper.selectList(new LambdaQueryWrapper<Question>().in(Question::getId, questionIds));
        correctAnswers = questions.stream()
                .collect(Collectors.toMap(Question::getId, Question::getAnswer));
        Map<Long,String> userAnswers = records.stream()
                .collect(Collectors.toMap(QuestionRecordDTO::getQuestionId, QuestionRecordDTO::getAnswer));
        correctAnswers.forEach((questionId, answer) -> {
            String userAnswer = userAnswers.get(questionId);
            if (answer.equals(userAnswer)){
                correctCount.incrementAndGet();
            }
        });
        Map<String,String> correctAnswersVO = new HashMap<>();
        correctAnswers.forEach((questionId, answer) -> {
            Question question = questionMapper.selectOne(new LambdaQueryWrapper<Question>().eq(Question::getId, questionId));
            correctAnswersVO.put(question.getTitle(), question.getAnswer());
        });
        Map<String,String> userAnswersVO = new HashMap<>();
        records.forEach(item -> {
            Question question = questionMapper.selectOne(new LambdaQueryWrapper<Question>().eq(Question::getId, item.getQuestionId()));
            userAnswersVO.put(question.getTitle(), item.getAnswer());
        });
        QuestionResultVO questionResultVO = QuestionResultVO.builder()
                .questionSize(questionIds.size())
                .correctCount(correctCount.get())
                .correctAnswersVO(correctAnswersVO)
                .userAnswersVO(userAnswersVO)
                .build();
        return Result.success(questionResultVO);
    }

    private QuestionVO convertToVO(Question question){
        QuestionVO questionVO = new QuestionVO();
        BeanUtils.copyProperties(question, questionVO);
        return questionVO;
    }
}
