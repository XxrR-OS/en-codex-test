package com.english.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("user_knowledge_stat")
public class UserKnowledgeStat {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long knowledgeId;
    private Integer totalCount;
    private Integer correctCount;
    private BigDecimal correctRate;
    /** 薄弱评分，越低越薄弱，用于自适应推荐 */
    private Integer weakScore;
    private LocalDateTime updateTime;
}
