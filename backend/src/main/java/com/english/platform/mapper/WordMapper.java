package com.english.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.english.platform.entity.Word;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface WordMapper extends BaseMapper<Word> {
    /**
     * 获取用户今日推荐单词（未学习或待复习的单词）
     */
    List<Word> selectRecommendWords(@Param("userId") Long userId, @Param("level") Integer level, @Param("limit") int limit);
}
