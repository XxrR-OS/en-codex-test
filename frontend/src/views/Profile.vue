<template>
  <div class="profile-page">
    <section class="profile-hero">
      <div class="hero-backdrop hero-backdrop-left"></div>
      <div class="hero-backdrop hero-backdrop-right"></div>

      <div class="hero-intro">
        <div class="hero-avatar">
          {{ displayInitial }}
        </div>
        <div class="hero-copy">
          <div class="hero-tag">PERSONAL HUB</div>
          <h1>{{ userInfo.nickname || userInfo.username || '未命名用户' }}</h1>
          <p class="hero-subtitle">
            @{{ userInfo.username || 'guest' }} · {{ levelInfo.label }} · {{ profileSlogan }}
          </p>
          <div class="hero-meta">
            <span class="meta-pill">注册于 {{ formatDate(userInfo.createTime) }}</span>
            <span class="meta-pill">{{ userInfo.email || '暂未填写邮箱' }}</span>
          </div>
        </div>
      </div>

      <div class="hero-stats">
        <div v-for="item in headlineStats" :key="item.label" class="hero-stat-card">
          <div class="hero-stat-value">{{ item.value }}</div>
          <div class="hero-stat-label">{{ item.label }}</div>
          <div class="hero-stat-note">{{ item.note }}</div>
        </div>
      </div>
    </section>

    <section class="profile-grid">
      <div class="profile-main">
        <el-card shadow="hover" class="panel-card edit-panel">
          <template #header>
            <div class="panel-head">
              <div>
                <h3>资料编辑</h3>
                <p>更新昵称、邮箱和当前英语水平，系统会据此调整推荐内容。</p>
              </div>
              <div class="panel-accent">PROFILE</div>
            </div>
          </template>

          <el-form ref="formRef" :model="editForm" :rules="rules" label-position="top" class="profile-form">
            <div class="form-grid">
              <el-form-item label="昵称" prop="nickname">
                <el-input v-model="editForm.nickname" placeholder="请输入昵称" />
              </el-form-item>
              <el-form-item label="邮箱" prop="email">
                <el-input v-model="editForm.email" placeholder="请输入邮箱" />
              </el-form-item>
            </div>

            <el-form-item label="英语等级" prop="level">
              <el-radio-group v-model="editForm.level" class="level-switch">
                <el-radio-button :label="1">初级</el-radio-button>
                <el-radio-button :label="2">中级</el-radio-button>
                <el-radio-button :label="3">高级</el-radio-button>
              </el-radio-group>
              <div class="form-tip">等级越高，系统推荐的单词和题目难度越高。</div>
            </el-form-item>

            <div class="action-row">
              <el-button type="primary" size="large" :loading="saving" @click="saveProfile">保存资料</el-button>
              <el-button size="large" @click="resetForm">恢复当前信息</el-button>
            </div>
          </el-form>
        </el-card>

        <el-card shadow="hover" class="panel-card security-panel">
          <template #header>
            <div class="panel-head">
              <div>
                <h3>账号安全</h3>
                <p>修改密码后会自动退出登录，确保账号安全。</p>
              </div>
              <div class="panel-accent warning">SECURITY</div>
            </div>
          </template>

          <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-position="top" class="profile-form">
            <div class="form-grid">
              <el-form-item label="新密码" prop="newPassword">
                <el-input
                  v-model="pwdForm.newPassword"
                  type="password"
                  show-password
                  placeholder="请输入6-30位新密码"
                />
              </el-form-item>
              <el-form-item label="确认密码" prop="confirmPassword">
                <el-input
                  v-model="pwdForm.confirmPassword"
                  type="password"
                  show-password
                  placeholder="请再次输入新密码"
                />
              </el-form-item>
            </div>

            <div class="security-tips">
              <div class="tip-item">
                <span class="tip-dot"></span>
                避免使用生日、手机号等易猜测信息
              </div>
              <div class="tip-item">
                <span class="tip-dot"></span>
                建议同时包含字母、数字和符号
              </div>
            </div>

            <div class="action-row">
              <el-button type="warning" size="large" :loading="changingPwd" @click="changePassword">
                修改密码
              </el-button>
            </div>
          </el-form>
        </el-card>
      </div>

      <div class="profile-side">
        <el-card shadow="hover" class="panel-card portrait-panel">
          <template #header>
            <div class="panel-head compact">
              <div>
                <h3>学习画像</h3>
                <p>从单词、答题和打卡数据快速了解当前状态。</p>
              </div>
            </div>
          </template>

          <div class="portrait-list">
            <div class="portrait-item">
              <div class="portrait-label">资料完整度</div>
              <div class="portrait-value">{{ profileCompletion }}%</div>
            </div>
            <el-progress :percentage="profileCompletion" :stroke-width="10" color="#0f766e" />

            <div class="portrait-item">
              <div class="portrait-label">单词掌握率</div>
              <div class="portrait-value">{{ wordRate }}%</div>
            </div>
            <el-progress :percentage="wordRate" :stroke-width="10" color="#2563eb" />

            <div class="portrait-item">
              <div class="portrait-label">答题正确率</div>
              <div class="portrait-value">{{ correctRate }}%</div>
            </div>
            <el-progress :percentage="correctRate" :stroke-width="10" color="#f59e0b" />
          </div>

          <div class="portrait-summary">
            <div class="summary-card">
              <span class="summary-title">本周学习</span>
              <strong>{{ weeklyWordCount }}</strong>
              <span class="summary-note">个单词</span>
            </div>
            <div class="summary-card">
              <span class="summary-title">本周练习</span>
              <strong>{{ weeklyQuestionCount }}</strong>
              <span class="summary-note">道题目</span>
            </div>
            <div class="summary-card">
              <span class="summary-title">连续打卡</span>
              <strong>{{ stats.continuousDays || 0 }}</strong>
              <span class="summary-note">天</span>
            </div>
          </div>
        </el-card>

        <el-card shadow="hover" class="panel-card focus-panel">
          <template #header>
            <div class="panel-head compact">
              <div>
                <h3>学习重点</h3>
                <p>优先补齐薄弱点，学习收益会更高。</p>
              </div>
            </div>
          </template>

          <div class="focus-hero">
            <div class="focus-title">{{ topWeakPoint ? topWeakPoint.knowledgeName : '开始练习后生成画像' }}</div>
            <div class="focus-desc">
              {{ focusSuggestion }}
            </div>
          </div>

          <div v-if="stats.weakPoints?.length" class="weak-list">
            <div v-for="item in stats.weakPoints.slice(0, 4)" :key="item.knowledgeId" class="weak-row">
              <div class="weak-info">
                <strong>{{ item.knowledgeName }}</strong>
                <span>已练 {{ item.totalCount || 0 }} 题</span>
              </div>
              <div class="weak-bar">
                <el-progress
                  :percentage="Math.round(normalizeRate(item.correctRate))"
                  :stroke-width="8"
                  :show-text="false"
                  :color="getProgressColor(item.correctRate)"
                />
              </div>
              <div class="weak-rate">{{ Math.round(normalizeRate(item.correctRate)) }}%</div>
            </div>
          </div>
          <el-empty v-else description="暂无薄弱点数据，去题库练习后再回来查看" :image-size="80" />

          <div class="shortcut-actions">
            <el-button type="primary" plain @click="$router.push('/practice')">去做题</el-button>
            <el-button type="success" plain @click="$router.push('/statistics')">看统计</el-button>
            <el-button type="info" plain @click="$router.push('/word')">背单词</el-button>
          </div>
        </el-card>
      </div>
    </section>

    <el-card shadow="hover" class="panel-card badge-panel">
      <template #header>
        <div class="panel-head">
          <div>
            <h3>成就墙</h3>
            <p>把阶段成果具象化，学习进度更清晰。</p>
          </div>
          <div class="panel-accent success">BADGES</div>
        </div>
      </template>

      <div class="badge-grid">
        <div v-for="badge in badges" :key="badge.name" class="badge-card" :class="{ unlocked: badge.unlocked }">
          <div class="badge-glow"></div>
          <div class="badge-icon">{{ badge.icon }}</div>
          <div class="badge-title">{{ badge.name }}</div>
          <div class="badge-desc">{{ badge.desc }}</div>
          <div class="badge-state">{{ badge.unlocked ? '已解锁' : '未达成' }}</div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import request from '@/api/request'
