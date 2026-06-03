<template>
  <el-container class="layout-wrap">
    <el-aside width="220px" class="sidebar">
      <div class="logo">
        <el-icon size="24" color="#fff"><Reading /></el-icon>
        <span>英语学习平台</span>
      </div>
      <el-menu
        ref="menuRef"
        :default-active="activeMenu"
        :default-openeds="defaultOpeneds"
        background-color="#1a1d23"
        text-color="#b0b8c8"
        active-text-color="#409EFF"
        class="side-menu"
        @select="handleMenuSelect"
      >
        <el-menu-item index="/home"><el-icon><House /></el-icon><span>学习首页</span></el-menu-item>
        <el-menu-item index="/word"><el-icon><Notebook /></el-icon><span>单词学习</span></el-menu-item>

        <el-sub-menu index="practice-group" :class="{ 'is-section-active': practiceSectionActive }">
          <template #title>
            <el-icon><EditPen /></el-icon>
            <span>题库练习</span>
          </template>
          <el-menu-item
            v-for="item in practiceCategories"
            :key="item.category"
            :index="`practice-category:${item.category}`"
          >
            {{ item.label }}
          </el-menu-item>
          <el-menu-item index="practice-adaptive">AI自适应练习</el-menu-item>
          <el-menu-item index="practice-wrong-book">错题本</el-menu-item>
        </el-sub-menu>

        <el-menu-item index="/essay"><el-icon><Document /></el-icon><span>AI作文批改</span></el-menu-item>
        <el-menu-item index="/pronunciation"><el-icon><Microphone /></el-icon><span>发音评测</span></el-menu-item>
        <el-menu-item index="/statistics"><el-icon><DataLine /></el-icon><span>学习统计</span></el-menu-item>
        <el-menu-item index="/profile"><el-icon><User /></el-icon><span>个人中心</span></el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <span class="page-title">{{ $route.meta.title }}</span>
        <div class="right">
          <el-tag type="success"><el-icon><Trophy /></el-icon> 积分：{{ userStore.userInfo?.totalScore || 0 }}</el-tag>
          <el-dropdown @command="cmd => cmd === 'logout' ? userStore.logout() : $router.push('/profile')">
            <div class="user-avatar">
              <el-avatar :size="32">{{ (userStore.userInfo?.nickname || 'U').charAt(0) }}</el-avatar>
              <span>{{ userStore.userInfo?.nickname || userStore.userInfo?.username || '用户' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="main"><router-view /></el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const route = useRoute()
const router = useRouter()
const menuRef = ref()

const practiceCategories = [
  { category: '语法', label: '语法题' },
  { category: '阅读', label: '阅读题' },
  { category: '词汇', label: '词汇题' },
  { category: '翻译', label: '翻译题' },
  { category: '写作', label: '写作题' },
  { category: '听力', label: '听力题' },
  { category: '口语', label: '口语题' }
]

const practiceSectionActive = computed(() => route.path === '/practice' || route.path === '/wrong-book')
const activeMenu = computed(() => {
  if (route.path === '/practice') {
    if (route.query.mode === 'category' && route.query.category) {
      return `practice-category:${route.query.category}`
    }
    return 'practice-adaptive'
  }
  if (route.path === '/wrong-book') {
    return 'practice-wrong-book'
  }
  return route.path
})
const defaultOpeneds = computed(() => practiceSectionActive.value ? ['practice-group'] : [])

function handleMenuSelect(index) {
  if (index === 'practice-adaptive') {
    router.push({ path: '/practice', query: { mode: 'adaptive' } })
    return
  }
  if (index === 'practice-wrong-book') {
    router.push('/wrong-book')
    return
  }
  if (index.startsWith('practice-category:')) {
    const category = index.split(':')[1]
    router.push({ path: '/practice', query: { mode: 'category', category } })
    return
  }
  router.push(index)
}

watch(() => route.fullPath, () => {
  if (practiceSectionActive.value) {
    menuRef.value?.open('practice-group')
  }
})
</script>

<style scoped>
.layout-wrap { height: 100vh; overflow: hidden; }
.sidebar { background: #1a1d23; display: flex; flex-direction: column; }
.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #fff;
  font-size: 15px;
  font-weight: bold;
  border-bottom: 1px solid #2a2f3a;
}
.side-menu { border-right: none; flex: 1; }
.side-menu :deep(.el-menu-item),
.side-menu :deep(.el-sub-menu__title) {
  height: 56px;
  line-height: 56px;
  border-bottom: none;
}
.side-menu :deep(.el-menu-item:hover),
.side-menu :deep(.el-sub-menu__title:hover) {
  background: #242933 !important;
}
.side-menu :deep(.el-menu-item.is-active) {
  background: rgba(64, 158, 255, 0.12) !important;
}
.side-menu :deep(.el-sub-menu .el-menu) {
  background: #16191f !important;
}
.side-menu :deep(.el-sub-menu .el-menu-item) {
  min-width: 220px;
  padding-left: 52px !important;
  font-size: 14px;
}
.side-menu :deep(.el-sub-menu.is-section-active > .el-sub-menu__title) {
  color: #409EFF !important;
}
.header {
  background: #fff;
  border-bottom: 1px solid #e8ecf0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
}
.page-title { font-size: 17px; font-weight: 600; color: #303133; }
.right { display: flex; align-items: center; gap: 16px; }
.user-avatar { display: flex; align-items: center; gap: 8px; cursor: pointer; }
.main { background: #f5f7fa; overflow-y: auto; }
</style>
