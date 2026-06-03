package com.english.platform.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class EssayResultVO {
    private Long id;
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