import dayjs from 'dayjs'

const userStore = useUserStore()
const formRef = ref()
const pwdFormRef = ref()
const saving = ref(false)
const changingPwd = ref(false)
const stats = ref({})

const userInfo = computed(() => userStore.userInfo || {})

const levelMap = {
  1: { label: '初级学习者', accent: '基础巩固阶段' },
  2: { label: '中级学习者', accent: '强化提升阶段' },
  3: { label: '高级学习者', accent: '冲刺突破阶段' }
}

const editForm = reactive({
  nickname: '',
  email: '',
  level: 1
})

const pwdForm = reactive({
  newPassword: '',
  confirmPassword: ''
})

const rules = {
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }]
}

const pwdRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 30, message: '密码长度为6-30位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== pwdForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ]
}

const levelInfo = computed(() => levelMap[userInfo.value.level] || levelMap[1])
const displayInitial = computed(() => (userInfo.value.nickname || userInfo.value.username || 'U').charAt(0).toUpperCase())

const profileCompletion = computed(() => {
  const checks = [
    !!userInfo.value.username,
    !!userInfo.value.nickname,
    !!userInfo.value.email,
    !!userInfo.value.level
  ]
  return Math.round((checks.filter(Boolean).length / checks.length) * 100)
})

const wordRate = computed(() => {
  const totalWords = Number(stats.value.totalWords || 0)
  const masteredWords = Number(stats.value.masteredWords || 0)
  if (!totalWords) return 0
  return Math.round((masteredWords / totalWords) * 100)
})

