// 单词相关 API
import request from './request'

export const wordApi = {
  // 获取今日推荐单词（艾宾浩斯）
  getTodayWords: () => request.get('/word/today'),
  // 分页查询单词库
  listWords: (params) => request.get('/word/list', { params }),
  // 搜索单词
  searchWords: (keyword) => request.get('/word/search', { params: { keyword } }),
  // 提交单词学习记录
  studyWord: (data) => request.post('/word/study', data)
}
