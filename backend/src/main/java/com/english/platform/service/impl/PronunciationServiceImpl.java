package com.english.platform.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.english.platform.ai.AliyunPronunciationClient;
import com.english.platform.ai.TongyiAiClient;
import com.english.platform.common.PageResult;
import com.english.platform.entity.PronunciationRecord;
import com.english.platform.exception.BusinessException;
import com.english.platform.mapper.PronunciationRecordMapper;
import com.english.platform.service.PronunciationService;
import com.english.platform.util.UserContext;
import com.english.platform.vo.PronunciationResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class PronunciationServiceImpl implements PronunciationService {

    private static final int MAX_REF_TEXT_LENGTH = 500;

    private final PronunciationRecordMapper recordMapper;
    private final AliyunPronunciationClient aliyunClient;
    private final TongyiAiClient tongyiClient;

    @Override
    public PronunciationResultVO evaluate(MultipartFile audioFile, String refText) {
        Long userId = UserContext.getUserId();
        if (audioFile == null || audioFile.isEmpty()) {
            throw new BusinessException("请先录音后再提交");
        }

        String normalizedRefText = refText == null ? "" : refText.trim();
        if (normalizedRefText.isBlank()) {
            throw new BusinessException("评测文本不能为空");
        }

        byte[] audioBytes;
        try {
            audioBytes = audioFile.getBytes();
        } catch (Exception e) {
            throw new BusinessException("音频文件读取失败");
        }

        var evaluation = aliyunClient.evaluate(audioBytes, normalizedRefText, audioFile.getContentType());
        PronunciationResultVO result = evaluation.result();

        try {
            String feedback = tongyiClient.generatePronunciationFeedback(
                    normalizedRefText,
                    evaluation.transcript(),
                    result.getTotalScore() == null ? 0 : result.getTotalScore().doubleValue(),
                    result.getAccuracyScore() == null ? 0 : result.getAccuracyScore().doubleValue(),
                    result.getFluencyScore() == null ? 0 : result.getFluencyScore().doubleValue()
            );
            result.setFeedback(feedback);
        } catch (Exception e) {
            log.warn("生成发音反馈失败", e);
        }

        try {
            PronunciationRecord record = new PronunciationRecord();
            record.setUserId(userId);
            record.setWordText(limitLength(normalizedRefText, MAX_REF_TEXT_LENGTH));
            record.setTotalScore(result.getTotalScore());
            record.setAccuracyScore(result.getAccuracyScore());
            record.setFluencyScore(result.getFluencyScore());
            record.setWordScores(JSON.toJSONString(result.getWordScores()));
            record.setFeedback(result.getFeedback());
            recordMapper.insert(record);
            result.setId(record.getId());
        } catch (Exception e) {
            log.warn("保存发音评测记录失败", e);
        }

        return result;
    }

    @Override
    public PageResult<PronunciationRecord> listRecords(Integer page, Integer size) {
        Long userId = UserContext.getUserId();
        var result = recordMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<PronunciationRecord>()
                        .eq(PronunciationRecord::getUserId, userId)
                        .orderByDesc(PronunciationRecord::getCreateTime)
        );
        return PageResult.of(result.getTotal(), result.getRecords());
    }

    private String limitLength(String value, int maxLength) {
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
