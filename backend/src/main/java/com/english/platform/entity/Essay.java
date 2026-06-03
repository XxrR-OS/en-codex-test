package com.english.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("essay")
public class Essay {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String topic;
    private String content;
    private BigDecimal totalScore;
    private BigDecimal grammarScore;
    private BigDecimal contentScore;
    private BigDecimal structureScore;
    private BigDecimal vocabularyScore;
    private String feedback;
    private String correction;
    private LocalDateTime createTime;
}
