package com.english.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.english.platform.algorithm.AdaptiveRecommendService;
import com.english.platform.common.PageResult;
import com.english.platform.dto.AnswerDTO;
import com.english.platform.entity.KnowledgePoint;
import com.english.platform.entity.Question;
import com.english.platform.entity.QuestionOption;
import com.english.platform.entity.StudyCheckin;
import com.english.platform.entity.User;
import com.english.platform.entity.UserQuestionRecord;
import com.english.platform.exception.BusinessException;
import com.english.platform.mapper.KnowledgePointMapper;
import com.english.platform.mapper.QuestionMapper;
import com.english.platform.mapper.QuestionOptionMapper;
import com.english.platform.mapper.StudyCheckinMapper;
import com.english.platform.mapper.UserMapper;
import com.english.platform.mapper.UserQuestionRecordMapper;
import com.english.platform.service.QuestionService;
import com.english.platform.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private final QuestionMapper questionMapper;
    private final QuestionOptionMapper optionMapper;
    private final UserQuestionRecordMapper recordMapper;
    private final UserMapper userMapper;
    private final StudyCheckinMapper checkinMapper;
    private final AdaptiveRecommendService adaptiveService;
    private final KnowledgePointMapper knowledgePointMapper;

    @Override
    public List<Question> getAdaptiveQuestions() {
        Long userId = UserContext.getUserId();
        return adaptiveService.recommendQuestions(userId);
    }

    @Override
    @Transactional
    public Map<String, Object> submitAnswer(AnswerDTO dto) {
        Long userId = UserContext.getUserId();
        Question question = questionMapper.selectById(dto.getQuestionId());
        if (question == null) {
            throw new BusinessException("题目不存在");
        }

        boolean correct = question.getAnswer().trim().equalsIgnoreCase(
                dto.getUserAnswer() == null ? "" : dto.getUserAnswer().trim());
        int scoreGot = correct ? (question.getScore() == null ? 10 : question.getScore()) : 0;

        UserQuestionRecord record = new UserQuestionRecord();
        record.setUserId(userId);
        record.setQuestionId(dto.getQuestionId());
        record.setKnowledgeId(question.getKnowledgeId());
        record.setUserAnswer(dto.getUserAnswer());
        record.setIsCorrect(correct ? 1 : 0);
        record.setScoreGot(scoreGot);
        record.setTimeSpent(dto.getTimeSpent());
        recordMapper.insert(record);

        if (scoreGot > 0) {
            User current = userMapper.selectById(userId);
            User user = new User();
            user.setId(userId);
            user.setTotalScore((current.getTotalScore() == null ? 0 : current.getTotalScore()) + scoreGot);
            userMapper.updateById(user);
        }

        adaptiveService.updateKnowledgeStat(userId, question.getKnowledgeId(), correct);
        updateCheckin(userId, scoreGot);

        Map<String, Object> result = new HashMap<>();
        result.put("correct", correct);
        result.put("correctAnswer", question.getAnswer());
        result.put("analysis", question.getAnalysis());
        result.put("scoreGot", scoreGot);
        return result;
    }

    @Override
    public PageResult<Map<String, Object>> getWrongQuestions(Integer page, Integer size) {
        Long userId = UserContext.getUserId();
        int offset = (page - 1) * size;
        List<Map<String, Object>> list = recordMapper.selectWrongQuestions(userId, offset, size);
        Long total = recordMapper.countWrongQuestions(userId);
        return PageResult.of(total, list);
    }

    @Override
    public List<Question> getByKnowledge(Long knowledgeId, Integer size) {
        List<Question> questions = questionMapper.selectList(
                new LambdaQueryWrapper<Question>()
                        .eq(Question::getKnowledgeId, knowledgeId)
                        .last("ORDER BY RAND() LIMIT " + size)
        );
        fillOptions(questions);
        return questions;
    }

    @Override
    public List<Question> getByCategory(String category, Integer size) {
        List<KnowledgePoint> points = knowledgePointMapper.selectList(
                new LambdaQueryWrapper<KnowledgePoint>()
                        .eq(KnowledgePoint::getCategory, category)
        );
        if (points.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> knowledgeIds = points.stream()
                .map(KnowledgePoint::getId)
                .toList();
        List<Question> questions = questionMapper.selectList(
                new LambdaQueryWrapper<Question>()
                        .in(Question::getKnowledgeId, knowledgeIds)
                        .last("ORDER BY RAND() LIMIT " + size)
        );
        fillOptions(questions);
        return questions;
    }

    private void fillOptions(List<Question> questions) {
        if (questions.isEmpty()) {
            return;
        }
        List<Long> ids = questions.stream().map(Question::getId).toList();
        List<QuestionOption> opts = optionMapper.selectList(
                new LambdaQueryWrapper<QuestionOption>().in(QuestionOption::getQuestionId, ids));
        Map<Long, LinkedHashMap<String, QuestionOption>> map = new HashMap<>();
        opts.forEach(option -> map
                .computeIfAbsent(option.getQuestionId(), key -> new LinkedHashMap<>())
                .putIfAbsent(option.getOptionKey() + "::" + option.getOptionValue(), option));
        questions.forEach(question -> {
            LinkedHashMap<String, QuestionOption> optionMap = map.get(question.getId());
            if (optionMap == null) {
                question.setOptions(Collections.emptyList());
            } else {
                question.setOptions(new ArrayList<>(optionMap.values()));
            }
        });
    }

    private void updateCheckin(Long userId, int scoreGot) {
        LocalDate today = LocalDate.now();
        StudyCheckin checkin = checkinMapper.selectOne(
                new LambdaQueryWrapper<StudyCheckin>()
                        .eq(StudyCheckin::getUserId, userId)
                        .eq(StudyCheckin::getCheckinDate, today)
        );
        if (checkin == null) {
            checkin = new StudyCheckin();
            checkin.setUserId(userId);
            checkin.setCheckinDate(today);
            checkin.setWordCount(0);
            checkin.setQuestionCount(1);
            checkin.setScoreGot(scoreGot);
            checkin.setStudyMinutes(0);
            checkinMapper.insert(checkin);
        } else {
            checkin.setQuestionCount(checkin.getQuestionCount() + 1);
            checkin.setScoreGot(checkin.getScoreGot() + scoreGot);
            checkinMapper.updateById(checkin);
        }
    }
}
