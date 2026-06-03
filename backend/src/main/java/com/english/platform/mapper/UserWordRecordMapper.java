package com.english.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.english.platform.entity.UserWordRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface UserWordRecordMapper extends BaseMapper<UserWordRecord> {
    List<Map<String, Object>> selectWeeklyWordStats(@Param("userId") Long userId);
}
