package com.english.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("pronunciation_record")
public class PronunciationRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String wordText;
    private String audioUrl;
    private BigDecimal totalScore;
    private BigDecimal accuracyScore;
    private BigDecimal fluencyScore;
    /** 单词级别得分，JSON格式存储 */
    private String wordScores;
    private String feedback;
    private LocalDateTime createTime;
}
