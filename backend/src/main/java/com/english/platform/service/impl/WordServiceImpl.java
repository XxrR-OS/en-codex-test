package com.english.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.english.platform.common.PageResult;
import com.english.platform.dto.WordStudyDTO;
import com.english.platform.entity.User;
import com.english.platform.entity.UserWordRecord;
import com.english.platform.entity.Word;
import com.english.platform.mapper.UserMapper;
import com.english.platform.mapper.UserWordRecordMapper;
import com.english.platform.mapper.WordMapper;
import com.english.platform.service.WordService;
import com.english.platform.util.UserContext;
import com.english.platform.vo.ErrorWordVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WordServiceImpl implements WordService {

    private final WordMapper wordMapper;
    private final UserWordRecordMapper wordRecordMapper;
    private final UserMapper userMapper;

    @Override
    public List<Word> getTodayWords() {
        Long userId = UserContext.getUserId();
        User user = userMapper.selectById(userId);
        int level = user != null ? user.getLevel() : 1;
        return wordMapper.selectRecommendWords(userId, level, 20);
    }

    @Override
    public PageResult<Word> listWords(Integer level, String category, Integer page, Integer size) {
        LambdaQueryWrapper<Word> wrapper = new LambdaQueryWrapper<>();
        if (level != null) wrapper.eq(Word::getLevel, level);
        if (category != null && !category.isBlank()) wrapper.eq(Word::getCategory, category);
        IPage<Word> result = wordMapper.selectPage(new Page<>(page, size), wrapper);
        return PageResult.of(result.getTotal(), result.getRecords());
    }

    @Override
    public List<Word> searchWords(String keyword) {
        return wordMapper.selectList(
                new LambdaQueryWrapper<Word>()
                        .like(Word::getWord, keyword)
                        .or()
                        .like(Word::getTranslation, keyword)
                        .last("LIMIT 20")
        );
    }

    @Override
    public void studyWord(WordStudyDTO dto) {
        Long userId = UserContext.getUserId();
        // 查询是否已有记录
        UserWordRecord record = wordRecordMapper.selectOne(
                new LambdaQueryWrapper<UserWordRecord>()
                        .eq(UserWordRecord::getUserId, userId)
                        .eq(UserWordRecord::getWordId, dto.getWordId())
        );

        LocalDateTime now = LocalDateTime.now();
        if (record == null) {
            record = new UserWordRecord();
            record.setUserId(userId);
            record.setWordId(dto.getWordId());
            record.setStudyCount(1);
            record.setCorrectCount(dto.getMastered() == 1 ? 1 : 0);
            record.setWrongCount(dto.getMastered() == 0 ? 1 : 0);
            record.setMastered(dto.getMastered());
            record.setLastStudy(now);
            // 艾宾浩斯遗忘曲线：第一次学习后1天复习
            record.setNextReview(dto.getMastered() == 1 ? now.plusDays(1) : now.plusHours(6));
            wordRecordMapper.insert(record);
        } else {
            record.setStudyCount(record.getStudyCount() + 1);
            record.setLastStudy(now);
            if (dto.getMastered() == 1) {
                record.setCorrectCount(record.getCorrectCount() + 1);
                record.setMastered(1);
                // 艾宾浩斯间隔：1天→3天→7天→15天→30天
                record.setNextReview(calcNextReview(record.getCorrectCount(), now));
            } else {
                record.setWrongCount(record.getWrongCount() + 1);
                record.setMastered(0);
                record.setNextReview(now.plusHours(6)); // 不认识的6小时后再复习
            }
            wordRecordMapper.updateById(record);
        }
    }

    /** 艾宾浩斯间隔计算 */
    private LocalDateTime calcNextReview(int correctCount, LocalDateTime now) {
        return switch (correctCount) {
            case 1 -> now.plusDays(1);
            case 2 -> now.plusDays(3);
            case 3 -> now.plusDays(7);
            case 4 -> now.plusDays(15);
            default -> now.plusDays(30);
        };
    }

    @Override
    public List<ErrorWordVO> getErrorWords() {
        Long userId = UserContext.getUserId();
        // 查询错误次数>0的记录
        List<UserWordRecord> records = wordRecordMapper.selectList(
                new LambdaQueryWrapper<UserWordRecord>()
                        .eq(UserWordRecord::getUserId, userId)
                        .gt(UserWordRecord::getWrongCount, 0)
                        .orderByDesc(UserWordRecord::getWrongCount)
        );
        List<ErrorWordVO> result = new ArrayList<>();
        for (UserWordRecord record : records) {
            Word word = wordMapper.selectById(record.getWordId());
            if (word == null) continue;
            ErrorWordVO vo = new ErrorWordVO();
            vo.setWordId(word.getId());
            vo.setWord(word.getWord());
            vo.setPhonetic(word.getPhonetic());
            vo.setTranslation(word.getTranslation());
            vo.setCategory(word.getCategory());
            vo.setWrongCount(record.getWrongCount());
            vo.setStudyCount(record.getStudyCount());
            result.add(vo);
        }
        return result;
    }

    @Override
    public List<ErrorWordVO> getLearnedWords() {
        Long userId = UserContext.getUserId();
        List<UserWordRecord> records = wordRecordMapper.selectList(
                new LambdaQueryWrapper<UserWordRecord>()
                        .eq(UserWordRecord::getUserId, userId)
                        .orderByDesc(UserWordRecord::getLastStudy)
        );
        return buildWordRecordVO(records);
    }

    @Override
    public List<ErrorWordVO> getMasteredWords() {
        Long userId = UserContext.getUserId();
        List<UserWordRecord> records = wordRecordMapper.selectList(
                new LambdaQueryWrapper<UserWordRecord>()
                        .eq(UserWordRecord::getUserId, userId)
                        .eq(UserWordRecord::getMastered, 1)
                        .orderByDesc(UserWordRecord::getLastStudy)
        );
        return buildWordRecordVO(records);
    }

    private List<ErrorWordVO> buildWordRecordVO(List<UserWordRecord> records) {
        List<ErrorWordVO> result = new ArrayList<>();
        for (UserWordRecord record : records) {
            Word word = wordMapper.selectById(record.getWordId());
            if (word == null) continue;
            ErrorWordVO vo = new ErrorWordVO();
            vo.setWordId(word.getId());
            vo.setWord(word.getWord());
            vo.setPhonetic(word.getPhonetic());
            vo.setTranslation(word.getTranslation());
            vo.setCategory(word.getCategory());
            vo.setWrongCount(record.getWrongCount());
            vo.setStudyCount(record.getStudyCount());
            result.add(vo);
        }
        return result;
    }

    @Override
    public void deleteErrorWord(Long wordId) {
        Long userId = UserContext.getUserId();
        // 清除错误记录（重置wrongCount为0）
        UserWordRecord record = wordRecordMapper.selectOne(
                new LambdaQueryWrapper<UserWordRecord>()
                        .eq(UserWordRecord::getUserId, userId)
                        .eq(UserWordRecord::getWordId, wordId)
        );
        if (record != null) {
            record.setWrongCount(0);
            wordRecordMapper.updateById(record);
        }
    }
}
