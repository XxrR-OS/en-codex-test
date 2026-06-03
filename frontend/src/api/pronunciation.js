// 发音评测相关 API
import request from './request'

export const pronunciationApi = {
  // 上传音频进行发音评测（multipart/form-data）
  evaluate: (formData) => request.post('/pronunciation/evaluate', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  }),
  // 评测历史记录
  getRecords: (params) => request.get('/pronunciation/records', { params })
}
