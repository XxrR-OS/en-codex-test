package com.english.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.english.platform.entity.StudyCheckin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface StudyCheckinMapper extends BaseMapper<StudyCheckin> {
    /** 查询连续打卡天数 */
    Integer selectContinuousDays(@Param("userId") Long userId);
}
