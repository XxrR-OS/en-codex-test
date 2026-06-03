package com.english.platform.service;

import com.english.platform.common.PageResult;
import com.english.platform.dto.WordStudyDTO;
import com.english.platform.entity.Word;
import com.english.platform.vo.ErrorWordVO;

import java.util.List;

public interface WordService {
    /** 获取今日推荐单词（未学习/待复习） */
    List<Word> getTodayWords();
    /** 分页查询单词库（支持单词本/级别/分类/分页） */
    PageResult<Word> listWords(Integer level, String category, Integer page, Integer size);
    /** 搜索单词 */
    List<Word> searchWords(String keyword);
    /** 提交单词学习记录 */
    void studyWord(WordStudyDTO dto);
    /** 获取错题本（错误次数>0的单词） */
    List<ErrorWordVO> getErrorWords();
    /** 获取已学习单词 */
    List<ErrorWordVO> getLearnedWords();
    /** 获取已掌握单词 */
    List<ErrorWordVO> getMasteredWords();
    /** 从错题本中删除（清除错误记录） */
    void deleteErrorWord(Long wordId);
}
