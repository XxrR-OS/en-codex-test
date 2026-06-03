package com.english.platform.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WordStudyDTO {
    @NotNull
    private Long wordId;
    /** 是否记住 1-记住 0-不认识 */
    private Integer mastered;
}
