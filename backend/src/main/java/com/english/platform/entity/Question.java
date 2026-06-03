package com.english.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("question")
public class Question {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    /** 题型 1-单选 2-多选 3-填空 4-翻译 */
    private Integer type;
    /** 难度 1-易 2-中 3-难 */
    private Integer difficulty;
    private Long knowledgeId;
    private String answer;
    private String analysis;
    private Integer score;
    private String source;
    private LocalDateTime createTime;

    /** 选项列表（非数据库字段） */
    @TableField(exist = false)
    private List<QuestionOption> options;
}
