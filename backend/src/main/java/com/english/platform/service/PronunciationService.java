package com.english.platform.service;

import com.english.platform.common.PageResult;
import com.english.platform.entity.PronunciationRecord;
import com.english.platform.vo.PronunciationResultVO;
import org.springframework.web.multipart.MultipartFile;

public interface PronunciationService {
    /** 提交音频进行发音评测 */
    PronunciationResultVO evaluate(MultipartFile audioFile, String refText);
    /** 分页查询发音评测历史 */
    PageResult<PronunciationRecord> listRecords(Integer page, Integer size);
}
