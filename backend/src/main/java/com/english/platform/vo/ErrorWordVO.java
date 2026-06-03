package com.english.platform.vo;

import lombok.Data;

@Data
public class ErrorWordVO {
    private Long wordId;
    private String word;
    private String phonetic;
    private String translation;
    private String category;
    private Integer wrongCount;
    private Integer studyCount;
}
