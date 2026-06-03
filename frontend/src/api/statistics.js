// 学习统计相关 API
import request from './request'

export const statisticsApi = {
  // 获取用户综合学习统计
  getOverview: () => request.get('/statistics/overview')
}
