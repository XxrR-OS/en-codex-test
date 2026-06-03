<template>
  <div class="login-page">
    <div class="login-box">
      <div class="login-header">
        <div class="logo-icon">📚</div>
        <h2>英语学习平台</h2>
        <p>EnglishLearningPlatform</p>
      </div>
      <el-form :model="form" :rules="rules" ref="formRef" class="login-form">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" size="large" prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" size="large"
            prefix-icon="Lock" show-password @keyup.enter="handleLogin" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" :loading="loading" @click="handleLogin" style="width:100%">
            登 录
          </el-button>
        </el-form-item>
        <div class="login-footer">
          还没有账号？<router-link to="/register">立即注册</router-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  try {
    await formRef.value.validate()
  } catch {
    return   // 表单校验不通过，直接返回，不发请求
  }
  loading.value = true
  try {
    await userStore.login(form)
  } catch {
    // 错误已由 request.js 拦截器统一 ElMessage.error 处理，此处静默
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #1a73e8 0%, #0d47a1 50%, #1a1d23 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}
.login-box {
  background: #fff;
  border-radius: 16px;
  padding: 48px 40px;
  width: 400px;
  box-shadow: 0 20px 60px rgba(0,0,0,0.3);
}
.login-header {
  text-align: center;
  margin-bottom: 32px;
}
.logo-icon { font-size: 48px; margin-bottom: 12px; }
.login-header h2 { font-size: 24px; color: #1a1d23; margin-bottom: 4px; }
.login-header p { color: #909399; font-size: 13px; }
.login-form { margin-top: 8px; }
.login-footer { text-align: center; font-size: 14px; color: #909399; }
.login-footer a { color: #409EFF; text-decoration: none; }
</style>
