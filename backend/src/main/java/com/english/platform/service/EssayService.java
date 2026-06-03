package com.english.platform.service;

import com.english.platform.common.PageResult;
import com.english.platform.dto.EssayDTO;
import com.english.platform.entity.Essay;
import com.english.platform.vo.EssayResultVO;

public interface EssayService {
    /** AI批改作文 */
    EssayResultVO correctEssay(EssayDTO dto);
    /** 分页查询历史作文 */
    PageResult<Essay> listEssays(Integer page, Integer size);
    /** 查询作文详情 */
    Essay getEssayById(Long id);
}
