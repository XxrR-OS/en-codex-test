package com.english.platform.controller;

import com.english.platform.common.Result;
import com.english.platform.service.AiQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * AI智能出题接口
 */
@RestController
@RequestMapping("/api/ai/question")
@RequiredArgsConstructor
public class AiQuestionController {

    private final AiQuestionService aiQuestionService;

    /**
     * AI智能生成题目并保存到数据库
     * POST /api/ai/question/generate?knowledgeId=2&difficulty=2&count=5
     */
    @PostMapping("/generate")
    public Result<String> generateQuestions(
            @RequestParam Long knowledgeId,
            @RequestParam(defaultValue = "2") Integer difficulty,
            @RequestParam(defaultValue = "5") Integer count) {
        int saved = aiQuestionService.generateAndSave(knowledgeId, difficulty, count);
        return Result.success("成功生成并保存 " + saved + " 道题目");
    }
}
