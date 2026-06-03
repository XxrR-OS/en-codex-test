<template>
  <div class="login-page">
    <div class="login-box">
      <div class="login-header">
        <div class="logo-icon">📚</div>
        <h2>注册账号</h2>
        <p>加入英语学习平台，开始你的学习之旅</p>
      </div>
      <el-form :model="form" :rules="rules" ref="formRef">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名（4-20位）" size="large" prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码（6-30位）"
            size="large" prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item prop="nickname">
          <el-input v-model="form.nickname" placeholder="昵称（可选）" size="large" prefix-icon="Star" />
        </el-form-item>
        <el-form-item prop="email">
          <el-input v-model="form.email" placeholder="邮箱（可选）" size="large" prefix-icon="Message" />
        </el-form-item>
        <el-form-item prop="level" label="英语等级">
          <el-radio-group v-model="form.level" size="large">
            <el-radio-button :value="1">初级</el-radio-button>
            <el-radio-button :value="2">中级</el-radio-button>
            <el-radio-button :value="3">高级</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" :loading="loading" @click="handleRegister" style="width:100%">
            立即注册
          </el-button>
        </el-form-item>
        <div class="login-footer">
          已有账号？<router-link to="/login">返回登录</router-link>
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

const form = reactive({ username: '', password: '', nickname: '', email: '', level: 1 })
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 4, max: 20, message: '用户名4-20位', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 30, message: '密码6-30位', trigger: 'blur' }
  ],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }]
}

async function handleRegister() {
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  loading.value = true
  try {
    await userStore.register(form)
  } catch {
    // 错误已由 request.js 拦截器统一提示
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
  padding: 40px;
  width: 420px;
  box-shadow: 0 20px 60px rgba(0,0,0,0.3);
}
.login-header { text-align: center; margin-bottom: 24px; }
.logo-icon { font-size: 40px; margin-bottom: 8px; }
.login-header h2 { font-size: 22px; color: #1a1d23; }
.login-header p { color: #909399; font-size: 13px; margin-top: 4px; }
.login-footer { text-align: center; font-size: 14px; color: #909399; }
.login-footer a { color: #409EFF; text-decoration: none; }
</style>
