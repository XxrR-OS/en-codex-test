package com.english.platform.controller;

import com.english.platform.common.PageResult;
import com.english.platform.common.Result;
import com.english.platform.dto.WordStudyDTO;
import com.english.platform.entity.Word;
import com.english.platform.service.WordService;
import com.english.platform.vo.ErrorWordVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 单词学习模块接口
 */
@RestController
@RequestMapping("/api/word")
@RequiredArgsConstructor
public class WordController {

    private final WordService wordService;

    /**
     * 获取今日推荐单词（基于艾宾浩斯遗忘曲线）
     * GET /api/word/today
     */
    @GetMapping("/today")
    public Result<List<Word>> getTodayWords() {
        return Result.success(wordService.getTodayWords());
    }

    /**
     * 分页查询单词库
     * GET /api/word/list?level=1&category=CET4&page=1&size=15
     */
    @GetMapping("/list")
    public Result<PageResult<Word>> listWords(
            @RequestParam(required = false) Integer level,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "15") Integer size) {
        return Result.success(wordService.listWords(level, category, page, size));
    }

    /**
     * 搜索单词
     * GET /api/word/search?keyword=xxx
     */
    @GetMapping("/search")
    public Result<List<Word>> searchWords(@RequestParam String keyword) {
        return Result.success(wordService.searchWords(keyword));
    }

    /**
     * 提交单词学习记录
     * POST /api/word/study
     */
    @PostMapping("/study")
    public Result<Void> studyWord(@RequestBody @Valid WordStudyDTO dto) {
        wordService.studyWord(dto);
        return Result.success();
    }

    /**
     * 获取错题本
     * GET /api/word/errors
     */
    @GetMapping("/errors")
    public Result<List<ErrorWordVO>> getErrorWords() {
        return Result.success(wordService.getErrorWords());
    }

    /**
     * 获取已学习单词
     * GET /api/word/learned
     */
    @GetMapping("/learned")
    public Result<List<ErrorWordVO>> getLearnedWords() {
        return Result.success(wordService.getLearnedWords());
    }

    /**
     * 获取已掌握单词
     * GET /api/word/mastered
     */
    @GetMapping("/mastered")
    public Result<List<ErrorWordVO>> getMasteredWords() {
        return Result.success(wordService.getMasteredWords());
    }

    /**
     * 从错题本删除（清除该单词的错误记录）
     * DELETE /api/word/errors/{wordId}
     */
    @DeleteMapping("/errors/{wordId}")
    public Result<Void> deleteErrorWord(@PathVariable Long wordId) {
        wordService.deleteErrorWord(wordId);
        return Result.success();
    }
}
