package com.english.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user_question_record")
public class UserQuestionRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long questionId;
    private Long knowledgeId;
    private String userAnswer;
    private Integer isCorrect;
    private Integer scoreGot;
    private Integer timeSpent;
    private LocalDateTime createTime;
}