const correctRate = computed(() => {
  const rate = Number(stats.value.correctRate || 0)
  return Math.round(rate > 1 ? rate : rate * 100)
})

const weeklyWordCount = computed(() =>
  (stats.value.weeklyData || []).reduce((sum, item) => sum + Number(item.wordCount || 0), 0)
)

const weeklyQuestionCount = computed(() =>
  (stats.value.weeklyData || []).reduce((sum, item) => sum + Number(item.questionCount || 0), 0)
)

const topWeakPoint = computed(() => (stats.value.weakPoints || [])[0] || null)

const headlineStats = computed(() => [
  {
    label: '累计积分',
    value: Number(userInfo.value.totalScore || 0),
    note: '学习行为沉淀的成长值'
  },
  {
    label: '已掌握单词',
    value: Number(stats.value.masteredWords || 0),
    note: `总学习 ${Number(stats.value.totalWords || 0)} 个`
  },
  {
    label: '答题正确率',
    value: `${correctRate.value}%`,
    note: `共完成 ${Number(stats.value.totalQuestions || 0)} 道题`
  },
  {
    label: '连续打卡',
    value: `${Number(stats.value.continuousDays || 0)} 天`,
    note: '保持节奏比短期爆发更重要'
  }
])

const profileSlogan = computed(() => {
  if (correctRate.value >= 80) return '保持稳定输出'
  if (correctRate.value >= 60) return '已经进入提分区间'
  return '当前更适合查漏补缺'
})

const focusSuggestion = computed(() => {
  if (!topWeakPoint.value) {
    return '先去完成一轮题库练习，系统会自动为你生成薄弱知识点画像。'
  }
  const rate = Math.round(normalizeRate(topWeakPoint.value.correctRate))
  if (rate >= 70) {
    return `当前 ${topWeakPoint.value.knowledgeName} 已接近稳定，建议继续保持练习频率。`
  }
  if (rate >= 40) {
    return `${topWeakPoint.value.knowledgeName} 还不够稳，建议优先进行针对性练习与错题回顾。`
  }
  return `${topWeakPoint.value.knowledgeName} 是目前最薄弱的板块，建议先完成一组专项训练再进入自适应练习。`
})

