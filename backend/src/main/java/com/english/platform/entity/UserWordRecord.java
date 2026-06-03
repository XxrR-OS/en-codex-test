package com.english.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user_word_record")
public class UserWordRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long wordId;
    private Integer studyCount;
    private Integer correctCount;
    private Integer wrongCount;
    private Integer mastered;
    private LocalDateTime nextReview;
    private LocalDateTime lastStudy;
}
