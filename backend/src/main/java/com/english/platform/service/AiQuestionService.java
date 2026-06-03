package com.english.platform.service;

public interface AiQuestionService {
    /** AI智能生成题目并保存到数据库 */
    int generateAndSave(Long knowledgeId, Integer difficulty, Integer count);
}
