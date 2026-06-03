// 作文批改相关 API
import request from './request'

export const essayApi = {
  // 提交作文 AI 批改
  correct: (data) => request.post('/essay/correct', data),
  // 历史作文列表
  list: (params) => request.get('/essay/list', { params }),
  // 作文详情
  getById: (id) => request.get(`/essay/${id}`)
}
