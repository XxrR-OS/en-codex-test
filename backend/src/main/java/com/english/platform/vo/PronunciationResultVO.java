package com.english.platform.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class PronunciationResultVO {
    private Long id;
    private BigDecimal totalScore;
    private BigDecimal accuracyScore;
    private BigDecimal fluencyScore;
    private List<WordScoreVO> wordScores;
    private String feedback;

    @Data
    public static class WordScoreVO {
        private String word;
        private BigDecimal score;
        private String status; // correct/wrong/missing
    }
}
