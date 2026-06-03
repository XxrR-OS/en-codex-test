package com.english.platform.controller;

import com.english.platform.common.Result;
import com.english.platform.entity.KnowledgePoint;
import com.english.platform.mapper.KnowledgePointMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 知识点模块接口
 */
@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgePointMapper knowledgePointMapper;

    /**
     * 获取所有知识点列表
     * GET /api/knowledge/list
     */
    @GetMapping("/list")
    public Result<List<KnowledgePoint>> listKnowledgePoints() {
        return Result.success(knowledgePointMapper.selectList(null));
    }
}
