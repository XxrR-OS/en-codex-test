package com.english.platform.controller;

import com.english.platform.common.PageResult;
import com.english.platform.common.Result;
import com.english.platform.entity.PronunciationRecord;
import com.english.platform.service.PronunciationService;
import com.english.platform.vo.PronunciationResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 英语发音评测模块接口
 */
@RestController
@RequestMapping("/api/pronunciation")
@RequiredArgsConstructor
public class PronunciationController {

    private final PronunciationService pronunciationService;

    /**
     * 上传音频文件进行发音评测（调用阿里云 AI）
     * POST /api/pronunciation/evaluate
     * Content-Type: multipart/form-data
     * 参数：audioFile（WAV音频）、refText（参考文本）
     */
    @PostMapping("/evaluate")
    public Result<PronunciationResultVO> evaluate(
            @RequestParam("audioFile") MultipartFile audioFile,
            @RequestParam("refText") String refText) {
        return Result.success(pronunciationService.evaluate(audioFile, refText));
    }

    /**
     * 分页查询发音评测历史记录
     * GET /api/pronunciation/records?page=1&size=10
     */
    @GetMapping("/records")
    public Result<PageResult<PronunciationRecord>> listRecords(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(pronunciationService.listRecords(page, size));
    }
}
