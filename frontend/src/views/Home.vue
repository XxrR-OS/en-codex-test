<template>
  <div class="home-page">
    <!-- 欢迎横幅 -->
    <div class="welcome-banner">
      <div class="banner-content">
        <h2>👋 你好，{{ userStore.userInfo?.nickname || '同学' }}！</h2>
        <p>今天也要坚持学习英语哦，每天进步一点点 🚀</p>
      </div>
      <div class="banner-stats">
        <div class="stat-item">
          <span class="stat-num">{{ stats.continuousDays || 0 }}</span>
          <span class="stat-label">连续打卡天</span>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item">
          <span class="stat-num">{{ stats.totalScore || 0 }}</span>
          <span class="stat-label">累计积分</span>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item">
          <span class="stat-num">{{ stats.masteredWords || 0 }}</span>
          <span class="stat-label">已掌握单词</span>
        </div>
      </div>
    </div>

    <!-- 功能卡片区 -->
    <el-row :gutter="20" class="feature-cards">
      <el-col :span="6" v-for="card in featureCards" :key="card.path">
        <div class="feature-card" @click="$router.push(card.path)"
          :style="{ background: card.gradient }">
          <div class="card-icon">{{ card.icon }}</div>
          <div class="card-info">
            <div class="card-title">{{ card.title }}</div>
            <div class="card-desc">{{ card.desc }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 学习进度 -->
    <el-row :gutter="20" class="progress-row">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <span>📊 学习进度</span>
          </template>
          <el-row :gutter="16">
            <el-col :span="12">
              <div class="progress-item">
                <div class="progress-label">单词掌握率</div>
                <el-progress
                  type="circle"
                  :percentage="wordRate"
                  :width="90"
                  color="#409EFF"
                />
              </div>
            </el-col>
            <el-col :span="12">
              <div class="progress-item">
                <div class="progress-label">答题正确率</div>
                <el-progress
                  type="circle"
                  :percentage="correctRate"
                  :width="90"
                  color="#67C23A"
                />
              </div>
            </el-col>
          </el-row>
          <el-row :gutter="16" style="margin-top: 16px;">
            <el-col :span="8">
              <el-statistic title="学习单词" :value="stats.totalWords || 0" suffix="个" />
            </el-col>
            <el-col :span="8">
              <el-statistic title="答题总数" :value="stats.totalQuestions || 0" suffix="题" />
            </el-col>
            <el-col :span="8">
              <el-statistic title="累计积分" :value="stats.totalScore || 0" suffix="分" />
            </el-col>
          </el-row>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <span>⚠️ 我的薄弱知识点</span>
          </template>
          <div v-if="stats.weakPoints && stats.weakPoints.length > 0">
            <div v-for="item in stats.weakPoints" :key="item.knowledgeId" class="weak-item">
              <span class="weak-name">{{ item.knowledgeName }}</span>
              <div style="flex:1; margin: 0 12px;">
                <el-progress
                  :percentage="Math.round((item.correctRate || 0) * 100)"
                  :color="getProgressColor(item.correctRate)"
                  :show-text="false"
                  stroke-width="8"
                />
              </div>
              <span class="weak-rate">{{ Math.round((item.correctRate || 0) * 100) }}%</span>
            </div>
          </div>
          <el-empty v-else description="暂无数据，去答题后查看" :image-size="80" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷学习按钮 -->
    <div class="quick-actions">
      <el-button type="primary" size="large" round @click="$router.push('/word')">
        📖 开始今日单词学习
      </el-button>
      <el-button type="success" size="large" round @click="$router.push('/practice')">
        ✏️ 开始自适应练习
      </el-button>
      <el-button type="warning" size="large" round @click="$router.push('/pronunciation')">
        🎤 发音评测
      </el-button>
      <el-button type="info" size="large" round @click="$router.push('/essay')">
        📝 AI作文批改
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/store/user'
import request from '@/api/request'

const userStore = useUserStore()
const stats = ref({})

const featureCards = [
  { path: '/word', title: '单词学习', desc: '艾宾浩斯记忆法', icon: '📖', gradient: 'linear-gradient(135deg,#1a73e8,#0d47a1)' },
  { path: '/practice', title: 'AI自适应练习', desc: '智能推荐薄弱题目', icon: '🧠', gradient: 'linear-gradient(135deg,#00c853,#1b5e20)' },
  { path: '/essay', title: 'AI作文批改', desc: '通义千问智能批改', icon: '✍️', gradient: 'linear-gradient(135deg,#ff6f00,#e65100)' },
  { path: '/pronunciation', title: '发音评测', desc: 'AI语音评测打分', icon: '🎤', gradient: 'linear-gradient(135deg,#7b1fa2,#4a148c)' }
]

const wordRate = computed(() => {
  if (!stats.value.totalWords) return 0
  return Math.round((stats.value.masteredWords / stats.value.totalWords) * 100)
})
const correctRate = computed(() => Math.round(stats.value.correctRate || 0))

function getProgressColor(rate) {
  const r = (rate || 0) * 100
  if (r >= 70) return '#67C23A'
  if (r >= 40) return '#E6A23C'
  return '#F56C6C'
}

onMounted(async () => {
  try {
    stats.value = await request.get('/statistics/overview')
  } catch (e) {
    console.error(e)
  }
})
</script>

<style scoped>
.home-page { max-width: 1200px; margin: 0 auto; }
.welcome-banner {
  background: linear-gradient(135deg, #1a73e8, #0d47a1);
  border-radius: 16px;
  padding: 28px 32px;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}
.banner-content h2 { font-size: 22px; margin-bottom: 6px; }
.banner-content p { opacity: 0.85; font-size: 14px; }
.banner-stats { display: flex; align-items: center; gap: 24px; }
.stat-item { text-align: center; }
.stat-num { display: block; font-size: 28px; font-weight: bold; }
.stat-label { font-size: 12px; opacity: 0.8; }
.stat-divider { width: 1px; height: 40px; background: rgba(255,255,255,0.3); }
.feature-cards { margin-bottom: 24px; }
.feature-card {
  border-radius: 12px;
  padding: 20px;
  color: #fff;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  display: flex;
  align-items: center;
  gap: 16px;
}
.feature-card:hover { transform: translateY(-4px); box-shadow: 0 8px 24px rgba(0,0,0,0.2); }
.card-icon { font-size: 36px; }
.card-title { font-size: 16px; font-weight: bold; margin-bottom: 4px; }
.card-desc { font-size: 12px; opacity: 0.85; }
.progress-row { margin-bottom: 24px; }
.progress-item { display: flex; flex-direction: column; align-items: center; gap: 8px; padding: 12px 0; }
.progress-label { font-size: 13px; color: #606266; }
.weak-item { display: flex; align-items: center; padding: 8px 0; border-bottom: 1px solid #f0f0f0; }
.weak-item:last-child { border-bottom: none; }
.weak-name { width: 100px; font-size: 13px; color: #303133; flex-shrink: 0; }
.weak-rate { font-size: 13px; color: #606266; width: 40px; text-align: right; flex-shrink: 0; }
.quick-actions { display: flex; gap: 16px; justify-content: center; flex-wrap: wrap; padding: 16px 0 8px; }
</style>
