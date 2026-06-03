import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/api/request'
import router from '@/router'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))
  const isLoggedIn = computed(() => !!token.value)

  async function login(data) {
    const res = await request.post('/user/login', data)
    token.value = res.token
    userInfo.value = res
    localStorage.setItem('token', res.token)
    localStorage.setItem('userInfo', JSON.stringify(res))
    ElMessage.success('登录成功！')
    router.push('/home')
  }

  async function register(data) {
    await request.post('/user/register', data)
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  }

  async function fetchUserInfo() {
    if (!token.value) return
    const res = await request.get('/user/info')
    userInfo.value = res
    localStorage.setItem('userInfo', JSON.stringify(res))
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    router.push('/login')
    ElMessage.success('已退出登录')
  }

  return { token, userInfo, isLoggedIn, login, register, fetchUserInfo, logout }
})
