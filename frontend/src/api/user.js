// 用户相关 API
import request from './request'

export const userApi = {
  login: (data) => request.post('/user/login', data),
  register: (data) => request.post('/user/register', data),
  getInfo: () => request.get('/user/info'),
  updateProfile: (data) => request.put('/user/profile', data)
}