const badges = computed(() => {
  const score = Number(userInfo.value.totalScore || 0)
  const words = Number(stats.value.masteredWords || 0)
  const streak = Number(stats.value.continuousDays || 0)
  const totalQuestions = Number(stats.value.totalQuestions || 0)
  return [
    { icon: '🌱', name: '学习启动', desc: '完成首次登录并开始学习', unlocked: true },
    { icon: '📘', name: '词汇积累者', desc: '掌握50个单词', unlocked: words >= 50 },
    { icon: '⚡', name: '百积分突破', desc: '累计积分达到100', unlocked: score >= 100 },
    { icon: '🧠', name: '练习常驻', desc: '累计完成100道题', unlocked: totalQuestions >= 100 },
    { icon: '🔥', name: '连击状态', desc: '连续打卡7天', unlocked: streak >= 7 },
    { icon: '🏆', name: '进阶达人', desc: '累计积分达到1000', unlocked: score >= 1000 }
  ]
})

function normalizeRate(rate) {
  const numeric = Number(rate || 0)
  return numeric > 1 ? numeric : numeric * 100
}

function getProgressColor(rate) {
  const numeric = normalizeRate(rate)
  if (numeric >= 70) return '#16a34a'
  if (numeric >= 40) return '#f59e0b'
  return '#ef4444'
}

function formatDate(value) {
  if (!value) return '未知'
  return dayjs(value).format('YYYY-MM-DD')
}

function syncEditForm() {
  editForm.nickname = userInfo.value.nickname || ''
  editForm.email = userInfo.value.email || ''
  editForm.level = userInfo.value.level || 1
}

function resetForm() {
  syncEditForm()
}

async function loadProfileData() {
  const tasks = [userStore.fetchUserInfo(), request.get('/statistics/overview')]
  const [userResult, statsResult] = await Promise.allSettled(tasks)

  if (userResult.status === 'fulfilled') {
    syncEditForm()
  }
  if (statsResult.status === 'fulfilled') {
    stats.value = statsResult.value || {}
  }
}

async function saveProfile() {
  if (!formRef.value) return
  await formRef.value.validate()
  saving.value = true
  try {
    await request.put('/user/profile', {
      nickname: editForm.nickname,
      email: editForm.email,
      level: editForm.level
    })
    await userStore.fetchUserInfo()
    syncEditForm()
    ElMessage.success('个人资料已更新')
  } catch (error) {
    ElMessage.error('资料保存失败')
  } finally {
    saving.value = false
  }
}

async function changePassword() {
  if (!pwdFormRef.value) return
  await pwdFormRef.value.validate()
  changingPwd.value = true
  try {
    await request.put('/user/profile', { password: pwdForm.newPassword })
    ElMessage.success('密码修改成功，即将重新登录')
    pwdForm.newPassword = ''
    pwdForm.confirmPassword = ''
    setTimeout(() => userStore.logout(), 1200)
  } catch (error) {
    ElMessage.error('密码修改失败')
  } finally {
    changingPwd.value = false
  }
}

watch(
  () => userStore.userInfo,
  () => {
    syncEditForm()
  },
  { immediate: true, deep: true }
)

onMounted(async () => {
  try {
    await loadProfileData()
  } catch (error) {
    ElMessage.warning('部分学习数据加载失败')
  }
})
</script>

<style scoped>
.profile-page {
  max-width: 1280px;
  margin: 0 auto;
  padding: 6px 4px 24px;
  color: #0f172a;
}

.profile-hero {
  position: relative;
  overflow: hidden;
  display: grid;
  grid-template-columns: minmax(320px, 1.2fr) minmax(320px, 1fr);
  gap: 24px;
  padding: 28px;
  border-radius: 28px;
  background:
    radial-gradient(circle at top left, rgba(45, 212, 191, 0.22), transparent 34%),
    radial-gradient(circle at bottom right, rgba(37, 99, 235, 0.22), transparent 38%),
    linear-gradient(135deg, #0f172a 0%, #10243e 42%, #0f766e 100%);
  box-shadow: 0 20px 50px rgba(15, 23, 42, 0.18);
  margin-bottom: 24px;
}

.hero-backdrop {
  position: absolute;
  border-radius: 999px;
  filter: blur(2px);
  opacity: 0.45;
}

.hero-backdrop-left {
  top: -60px;
  left: -40px;
  width: 220px;
  height: 220px;
  background: radial-gradient(circle, rgba(59, 130, 246, 0.45), transparent 72%);
}

.hero-backdrop-right {
  right: -80px;
  bottom: -80px;
  width: 280px;
  height: 280px;
  background: radial-gradient(circle, rgba(45, 212, 191, 0.42), transparent 72%);
}

.hero-intro,
.hero-stats {
  position: relative;
  z-index: 1;
}

.hero-intro {
  display: flex;
  align-items: center;
  gap: 20px;
  min-width: 0;
}

.hero-avatar {
  width: 104px;
  height: 104px;
  border-radius: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 42px;
  font-weight: 700;
  color: #ecfeff;
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.22), rgba(255, 255, 255, 0.08));
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.25);
}

