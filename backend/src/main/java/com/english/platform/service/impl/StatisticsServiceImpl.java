package com.english.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.english.platform.entity.*;
import com.english.platform.mapper.*;
import com.english.platform.service.StatisticsService;
import com.english.platform.util.UserContext;
import com.english.platform.vo.StatisticsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final UserWordRecordMapper wordRecordMapper;
    private final UserQuestionRecordMapper questionRecordMapper;
    private final UserKnowledgeStatMapper knowledgeStatMapper;
    private final KnowledgePointMapper knowledgePointMapper;
    private final StudyCheckinMapper checkinMapper;
    private final UserMapper userMapper;

    @Override
    public StatisticsVO getOverview() {
        Long userId = UserContext.getUserId();
        StatisticsVO vo = new StatisticsVO();

        // 1. 单词统计
        Long totalWords = wordRecordMapper.selectCount(
                new LambdaQueryWrapper<UserWordRecord>().eq(UserWordRecord::getUserId, userId));
        Long masteredWords = wordRecordMapper.selectCount(
                new LambdaQueryWrapper<UserWordRecord>()
                        .eq(UserWordRecord::getUserId, userId)
                        .eq(UserWordRecord::getMastered, 1));
        vo.setTotalWords(totalWords.intValue());
        vo.setMasteredWords(masteredWords.intValue());

        // 2. 答题统计
        Long totalQuestions = questionRecordMapper.selectCount(
                new LambdaQueryWrapper<UserQuestionRecord>().eq(UserQuestionRecord::getUserId, userId));
        Long correctQuestions = questionRecordMapper.selectCount(
                new LambdaQueryWrapper<UserQuestionRecord>()
                        .eq(UserQuestionRecord::getUserId, userId)
                        .eq(UserQuestionRecord::getIsCorrect, 1));
        vo.setTotalQuestions(totalQuestions.intValue());
        vo.setCorrectQuestions(correctQuestions.intValue());
        // vo.setCorrectRate(totalQuestions > 0
        //         ? (double) Math.round((double) correctQuestions / totalQuestions * 100)
        //         : 0.0);

        // 正确率计算（修复版，0错误）
        double correctRate = 0.0;
        if (totalQuestions != null && totalQuestions > 0) {
            correctRate = (double) Math.round((double) correctQuestions / totalQuestions * 100 * 100) / 100;
        }
        vo.setCorrectRate(correctRate);

        // 3. 用户积分
        User user = userMapper.selectById(userId);
        vo.setTotalScore(user != null && user.getTotalScore() != null ? user.getTotalScore() : 0);

        // 4. 连续打卡天数
        Integer continuousDays = checkinMapper.selectContinuousDays(userId);
        vo.setContinuousDays(continuousDays == null ? 0 : continuousDays);

        // 5. 近7天学习数据（合并单词和答题）
        List<Map<String, Object>> wordWeekly = wordRecordMapper.selectWeeklyWordStats(userId);
        List<Map<String, Object>> qWeekly = questionRecordMapper.selectWeeklyQuestionStats(userId);

        // 生成近7天日期列表
        List<Map<String, Object>> weeklyData = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (int i = 6; i >= 0; i--) {
            String date = LocalDate.now().minusDays(i).format(fmt);
            Map<String, Object> day = new HashMap<>();
            day.put("date", date);
            // 单词数
            int wCount = wordWeekly.stream()
                    .filter(m -> date.equals(String.valueOf(m.get("studyDate"))))
                    .mapToInt(m -> ((Number) m.getOrDefault("wordCount", 0)).intValue())
                    .sum();
            // 答题数和积分
            int qCount = 0, score = 0;
            for (Map<String, Object> m : qWeekly) {
                if (date.equals(String.valueOf(m.get("studyDate")))) {
                    qCount = ((Number) m.getOrDefault("total", 0)).intValue();
                    score = ((Number) m.getOrDefault("totalScore", 0)).intValue();
                }
            }
            day.put("wordCount", wCount);
            day.put("questionCount", qCount);
            day.put("score", score);
            weeklyData.add(day);
        }
        vo.setWeeklyData(weeklyData);

        // 6. 薄弱知识点
        List<UserKnowledgeStat> weakStats = knowledgeStatMapper.selectWeakestKnowledge(userId, 5);
        List<Map<String, Object>> weakPoints = weakStats.stream().map(stat -> {
            KnowledgePoint kp = knowledgePointMapper.selectById(stat.getKnowledgeId());
            Map<String, Object> m = new HashMap<>();
            m.put("knowledgeId", stat.getKnowledgeId());
            m.put("knowledgeName", kp != null ? kp.getName() : "未知知识点");
            m.put("correctRate", stat.getCorrectRate());
            m.put("totalCount", stat.getTotalCount());
            m.put("weakScore", stat.getWeakScore());
            return m;
        }).collect(Collectors.toList());
        vo.setWeakPoints(weakPoints);

        return vo;
    }
}
