package com.english.platform.controller;

import com.english.platform.common.PageResult;
import com.english.platform.common.Result;
import com.english.platform.dto.AnswerDTO;
import com.english.platform.entity.Question;
import com.english.platform.service.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/question")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @GetMapping("/adaptive")
    public Result<List<Question>> getAdaptiveQuestions() {
        return Result.success(questionService.getAdaptiveQuestions());
    }

    @PostMapping("/answer")
    public Result<Map<String, Object>> submitAnswer(@RequestBody @Valid AnswerDTO dto) {
        return Result.success(questionService.submitAnswer(dto));
    }

    @GetMapping("/wrong")
    public Result<PageResult<Map<String, Object>>> getWrongQuestions(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(questionService.getWrongQuestions(page, size));
    }

    @GetMapping("/by-knowledge")
    public Result<List<Question>> getByKnowledge(
            @RequestParam Long knowledgeId,
            @RequestParam(defaultValue = "5") Integer size) {
        return Result.success(questionService.getByKnowledge(knowledgeId, size));
    }

    @GetMapping("/by-category")
    public Result<List<Question>> getByCategory(
            @RequestParam String category,
            @RequestParam(defaultValue = "8") Integer size) {
        return Result.success(questionService.getByCategory(category, size));
    }
}
