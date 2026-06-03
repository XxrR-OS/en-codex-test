package com.english.platform.service;

import com.english.platform.common.PageResult;
import com.english.platform.dto.AnswerDTO;
import com.english.platform.entity.Question;
import java.util.List;
import java.util.Map;

public interface QuestionService {
    /** 自适应推荐题目 */
    List<Question> getAdaptiveQuestions();
    /** 提交答案，返回结果 */
    Map<String, Object> submitAnswer(AnswerDTO dto);
    /** 错题本（分页） */
    PageResult<Map<String, Object>> getWrongQuestions(Integer page, Integer size);
    /** 按知识点获取题目 */
    List<Question> getByKnowledge(Long knowledgeId, Integer size);
    /** 按题库分类获取题目 */
    List<Question> getByCategory(String category, Integer size);
}
