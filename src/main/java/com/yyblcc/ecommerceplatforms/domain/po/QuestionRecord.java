package com.yyblcc.ecommerceplatforms.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("tb_quiz_record")
public class QuestionRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Long> questionIds;
    private String answer;
    private LocalDateTime createTime;
}
