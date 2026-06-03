package com.english.platform.algorithm;

import com.english.platform.entity.*;
import com.english.platform.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 自适应习题推荐算法
 *
 * 核心思路：
 * 1. 分析用户各知识点的正确率（weak_score）
 * 2. 优先推送正确率低的薄弱知识点题目
 * 3. 根据正确率动态调整题目难度：
 *    - 正确率 < 40%：降低难度（推简单题）
 *    - 正确率 40-70%：维持当前难度
 *    - 正确率 > 70%：提升难度（推难题）
 * 4. 排除近期已答过的题目，避免重复
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdaptiveRecommendService {

    private final UserKnowledgeStatMapper knowledgeStatMapper;
    private final UserQuestionRecordMapper questionRecordMapper;
    private final QuestionMapper questionMapper;
    private final QuestionOptionMapper optionMapper;
    private final UserMapper userMapper;

    /** 每次推荐题目数量 */
    private static final int RECOMMEND_COUNT = 10;
    /** 薄弱知识点数量（取前N个最薄弱的） */
    private static final int WEAK_KNOWLEDGE_LIMIT = 3;
    /** 近期答过的题目排除窗口（题数） */
    private static final int RECENT_EXCLUDE_LIMIT = 20;

    /**
     * 为用户生成自适应推荐题目列表
     *
     * @param userId 用户ID
     * @return 推荐题目列表（附带选项）
     */
    public List<Question> recommendQuestions(Long userId) {
        // 1. 获取用户等级
        User user = userMapper.selectById(userId);
        int userLevel = user != null ? user.getLevel() : 1;

        // 2. 获取最薄弱的知识点
        List<UserKnowledgeStat> weakStats = knowledgeStatMapper.selectWeakestKnowledge(userId, WEAK_KNOWLEDGE_LIMIT);

        // 3. 确定推荐难度
        int difficulty = calculateRecommendDifficulty(weakStats, userLevel);

        // 4. 获取近期已答题目ID（排除）
        List<Long> recentAnsweredIds = getRecentAnsweredQuestionIds(userId);

        // 5. 查询薄弱知识点相关题目
        List<Long> weakKnowledgeIds = weakStats.stream()
                .map(UserKnowledgeStat::getKnowledgeId)
                .collect(Collectors.toList());

        List<Question> questions = collectRecommendedQuestions(weakKnowledgeIds, difficulty, recentAnsweredIds);

        // 6. 填充选项
        fillOptions(questions);

        log.info("用户[{}]自适应推荐：难度={}, 薄弱知识点={}, 近期排除={}, 推荐题数={}",
                userId, difficulty, weakKnowledgeIds, recentAnsweredIds.size(), questions.size());
        return questions;
    }

    private List<Question> collectRecommendedQuestions(List<Long> weakKnowledgeIds, int targetDifficulty, List<Long> recentAnsweredIds) {
        LinkedHashMap<Long, Question> selected = new LinkedHashMap<>();
        LinkedHashSet<Long> excludeIds = new LinkedHashSet<>(recentAnsweredIds);
        List<Integer> fallbackDifficulties = buildDifficultyFallbacks(targetDifficulty);

        appendQuestions(selected, excludeIds, weakKnowledgeIds, List.of(targetDifficulty), RECOMMEND_COUNT);
        appendQuestions(selected, excludeIds, weakKnowledgeIds, fallbackDifficulties, RECOMMEND_COUNT);
        appendQuestions(selected, excludeIds, Collections.emptyList(), List.of(targetDifficulty), RECOMMEND_COUNT);
        appendQuestions(selected, excludeIds, Collections.emptyList(), fallbackDifficulties, RECOMMEND_COUNT);

        // 题库很小时，允许重新出最近做过的题，至少保证页面有题可练
        if (selected.size() < RECOMMEND_COUNT) {
            appendQuestions(selected, new LinkedHashSet<>(selected.keySet()), weakKnowledgeIds, fallbackDifficulties, RECOMMEND_COUNT);
            appendQuestions(selected, new LinkedHashSet<>(selected.keySet()), Collections.emptyList(), fallbackDifficulties, RECOMMEND_COUNT);
        }

        return new ArrayList<>(selected.values());
    }

    private void appendQuestions(Map<Long, Question> selected,
                                 Set<Long> excludeIds,
                                 List<Long> knowledgeIds,
                                 List<Integer> difficulties,
                                 int limit) {
        if (selected.size() >= limit || difficulties.isEmpty()) {
            return;
        }

        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<Question>()
                .in(Question::getDifficulty, difficulties);

        if (knowledgeIds != null && !knowledgeIds.isEmpty()) {
            wrapper.in(Question::getKnowledgeId, knowledgeIds);
        }
        if (excludeIds != null && !excludeIds.isEmpty()) {
            wrapper.notIn(Question::getId, excludeIds);
        }

        wrapper.last("ORDER BY RAND() LIMIT " + (limit - selected.size()));

        List<Question> candidates = questionMapper.selectList(wrapper);
        for (Question question : candidates) {
            if (!selected.containsKey(question.getId())) {
                selected.put(question.getId(), question);
                excludeIds.add(question.getId());
                if (selected.size() >= limit) {
                    break;
                }
            }
        }
    }

    private List<Integer> buildDifficultyFallbacks(int targetDifficulty) {
        List<Integer> difficulties = new ArrayList<>();
        difficulties.add(targetDifficulty);
        for (int diff = 1; diff <= 3; diff++) {
            if (!difficulties.contains(diff)) {
                difficulties.add(diff);
            }
        }
        return difficulties;
    }

    /**
     * 根据薄弱统计和用户等级计算推荐难度
     *
     * 算法：
     * - 计算薄弱知识点平均正确率
     * - 正确率 > 0.7 → 提升难度
     * - 正确率 < 0.4 → 降低难度
     * - 其他 → 维持与用户等级匹配的难度
     */
    private int calculateRecommendDifficulty(List<UserKnowledgeStat> weakStats, int userLevel) {
        if (weakStats.isEmpty()) {
            return userLevel; // 新用户按等级出题
        }

        double avgCorrectRate = weakStats.stream()
                .mapToDouble(s -> s.getCorrectRate() != null ? s.getCorrectRate().doubleValue() : 0.5)
                .average()
                .orElse(0.5);

        int baseDifficulty = userLevel; // 基础难度 = 用户等级
        if (avgCorrectRate > 0.70) {
            // 正确率高，提升难度，但不超过最大值3
            return Math.min(baseDifficulty + 1, 3);
        } else if (avgCorrectRate < 0.40) {
            // 正确率低，降低难度，但不低于1
            return Math.max(baseDifficulty - 1, 1);
        }
        return baseDifficulty;
    }

    /**
     * 获取用户近期答过的题目ID列表
     */
    private List<Long> getRecentAnsweredQuestionIds(Long userId) {
        // 查询最近答过的题目记录
        List<UserQuestionRecord> recentRecords = questionRecordMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserQuestionRecord>()
                        .eq(UserQuestionRecord::getUserId, userId)
                        .orderByDesc(UserQuestionRecord::getCreateTime)
                        .last("LIMIT " + RECENT_EXCLUDE_LIMIT)
        );
        return recentRecords.stream()
                .map(UserQuestionRecord::getQuestionId)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 为题目列表填充选项
     */
    private void fillOptions(List<Question> questions) {
        if (questions.isEmpty()) return;
        List<Long> questionIds = questions.stream().map(Question::getId).collect(Collectors.toList());
        List<QuestionOption> allOptions = optionMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<QuestionOption>()
                        .in(QuestionOption::getQuestionId, questionIds)
                        .orderByAsc(QuestionOption::getOptionKey)
        );
        Map<Long, List<QuestionOption>> optionMap = allOptions.stream()
                .collect(Collectors.groupingBy(QuestionOption::getQuestionId));
        questions.forEach(q -> q.setOptions(optionMap.getOrDefault(q.getId(), Collections.emptyList())));
    }

    /**
     * 更新用户知识点薄弱统计（每次答题后调用）
     *
     * @param userId      用户ID
     * @param knowledgeId 知识点ID
     * @param isCorrect   是否答对
     */
    public void updateKnowledgeStat(Long userId, Long knowledgeId, boolean isCorrect) {
        if (knowledgeId == null) return;

        UserKnowledgeStat stat = knowledgeStatMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserKnowledgeStat>()
                        .eq(UserKnowledgeStat::getUserId, userId)
                        .eq(UserKnowledgeStat::getKnowledgeId, knowledgeId)
        );

        if (stat == null) {
            stat = new UserKnowledgeStat();
            stat.setUserId(userId);
            stat.setKnowledgeId(knowledgeId);
            stat.setTotalCount(0);
            stat.setCorrectCount(0);
            stat.setWeakScore(50);
        }

        stat.setTotalCount(stat.getTotalCount() + 1);
        if (isCorrect) stat.setCorrectCount(stat.getCorrectCount() + 1);

        // 计算正确率
        double correctRate = (double) stat.getCorrectCount() / stat.getTotalCount();
        stat.setCorrectRate(java.math.BigDecimal.valueOf(correctRate));

        // 计算薄弱评分 (0-100)：正确率*100，越高越好，越低越薄弱
        // 使用加权平均：70%权重当前积累数据 + 30%本次表现，防止少量答题剧烈波动
        int newWeakScore = (int)(correctRate * 100);
        int oldWeakScore = stat.getWeakScore() != null ? stat.getWeakScore() : 50;
        // 题目少时，更多依赖历史数据；题目多时，更多依赖真实统计
        double weight = Math.min(0.7, (double) stat.getTotalCount() / 20);
        stat.setWeakScore((int)(weight * newWeakScore + (1 - weight) * oldWeakScore));

        if (stat.getId() == null) {
            knowledgeStatMapper.insert(stat);
        } else {
            knowledgeStatMapper.updateById(stat);
        }
    }
}
