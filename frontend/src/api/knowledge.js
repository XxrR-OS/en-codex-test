// 知识点相关 API
import request from './request'

export const knowledgeApi = {
  // 获取所有知识点列表
  list: () => request.get('/knowledge/list')
}
