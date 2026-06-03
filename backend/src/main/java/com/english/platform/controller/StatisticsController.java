package com.english.platform.controller;

import com.english.platform.common.Result;
import com.english.platform.service.StatisticsService;
import com.english.platform.vo.StatisticsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 学习统计模块接口
 */
@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    /**
     * 获取用户综合学习统计概览
     * GET /api/statistics/overview
     */
    @GetMapping("/overview")
    public Result<StatisticsVO> getOverview() {
        return Result.success(statisticsService.getOverview());
    }
}
