package com.english.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.english.platform.entity.Question;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface QuestionMapper extends BaseMapper<Question> {
    /**
     * 按知识点和难度查询题目（自适应推荐用）
     */
    List<Question> selectAdaptiveQuestions(
            @Param("knowledgeIds") List<Long> knowledgeIds,
            @Param("difficulty") Integer difficulty,
            @Param("excludeIds") List<Long> excludeIds,
            @Param("limit") int limit
    );
}