.hero-copy h1 {
  margin: 6px 0 8px;
  font-size: 34px;
  line-height: 1.1;
  color: #f8fafc;
}

.hero-tag {
  display: inline-flex;
  padding: 6px 12px;
  border-radius: 999px;
  letter-spacing: 0.14em;
  font-size: 12px;
  font-weight: 700;
  color: #99f6e4;
  background: rgba(15, 118, 110, 0.25);
  border: 1px solid rgba(153, 246, 228, 0.18);
}

.hero-subtitle {
  margin: 0;
  font-size: 15px;
  line-height: 1.7;
  color: rgba(226, 232, 240, 0.92);
}

.hero-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 18px;
}

.meta-pill {
  padding: 9px 14px;
  border-radius: 999px;
  font-size: 13px;
  color: #dbeafe;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.12);
}

.hero-stats {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.hero-stat-card {
  padding: 18px 18px 16px;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.12);
  backdrop-filter: blur(12px);
}

.hero-stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #f8fafc;
}

.hero-stat-label {
  margin-top: 4px;
  font-size: 13px;
  letter-spacing: 0.04em;
  color: #a5f3fc;
}

.hero-stat-note {
  margin-top: 10px;
  font-size: 12px;
  line-height: 1.6;
  color: rgba(226, 232, 240, 0.76);
}

.profile-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.25fr) minmax(320px, 0.75fr);
  gap: 24px;
  align-items: start;
}

.profile-main,
.profile-side {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.panel-card {
  border: none;
  border-radius: 26px;
  overflow: hidden;
  box-shadow: 0 18px 45px rgba(15, 23, 42, 0.08);
}

.panel-card :deep(.el-card__header) {
  padding: 22px 24px 18px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.16);
}

.panel-card :deep(.el-card__body) {
  padding: 24px;
}

.edit-panel {
  background:
    linear-gradient(180deg, rgba(240, 253, 250, 0.9), rgba(255, 255, 255, 1) 34%),
    #ffffff;
}

.security-panel {
  background:
    linear-gradient(180deg, rgba(255, 251, 235, 0.92), rgba(255, 255, 255, 1) 34%),
    #ffffff;
}

.portrait-panel,
.focus-panel,
.badge-panel {
  background: #ffffff;
}

.panel-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.panel-head.compact {
  justify-content: flex-start;
}

.panel-head h3 {
  margin: 0;
  font-size: 20px;
  color: #0f172a;
}

.panel-head p {
  margin: 6px 0 0;
  font-size: 13px;
  line-height: 1.6;
  color: #64748b;
}

.panel-accent {
  padding: 8px 12px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.14em;
  color: #0f766e;
  background: rgba(20, 184, 166, 0.12);
}

.panel-accent.warning {
  color: #b45309;
  background: rgba(245, 158, 11, 0.16);
}

.panel-accent.success {
  color: #166534;
  background: rgba(34, 197, 94, 0.14);
}

.profile-form {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.profile-form :deep(.el-form-item__label) {
  padding-bottom: 8px;
  color: #334155;
  font-weight: 600;
}

.profile-form :deep(.el-input__wrapper) {
  min-height: 46px;
  border-radius: 14px;
  box-shadow: 0 0 0 1px rgba(148, 163, 184, 0.26) inset;
}

.profile-form :deep(.el-textarea__inner) {
  border-radius: 14px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.level-switch {
  display: flex;
  flex-wrap: wrap;
}

.level-switch :deep(.el-radio-button__inner) {
  min-width: 96px;
  border-radius: 14px;
  border: none;
  box-shadow: 0 0 0 1px rgba(148, 163, 184, 0.22) inset;
}

.form-tip {
  margin-top: 10px;
  font-size: 12px;
  color: #64748b;
}

.action-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 4px;
}

.action-row :deep(.el-button) {
  min-width: 128px;
  border-radius: 14px;
}

.security-tips {
  display: grid;
  gap: 10px;
  margin-bottom: 4px;
}

.tip-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
  border-radius: 16px;
  background: #fff8eb;
  color: #92400e;
  font-size: 13px;
}

