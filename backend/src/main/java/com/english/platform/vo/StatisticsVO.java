package com.english.platform.vo;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class StatisticsVO {
    private Integer totalWords;       // 学习单词总数
    private Integer masteredWords;    // 已掌握单词数
    private Integer totalQuestions;   // 答题总数
    private Integer correctQuestions; // 正确题数
    private Double correctRate;       // 整体正确率
    private Integer totalScore;       // 总积分
    private Integer continuousDays;   // 连续打卡天数
    private List<Map<String, Object>> weeklyData;    // 近7天学习数据
    private List<Map<String, Object>> weakPoints;    // 薄弱知识点列表
}
