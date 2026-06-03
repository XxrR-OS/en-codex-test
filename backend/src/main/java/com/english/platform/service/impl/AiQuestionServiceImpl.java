package com.english.platform.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.english.platform.ai.TongyiAiClient;
import com.english.platform.entity.KnowledgePoint;
import com.english.platform.entity.Question;
import com.english.platform.entity.QuestionOption;
import com.english.platform.exception.BusinessException;
import com.english.platform.mapper.KnowledgePointMapper;
import com.english.platform.mapper.QuestionMapper;
import com.english.platform.mapper.QuestionOptionMapper;
import com.english.platform.service.AiQuestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiQuestionServiceImpl implements AiQuestionService {

    private final TongyiAiClient tongyiClient;
    private final QuestionMapper questionMapper;
    private final QuestionOptionMapper optionMapper;
    private final KnowledgePointMapper knowledgePointMapper;

    @Override
    @Transactional
    public int generateAndSave(Long knowledgeId, Integer difficulty, Integer count) {
        KnowledgePoint kp = knowledgePointMapper.selectById(knowledgeId);
        if (kp == null) throw new BusinessException("知识点不存在");

        // 调用通义千问生成题目
        String aiResponse = tongyiClient.generateQuestions(kp.getName(), difficulty, count);
        log.info("AI出题响应: {}", aiResponse);

        JSONArray questions;
        try {
            int start = aiResponse.indexOf('[');
            int end = aiResponse.lastIndexOf(']');
            if (start >= 0 && end > start) {
                aiResponse = aiResponse.substring(start, end + 1);
            }
            questions = JSON.parseArray(aiResponse);
        } catch (Exception e) {
            log.error("解析AI出题结果失败", e);
            throw new BusinessException("AI出题结果解析失败");
        }

        int saved = 0;
        for (int i = 0; i < questions.size(); i++) {
            JSONObject qJson = questions.getJSONObject(i);
            try {
                Question q = new Question();
                q.setTitle(qJson.getString("title"));
                q.setType(1); // 单选题
                q.setDifficulty(difficulty);
                q.setKnowledgeId(knowledgeId);
                q.setAnswer(qJson.getString("answer"));
                q.setAnalysis(qJson.getString("analysis"));
                q.setScore(10);
                q.setSource("AI");
                questionMapper.insert(q);

                // 保存选项
                JSONArray options = qJson.getJSONArray("options");
                if (options != null) {
                    for (int j = 0; j < options.size(); j++) {
                        JSONObject optJson = options.getJSONObject(j);
                        QuestionOption opt = new QuestionOption();
                        opt.setQuestionId(q.getId());
                        opt.setOptionKey(optJson.getString("key"));
                        opt.setOptionValue(optJson.getString("value"));
                        optionMapper.insert(opt);
                    }
                }
                saved++;
            } catch (Exception e) {
                log.warn("保存AI题目失败: {}", qJson, e);
            }
        }
        return saved;
    }
}
