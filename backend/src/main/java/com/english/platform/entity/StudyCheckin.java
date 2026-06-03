package com.english.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@TableName("study_checkin")
public class StudyCheckin {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private LocalDate checkinDate;
    private Integer wordCount;
    private Integer questionCount;
    private Integer scoreGot;
    private Integer studyMinutes;
}
