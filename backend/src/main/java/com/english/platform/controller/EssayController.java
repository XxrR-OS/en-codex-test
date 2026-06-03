package com.english.platform.controller;

import com.english.platform.common.PageResult;
import com.english.platform.common.Result;
import com.english.platform.dto.EssayDTO;
import com.english.platform.entity.Essay;
import com.english.platform.service.EssayService;
import com.english.platform.vo.EssayResultVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * AI英语作文批改模块接口
 */
@RestController
@RequestMapping("/api/essay")
@RequiredArgsConstructor
public class EssayController {

    private final EssayService essayService;

    /**
     * 提交作文进行AI批改（调用通义千问API）
     * POST /api/essay/correct
     */
    @PostMapping("/correct")
    public Result<EssayResultVO> correctEssay(@RequestBody @Valid EssayDTO dto) {
        return Result.success(essayService.correctEssay(dto));
    }

    /**
     * 分页查询历史作文批改记录
     * GET /api/essay/list?page=1&size=10
     */
    @GetMapping("/list")
    public Result<PageResult<Essay>> listEssays(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(essayService.listEssays(page, size));
    }

    /**
     * 查询作文详情
     * GET /api/essay/{id}
     */
    @GetMapping("/{id}")
    public Result<Essay> getEssayById(@PathVariable Long id) {
        return Result.success(essayService.getEssayById(id));
    }
}
