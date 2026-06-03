package com.english.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.english.platform.entity.UserQuestionRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface UserQuestionRecordMapper extends BaseMapper<UserQuestionRecord> {
    List<Map<String, Object>> selectWeeklyQuestionStats(@Param("userId") Long userId);
    /** 查询用户错题列表（含题目信息） */
    List<Map<String, Object>> selectWrongQuestions(@Param("userId") Long userId, @Param("offset") int offset, @Param("limit") int limit);
    Long countWrongQuestions(@Param("userId") Long userId);
}
