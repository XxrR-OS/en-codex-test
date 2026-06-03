package com.english.platform.service;

import com.english.platform.vo.StatisticsVO;

public interface StatisticsService {
    /** 获取用户综合学习统计 */
    StatisticsVO getOverview();
}
