package com.english.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.english.platform.entity.UserKnowledgeStat;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface UserKnowledgeStatMapper extends BaseMapper<UserKnowledgeStat> {
    /** 查询用户最薄弱的知识点，按weakScore升序 */
    List<UserKnowledgeStat> selectWeakestKnowledge(@Param("userId") Long userId, @Param("limit") int limit);
}
