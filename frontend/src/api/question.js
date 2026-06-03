import request from './request'

export const questionApi = {
  getAdaptive: () => request.get('/question/adaptive'),
  submitAnswer: (data) => request.post('/question/answer', data),
  getWrongBook: (params) => request.get('/question/wrong', { params }),
  getByKnowledge: (params) => request.get('/question/by-knowledge', { params }),
  getByCategory: (params) => request.get('/question/by-category', { params })
}
