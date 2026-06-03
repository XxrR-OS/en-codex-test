package com.english.platform.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.english.platform.ai.TongyiAiClient;
import com.english.platform.common.PageResult;
import com.english.platform.dto.EssayDTO;
import com.english.platform.entity.Essay;
import com.english.platform.exception.BusinessException;
import com.english.platform.mapper.EssayMapper;
import com.english.platform.service.EssayService;
import com.english.platform.util.UserContext;
import com.english.platform.vo.EssayResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class EssayServiceImpl implements EssayService {

    private final EssayMapper essayMapper;
    private final TongyiAiClient tongyiClient;

    @Override
    public EssayResultVO correctEssay(EssayDTO dto) {
        Long userId = UserContext.getUserId();

        // 调用通义千问 AI 批改
        String aiResponse = tongyiClient.correctEssay(dto.getTopic(), dto.getContent());
        log.info("AI批改响应: {}", aiResponse);

        // 解析 JSON 结果
        JSONObject json;
        try {
            // 提取JSON部分（防止AI返回非JSON前缀）
            int start = aiResponse.indexOf('{');
            int end = aiResponse.lastIndexOf('}');
            if (start >= 0 && end > start) {
                aiResponse = aiResponse.substring(start, end + 1);
            }
            json = JSON.parseObject(aiResponse);
        } catch (Exception e) {
            log.error("解析AI批改结果失败: {}", aiResponse, e);
            throw new BusinessException("AI批改结果解析失败，请重试");
        }

        // 保存到数据库
        Essay essay = new Essay();
        essay.setUserId(userId);
        essay.setTopic(dto.getTopic());
        essay.setContent(dto.getContent());
        essay.setTotalScore(BigDecimal.valueOf(json.getDoubleValue("totalScore")));
        essay.setGrammarScore(BigDecimal.valueOf(json.getDoubleValue("grammarScore")));
        essay.setContentScore(BigDecimal.valueOf(json.getDoubleValue("contentScore")));
        essay.setStructureScore(BigDecimal.valueOf(json.getDoubleValue("structureScore")));
        essay.setVocabularyScore(BigDecimal.valueOf(json.getDoubleValue("vocabularyScore")));
        essay.setFeedback(json.getString("feedback"));
        essay.setCorrection(json.getString("correction"));
        essayMapper.insert(essay);

        EssayResultVO vo = new EssayResultVO();
        BeanUtils.copyProperties(essay, vo);
        return vo;
    }

    @Override
    public PageResult<Essay> listEssays(Integer page, Integer size) {
        Long userId = UserContext.getUserId();
        var result = essayMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<Essay>()
                        .eq(Essay::getUserId, userId)
                        .orderByDesc(Essay::getCreateTime)
        );
        return PageResult.of(result.getTotal(), result.getRecords());
    }

    @Override
    public Essay getEssayById(Long id) {
        return essayMapper.selectById(id);
    }
}
