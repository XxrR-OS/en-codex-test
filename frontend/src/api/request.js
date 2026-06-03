import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const request = axios.create({ baseURL: '/api', timeout: 60000 })

function resolveErrorMessage(error) {
  const responseData = error?.response?.data
  if (typeof responseData?.message === 'string' && responseData.message.trim()) {
    return responseData.message
  }
  if (typeof responseData === 'string' && responseData.trim()) {
    return responseData
  }
  if (typeof error?.message === 'string' && error.message.trim()) {
    return error.message
  }
  return '网络错误，请稍后重试'
}

request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
}, error => Promise.reject(error))

request.interceptors.response.use(response => {
  const { code, message, data } = response.data
  if (code === 200) return data
  if (code === 401) {
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    router.push('/login')
    ElMessage.error(message || '登录已过期，请重新登录')
  } else {
    ElMessage.error(message || '请求失败')
  }
  return Promise.reject(new Error(message || '请求失败'))
}, error => {
  ElMessage.error(resolveErrorMessage(error))
  return Promise.reject(error)
})

export default request
