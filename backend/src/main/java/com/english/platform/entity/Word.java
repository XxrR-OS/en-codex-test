package com.english.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("word")
public class Word {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String word;
    private String phonetic;
    private String translation;
    private String example;
    private String exampleTrans;
    private Integer level;
    private String category;
    private String audioUrl;
    private LocalDateTime createTime;
}
