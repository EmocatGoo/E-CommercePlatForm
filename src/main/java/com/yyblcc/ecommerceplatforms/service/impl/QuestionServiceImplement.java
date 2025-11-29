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
import com.yyblcc.ecommerceplatforms.mapper.QuestionMapper;
import com.yyblcc.ecommerceplatforms.mapper.QuestionRecordMapper;
import com.yyblcc.ecommerceplatforms.service.QuestionService;
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
    public Result listQuestion(PageQuery query) {
        String key = "question:page:"+query.getPage()+":"+query.getPageSize();
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
        Page<Question> questions = questionMapper.selectPage(new Page<>(query.getPage(), query.getPageSize()),
                new LambdaQueryWrapper<Question>()
                        .like(query.getKeyword()!= null, Question::getTitle, query.getKeyword())
                        .orderByDesc(Question::getCreateTime));
        PageBean pageBean = new PageBean<>(questions.getTotal(), questions.getRecords());
        stringRedisTemplate.opsForValue().set(key, JSON.toJSONString(pageBean), Duration.ofMinutes(10));
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
//        Long userId = AuthContext.getUserId();
        //TODO 测试环境，记得删除
        Long userId = 4L;
        String hashKey = "quiz:question:"+userId;
        try{
            Map<Object, Object> userQuestionMap = stringRedisTemplate.opsForHash().entries(hashKey);
            if (MapUtil.isNotEmpty(userQuestionMap)){
                return Result.error("请勿重复开始 quiz");
            }
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
        List<Long> questionIds =questionMapper.selectList(new LambdaQueryWrapper<Question>()
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
        List<Long> randomIds = questionIds.subList(0, count);
        List<Question> questions = questionMapper.selectBatchIds(randomIds);
        List<QuestionVO> questionVOList = questions
                .stream()
                .map(this::convertToVO)
                .toList();
        Map<String, String> questionMap = questions.stream()
                .collect(Collectors.toMap(question -> question.getId().toString(), Question::getAnswer, (a, b) -> b));
        stringRedisTemplate.opsForHash().putAll(hashKey, questionMap);
        stringRedisTemplate.expire(hashKey, Duration.ofMinutes(10));
        return Result.success(questionVOList);
    }

    @Override
    public Result submitQuiz(List<QuestionRecordDTO> records) {
//        Long userId = AuthContext.getUserId();
        Long userId = 4L;
        String key = "quiz:question:"+userId;
        if (CollUtil.isEmpty(records)){
            return Result.error("未提交答题信息");
        }
        StringBuffer userSubmitAnswer = new StringBuffer();
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
        Map<Object, Object> cachedAnswers;
        try{
            cachedAnswers = stringRedisTemplate.opsForHash().entries(key);
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
        Map<Long,String> correctAnswers;
        if (MapUtil.isNotEmpty(cachedAnswers)){
            correctAnswers = cachedAnswers.entrySet().stream()
                    .collect(Collectors.toMap(
                            entry -> Long.valueOf(entry.getKey().toString()),
                            entry -> entry.getValue().toString()
                    ));
        }else{
            List<Question> questions = questionMapper.selectList(new LambdaQueryWrapper<Question>().in(Question::getId, questionIds));
            correctAnswers = questions.stream()
                    .collect(Collectors.toMap(Question::getId, Question::getAnswer));
        }
        Map<Long,String> userAnswers = records.stream()
                .collect(Collectors.toMap(QuestionRecordDTO::getQuestionId, QuestionRecordDTO::getAnswer));
        correctAnswers.forEach((questionId, answer) -> {
            String userAnswer = userAnswers.get(questionId);
            if (answer.equals(userAnswer)){
                correctCount.incrementAndGet();
            }
        });
        stringRedisTemplate.delete(key);
        QuestionResultVO questionResultVO = QuestionResultVO.builder()
                .questionSize(questionIds.size())
                .correctCount(correctCount.get())
                .build();
        return Result.success(questionResultVO);
    }

    private QuestionVO convertToVO(Question question){
        QuestionVO questionVO = new QuestionVO();
        BeanUtils.copyProperties(question, questionVO);
        return questionVO;
    }
}