.tip-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #f59e0b;
}

.portrait-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.portrait-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: -6px;
}

.portrait-label {
  font-size: 14px;
  color: #334155;
  font-weight: 600;
}

.portrait-value {
  font-size: 14px;
  color: #0f172a;
  font-weight: 700;
}

.portrait-summary {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-top: 18px;
}

.summary-card {
  padding: 16px 14px;
  border-radius: 20px;
  background: linear-gradient(180deg, #f8fafc, #f1f5f9);
  text-align: center;
}

.summary-card strong {
  display: block;
  margin: 8px 0 4px;
  font-size: 24px;
  color: #0f172a;
}

.summary-title,
.summary-note {
  display: block;
  font-size: 12px;
  color: #64748b;
}

.focus-hero {
  padding: 18px;
  border-radius: 22px;
  background: linear-gradient(135deg, #eff6ff, #ecfeff);
  border: 1px solid rgba(14, 165, 233, 0.12);
}

.focus-title {
  font-size: 18px;
  font-weight: 700;
  color: #0f172a;
}

.focus-desc {
  margin-top: 8px;
  font-size: 13px;
  line-height: 1.7;
  color: #475569;
}

.weak-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
  margin-top: 18px;
}

.weak-row {
  display: grid;
  grid-template-columns: 116px minmax(0, 1fr) 50px;
  gap: 12px;
  align-items: center;
}

.weak-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.weak-info strong {
  font-size: 13px;
  color: #0f172a;
}

.weak-info span,
.weak-rate {
  font-size: 12px;
  color: #64748b;
}

.shortcut-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 20px;
}

.shortcut-actions :deep(.el-button) {
  border-radius: 14px;
}

.badge-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.badge-card {
  position: relative;
  overflow: hidden;
  padding: 22px 18px;
  border-radius: 22px;
  background: linear-gradient(180deg, #f8fafc, #f1f5f9);
  border: 1px solid transparent;
  opacity: 0.56;
  transition: transform 0.24s ease, box-shadow 0.24s ease, opacity 0.24s ease;
}

.badge-card.unlocked {
  opacity: 1;
  background: linear-gradient(135deg, #eff6ff 0%, #ecfeff 44%, #f0fdf4 100%);
  border-color: rgba(14, 165, 233, 0.14);
  box-shadow: 0 14px 30px rgba(14, 165, 233, 0.08);
}

.badge-card:hover {
  transform: translateY(-4px);
}

.badge-glow {
  position: absolute;
  top: -30px;
  right: -30px;
  width: 120px;
  height: 120px;
  background: radial-gradient(circle, rgba(59, 130, 246, 0.18), transparent 70%);
}

.badge-icon,
.badge-title,
.badge-desc,
.badge-state {
  position: relative;
  z-index: 1;
}

.badge-icon {
  font-size: 34px;
}

.badge-title {
  margin-top: 12px;
  font-size: 16px;
  font-weight: 700;
  color: #0f172a;
}

.badge-desc {
  margin-top: 8px;
  font-size: 13px;
  line-height: 1.6;
  color: #64748b;
  min-height: 40px;
}

.badge-state {
  margin-top: 14px;
  font-size: 12px;
  font-weight: 700;
  color: #0f766e;
}

@media (max-width: 1100px) {
  .profile-hero,
  .profile-grid {
    grid-template-columns: 1fr;
  }

  .badge-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .profile-page {
    padding: 0 0 18px;
  }

  .profile-hero {
    padding: 20px;
    border-radius: 22px;
  }

  .hero-intro {
    flex-direction: column;
    align-items: flex-start;
  }

  .hero-copy h1 {
    font-size: 28px;
  }

  .hero-stats,
  .form-grid,
  .portrait-summary,
  .badge-grid {
    grid-template-columns: 1fr;
  }

  .weak-row {
    grid-template-columns: 1fr;
  }

  .weak-rate {
    text-align: left;
  }
}
</style>
