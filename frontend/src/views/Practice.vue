<template>
  <div class="practice-page">
    <section v-if="practicing && currentQuestion" class="practice-shell compact-shell">
      <section class="focus-band compact-band">
        <div class="focus-left">
          <div class="section-label">练习进度</div>
          <div class="focus-value">{{ progressPercent }}%</div>
          <div class="focus-subtitle">{{ currentModeTitle }}</div>
        </div>
        <div class="focus-center">
          <el-progress :percentage="progressPercent" :show-text="false" :stroke-width="14" />
        </div>
        <div class="focus-right">
          <span class="pill primary">{{ questionTypeLabel }}</span>
          <span class="pill">{{ currentModeLabel }}</span>
          <span class="pill">{{ getDiffLabel(currentQuestion.difficulty) }}</span>
          <span class="pill">第 {{ currentIndex + 1 }} / {{ questions.length }} 题</span>
          <span class="pill">用时 {{ formatTime(elapsed) }}</span>
        </div>
      </section>

      <div class="content-grid answering-grid">
        <section class="question-stage">
          <div class="stage-head">
            <div class="stage-index">Q{{ currentIndex + 1 }}</div>
            <div class="stage-caption">
              <span>当前题面</span>
              <strong>{{ currentQuestion.title }}</strong>
            </div>
          </div>

          <div v-if="currentQuestion.type === 1 || currentQuestion.type === 2" class="answer-grid compact-answer-grid">
            <button
              v-for="opt in currentQuestion.options"
              :key="opt.optionKey"
              type="button"
              class="answer-card"
              :class="getOptionClass(opt.optionKey)"
              @click="selectOption(opt.optionKey)"
            >
              <span class="answer-key">{{ opt.optionKey }}</span>
              <span class="answer-copy">{{ opt.optionValue }}</span>
            </button>
          </div>

          <div v-else class="response-layout">
            <div class="prompt-card">
              <div class="prompt-label">题目内容</div>
              <div class="prompt-text">{{ currentQuestion.title }}</div>
              <div class="prompt-note">左侧看题，右侧直接作答，减少来回滑动。</div>
            </div>

            <div class="writing-zone response-card">
              <div class="writing-head">
                <span>作答输入区</span>
                <strong>{{ currentQuestion.type === 4 ? '翻译题' : '填空题' }}</strong>
              </div>
              <el-input
                v-model="userAnswer"
                type="textarea"
                :rows="9"
                placeholder="请输入你的答案..."
                :disabled="answered"
              />
            </div>
          </div>

          <div v-if="answered && answerResult" class="feedback-board" :class="{ correct: answerResult.correct, wrong: !answerResult.correct }">
            <div class="feedback-title">
              {{ answerResult.correct ? `回答正确，获得 ${answerResult.scoreGot} 分` : '回答错误，需要回看解析' }}
            </div>
            <div v-if="!answerResult.correct" class="feedback-line">
              正确答案：<strong>{{ answerResult.correctAnswer }}</strong>
            </div>
            <div v-if="answerResult.analysis" class="feedback-line">
              解析：{{ answerResult.analysis }}
            </div>
          </div>

          <div class="action-row">
            <el-button
              v-if="!answered && currentQuestion.type === 2"
              type="primary"
              size="large"
              :disabled="!selectedOption"
              :loading="submitting"
              @click="doSubmit(selectedOption)"
            >
              提交答案
            </el-button>
            <el-button
              v-if="!answered && (currentQuestion.type === 3 || currentQuestion.type === 4)"
              type="primary"
              size="large"
              :loading="submitting"
              @click="submitFillAnswer"
            >
              提交答案
            </el-button>
            <el-button v-if="answered" type="primary" size="large" @click="nextQuestion">
              {{ currentIndex + 1 < questions.length ? '进入下一题' : '查看本轮结果' }}
            </el-button>
          </div>
        </section>

        <aside class="insight-panel">
          <div class="insight-card highlight">
            <div class="card-title">练习聚焦</div>
            <div class="spotlight-score">{{ totalScore }}</div>
            <div class="spotlight-note">当前累计得分</div>
          </div>

          <div class="insight-card">
            <div class="card-title">即时表现</div>
            <div class="mini-stats">
              <div class="mini-stat">
                <span>答对</span>
                <strong>{{ correctCount }}</strong>
              </div>
              <div class="mini-stat">
                <span>剩余</span>
                <strong>{{ Math.max(questions.length - currentIndex - 1, 0) }}</strong>
              </div>
            </div>
          </div>

          <div class="insight-card">
            <div class="card-title">作答提醒</div>
            <div class="reminder-list">
              <div class="reminder-item">
                <span class="dot blue"></span>
                <span>单选题点击后会立即提交。</span>
              </div>
              <div class="reminder-item">
                <span class="dot gold"></span>
                <span>翻译与填空可先修改再提交。</span>
              </div>
              <div class="reminder-item">
                <span class="dot green"></span>
                <span>结果会实时写入自适应推荐逻辑。</span>
              </div>
            </div>
          </div>
        </aside>
      </div>
    </section>

    <section v-else-if="finished" class="practice-shell">
      <header class="hero-banner result">
        <div class="hero-copy">
          <span class="banner-kicker">练习完成</span>
          <h1>{{ scorePercent >= 60 ? '本轮练习完成，节奏不错' : '本轮已结束，建议继续强化薄弱点' }}</h1>
          <p>你刚完成了一轮 {{ currentModeLabel }}。系统已更新练习记录，下一轮会更贴近你的薄弱项。</p>
        </div>
      </header>

      <section class="result-dashboard">
        <div class="result-main">
          <div class="result-score-ring">
            <el-progress
              type="dashboard"
              :percentage="scorePercent"
              :stroke-width="14"
              :width="180"
              :color="scorePercent >= 60 ? '#2d8a4f' : '#d56a2d'"
            >
              <template #default>
                <div class="ring-center">
                  <div class="ring-number">{{ scorePercent }}%</div>
                  <div class="ring-label">正确率</div>
                </div>
              </template>
            </el-progress>
          </div>
          <div class="result-summary">
            <div class="summary-title">本轮成绩概览</div>
            <div class="summary-grid">
              <div class="summary-tile">
                <span>总题数</span>
                <strong>{{ questions.length }}</strong>
              </div>
              <div class="summary-tile">
                <span>答对</span>
                <strong class="ok">{{ correctCount }}</strong>
              </div>
              <div class="summary-tile">
                <span>答错</span>
                <strong class="bad">{{ questions.length - correctCount }}</strong>
              </div>
              <div class="summary-tile">
                <span>得分</span>
                <strong class="gold">{{ totalScore }}</strong>
              </div>
            </div>
          </div>
        </div>

        <div class="result-cta">
          <div class="cta-copy">
            <div class="cta-title">{{ currentModeLabel }}</div>
            <div class="cta-text">继续刷同类题型，或者去错题本做针对性复盘。</div>
          </div>
          <div class="cta-actions">
            <el-button type="primary" size="large" @click="startSelectedPractice">再练一次</el-button>
            <el-button size="large" @click="openWrongBook">查看错题本</el-button>
            <el-button size="large" @click="router.push('/statistics')">学习统计</el-button>
          </div>
        </div>
      </section>
    </section>

    <section v-else class="practice-shell">
      <header class="hero-banner landing">
        <div class="hero-copy">
          <span class="banner-kicker">{{ currentModeLabel }}</span>
          <h1>{{ currentModeTitle }}</h1>
          <p>{{ currentModeDescription }}</p>
          <div class="hero-actions">
            <el-button type="primary" size="large" :loading="loading" @click="startSelectedPractice">
              开始练习
            </el-button>
            <el-button size="large" @click="openWrongBook">进入错题本</el-button>
          </div>
        </div>

        <div class="landing-board">
          <div class="board-top">
            <span>模式概览</span>
            <strong>{{ isAdaptiveMode ? '自适应调度中' : `${selectedCategory || '分类'}专项` }}</strong>
          </div>
          <div class="board-metrics">
            <div class="board-metric">
              <span>知识点</span>
              <strong>{{ isAdaptiveMode ? categoryOptions.length : selectedCategoryCount }}</strong>
            </div>
            <div class="board-metric">
              <span>题量</span>
              <strong>{{ isAdaptiveMode ? '动态' : '8题' }}</strong>
            </div>
            <div class="board-metric">
              <span>入口</span>
              <strong>左侧展开</strong>
            </div>
          </div>
        </div>
      </header>

      <div class="landing-grid">
        <section class="theme-panel emphasize">
          <div class="panel-header">
            <div>
              <span class="header-kicker">核心信息</span>
              <h2>{{ isAdaptiveMode ? 'AI 练习如何突出重点' : `${selectedCategory || '分类'}题库重点` }}</h2>
            </div>
          </div>

          <div v-if="isAdaptiveMode" class="feature-grid">
            <article class="feature-box amber">
              <div class="feature-index">01</div>
              <h3>薄弱知识点优先</h3>
              <p>系统会结合历史错题和掌握率，优先给你推最该练的内容。</p>
            </article>
            <article class="feature-box blue">
              <div class="feature-index">02</div>
              <h3>难度动态切换</h3>
              <p>做得稳定后自动提升强度，不会一直停留在简单题层面。</p>
            </article>
            <article class="feature-box green">
              <div class="feature-index">03</div>
              <h3>结果立刻反馈</h3>
              <p>每次提交都会影响后续推荐，让练习更像连续的训练流。</p>
            </article>
          </div>

          <div v-else class="category-cards">
            <article class="category-card">
              <span>当前分类</span>
              <strong>{{ selectedCategory || '未选择' }}</strong>
            </article>
            <article class="category-card">
              <span>关联知识点</span>
              <strong>{{ selectedCategoryCount }}</strong>
            </article>
            <article class="category-card">
              <span>练习题数</span>
              <strong>8</strong>
            </article>
          </div>
        </section>

        <section class="theme-panel contrast">
          <div class="panel-header">
            <div>
              <span class="header-kicker">分类导航</span>
              <h2>左侧栏已作为主入口</h2>
            </div>
          </div>

          <div class="category-cloud">
            <span
              v-for="item in categoryOptions"
              :key="item.category"
              class="cloud-chip"
              :class="{ active: item.category === selectedCategory && !isAdaptiveMode }"
            >
              {{ item.label }} · {{ item.count }}
            </span>
          </div>

          <div class="mode-note">
            题库页现在以整页展示为主，题目、进度、结果会放大呈现，避免被简洁卡片弱化。
          </div>
        </section>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { knowledgeApi } from '@/api/knowledge'
import { questionApi } from '@/api/question'

const route = useRoute()
const router = useRouter()

const practicing = ref(false)
const finished = ref(false)
const loading = ref(false)
const submitting = ref(false)
const questions = ref([])
const currentIndex = ref(0)
const answered = ref(false)
const userAnswer = ref('')
const answerResult = ref(null)
const selectedOption = ref('')
const totalScore = ref(0)
const correctCount = ref(0)
const elapsed = ref(0)
const loadingCategories = ref(false)
const categoryOptions = ref([])
const practiceMode = ref('adaptive')
const selectedCategory = ref('')

let timer = null

const categoryOrder = ['语法', '阅读', '词汇', '翻译', '写作', '听力', '口语']

const currentQuestion = computed(() => questions.value[currentIndex.value] || null)
const isAdaptiveMode = computed(() => practiceMode.value === 'adaptive')
const progressPercent = computed(() => Math.round((currentIndex.value / Math.max(questions.value.length, 1)) * 100))
const scorePercent = computed(() => {
  const maxScore = questions.value.reduce((sum, item) => sum + (item.score || 10), 0)
  return maxScore > 0 ? Math.round((totalScore.value / maxScore) * 100) : 0
})
const currentAccuracy = computed(() => {
  const answeredCount = answered.value ? currentIndex.value + 1 : currentIndex.value
  if (answeredCount <= 0) {
    return 0
  }
  return Math.round((correctCount.value / answeredCount) * 100)
})
const currentModeLabel = computed(() => (
  isAdaptiveMode.value ? 'AI自适应练习' : `${selectedCategory.value || '分类'}题库`
))
const currentModeTitle = computed(() => (
  isAdaptiveMode.value ? '按你的薄弱点智能推荐题目' : `${selectedCategory.value || '分类'}专项练习`
))
const currentModeDescription = computed(() => {
  if (isAdaptiveMode.value) {
    return '系统会根据你的做题记录、知识点掌握情况和近期练习结果自动抽题，适合连续强化训练。'
  }
  return `当前展示的是 ${selectedCategory.value || '该分类'} 题库，开始后会从这一类题目中随机抽取练习。`
})
const selectedCategoryCount = computed(() => {
  const match = categoryOptions.value.find(item => item.category === selectedCategory.value)
  return match?.count || 0
})
const questionTypeLabel = computed(() => {
  if (!currentQuestion.value) {
    return ''
  }
  return {
    1: '单选题',
    2: '多选题',
    3: '填空题',
    4: '翻译题'
  }[currentQuestion.value.type] || '题目'
})

function getDiffLabel(difficulty) {
  return difficulty === 1 ? '简单' : difficulty === 2 ? '中等' : '困难'
}

function formatTime(seconds) {
  return `${String(Math.floor(seconds / 60)).padStart(2, '0')}:${String(seconds % 60).padStart(2, '0')}`
}

function getOptionClass(key) {
  if (!answered.value) {
    return {
      selected: selectedOption.value === key
    }
  }
  return {
    correct: answerResult.value?.correctAnswer === key,
    wrong: selectedOption.value === key && !answerResult.value?.correct
  }
}

function clearTimer() {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
}

function resetSession() {
  clearTimer()
  practicing.value = false
  finished.value = false
  submitting.value = false
  questions.value = []
  currentIndex.value = 0
  answered.value = false
  userAnswer.value = ''
  answerResult.value = null
  selectedOption.value = ''
  totalScore.value = 0
  correctCount.value = 0
  elapsed.value = 0
}

function syncModeFromRoute() {
  resetSession()

  if (route.query.mode === 'category') {
    practiceMode.value = 'category'

    const requestedCategory = typeof route.query.category === 'string' ? route.query.category.trim() : ''
    const matchedCategory = categoryOptions.value.find(item => item.category === requestedCategory)

    if (matchedCategory) {
      selectedCategory.value = matchedCategory.category
      return
    }

    if (requestedCategory) {
      selectedCategory.value = requestedCategory
      return
    }

    if (categoryOptions.value.length) {
      selectedCategory.value = categoryOptions.value[0].category
      router.replace({
        path: '/practice',
        query: {
          mode: 'category',
          category: selectedCategory.value
        }
      })
    }
    return
  }

  practiceMode.value = 'adaptive'
  if (!selectedCategory.value && categoryOptions.value.length) {
    selectedCategory.value = categoryOptions.value[0].category
  }
}

function openWrongBook() {
  clearTimer()
  router.push('/wrong-book')
}

async function loadCategories() {
  loadingCategories.value = true
  try {
    const list = await knowledgeApi.list()
    const grouped = list.reduce((map, item) => {
      const category = item.category?.trim()
      if (!category) {
        return map
      }
      map.set(category, (map.get(category) || 0) + 1)
      return map
    }, new Map())

    categoryOptions.value = Array.from(grouped.entries())
      .map(([category, count]) => ({
        category,
        count,
        label: `${category}题`
      }))
      .sort((left, right) => {
        const leftIndex = categoryOrder.indexOf(left.category)
        const rightIndex = categoryOrder.indexOf(right.category)
        if (leftIndex === -1 && rightIndex === -1) {
          return left.category.localeCompare(right.category, 'zh-CN')
        }
        if (leftIndex === -1) {
          return 1
        }
        if (rightIndex === -1) {
          return -1
        }
        return leftIndex - rightIndex
      })
  } catch (error) {
    ElMessage.error('加载题库分类失败')
  } finally {
    loadingCategories.value = false
  }
}

async function selectOption(key) {
  if (answered.value) {
    return
  }
  selectedOption.value = key
  if (currentQuestion.value.type === 1) {
    await doSubmit(key)
  }
}

async function submitFillAnswer() {
  if (!userAnswer.value.trim()) {
    ElMessage.warning('请输入答案')
    return
  }
  await doSubmit(userAnswer.value)
}

async function doSubmit(answer) {
  submitting.value = true
  try {
    const result = await questionApi.submitAnswer({
      questionId: currentQuestion.value.id,
      userAnswer: answer,
      timeSpent: elapsed.value
    })
    answerResult.value = result
    answered.value = true
    if (result.correct) {
      correctCount.value++
      totalScore.value += result.scoreGot || 0
    }
  } catch (error) {
    ElMessage.error('提交失败')
  } finally {
    submitting.value = false
  }
}

function nextQuestion() {
  if (currentIndex.value + 1 >= questions.value.length) {
    finished.value = true
    practicing.value = false
    clearTimer()
    return
  }

  currentIndex.value++
  answered.value = false
  answerResult.value = null
  selectedOption.value = ''
  userAnswer.value = ''
}

async function startSelectedPractice() {
  if (!isAdaptiveMode.value && !selectedCategory.value) {
    ElMessage.warning('当前分类题库未准备好')
    return
  }

  loading.value = true
  try {
    const nextQuestions = isAdaptiveMode.value
      ? await questionApi.getAdaptive()
      : await questionApi.getByCategory({
          category: selectedCategory.value,
          size: 8
        })

    if (!nextQuestions || nextQuestions.length === 0) {
      ElMessage.warning('当前题库暂无可练习的题目')
      return
    }

    clearTimer()
    questions.value = nextQuestions
    currentIndex.value = 0
    answered.value = false
    answerResult.value = null
    selectedOption.value = ''
    userAnswer.value = ''
    totalScore.value = 0
    correctCount.value = 0
    elapsed.value = 0
    finished.value = false
    practicing.value = true
    timer = setInterval(() => {
      elapsed.value++
    }, 1000)
  } catch (error) {
    ElMessage.error('获取题目失败')
  } finally {
    loading.value = false
  }
}

watch(() => route.fullPath, syncModeFromRoute)

onMounted(async () => {
  await loadCategories()
  syncModeFromRoute()
})

onUnmounted(clearTimer)
</script>

<style scoped>
.practice-page {
  --ink-900: #12263f;
  --ink-700: #36506c;
  --ink-500: #6d8195;
  --surface: rgba(255, 255, 255, 0.88);
  --surface-strong: #ffffff;
  --line: rgba(178, 198, 220, 0.72);
  --shadow: 0 26px 60px rgba(41, 70, 108, 0.12);
  --blue: #1f72d8;
  --gold: #f0a03b;
  --green: #2f9d5a;
  --red: #d75b5b;
  min-height: calc(100vh - 120px);
  margin: 0 -24px -24px;
  padding: 28px;
  background:
    radial-gradient(circle at top left, rgba(248, 197, 83, 0.25), transparent 24%),
    radial-gradient(circle at 82% 10%, rgba(49, 141, 255, 0.24), transparent 22%),
    linear-gradient(180deg, #f8fbff 0%, #edf4fb 100%);
  font-family: "Source Han Sans SC", "PingFang SC", "Microsoft YaHei", sans-serif;
}

.practice-shell {
  display: flex;
  flex-direction: column;
  gap: 22px;
}

.hero-banner {
  display: grid;
  gap: 24px;
  border-radius: 30px;
  padding: 34px;
  color: #fff;
  box-shadow: 0 30px 70px rgba(19, 46, 86, 0.18);
}

.hero-banner.landing {
  grid-template-columns: minmax(0, 1.4fr) minmax(300px, 0.9fr);
  background:
    radial-gradient(circle at top right, rgba(255, 209, 127, 0.18), transparent 28%),
    linear-gradient(135deg, #102d4e 0%, #194f83 48%, #247cd0 100%);
}

.hero-banner.result {
  background:
    radial-gradient(circle at top right, rgba(255, 211, 116, 0.22), transparent 28%),
    linear-gradient(135deg, #123154 0%, #1b568d 60%, #2b8bd2 100%);
}

.hero-copy h1 {
  margin: 10px 0 12px;
  font-size: 38px;
  line-height: 1.18;
  letter-spacing: 0.01em;
}

.hero-copy p {
  margin: 0;
  max-width: 720px;
  font-size: 15px;
  line-height: 1.9;
  color: rgba(255, 255, 255, 0.8);
}

.banner-kicker,
.header-kicker {
  display: inline-flex;
  align-items: center;
  min-height: 32px;
  padding: 0 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.12);
  font-size: 12px;
  letter-spacing: 0.1em;
}

.hero-counters,
.board-metrics {
  display: grid;
  gap: 14px;
}

.hero-counters {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.counter-card,
.board-metric {
  border-radius: 22px;
  padding: 18px 20px;
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.12);
  backdrop-filter: blur(10px);
}

.counter-card span,
.board-metric span,
.board-top span {
  display: block;
  color: rgba(255, 255, 255, 0.72);
  font-size: 12px;
  margin-bottom: 8px;
}

.counter-card strong,
.board-metric strong,
.board-top strong {
  display: block;
  font-size: 24px;
  color: #fff;
}

.focus-band,
.question-stage,
.insight-card,
.theme-panel,
.result-dashboard {
  border-radius: 28px;
  background: var(--surface);
  border: 1px solid var(--line);
  box-shadow: var(--shadow);
}

.focus-band {
  display: grid;
  grid-template-columns: 180px minmax(0, 1fr) 280px;
  align-items: center;
  gap: 22px;
  padding: 22px 26px;
}

.compact-shell {
  gap: 16px;
}

.compact-band {
  padding: 18px 22px;
}

.section-label {
  font-size: 12px;
  color: var(--ink-500);
  margin-bottom: 8px;
}

.focus-value {
  font-size: 42px;
  line-height: 1;
  font-weight: 800;
  color: var(--ink-900);
}

.focus-subtitle {
  margin-top: 10px;
  color: var(--ink-700);
  line-height: 1.7;
}

.focus-right {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.pill {
  display: inline-flex;
  align-items: center;
  min-height: 38px;
  padding: 0 14px;
  border-radius: 999px;
  background: #edf3fa;
  color: var(--ink-700);
  font-size: 13px;
}

.pill.primary {
  background: #deefff;
  color: var(--blue);
}

.content-grid,
.landing-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.5fr) minmax(300px, 0.82fr);
  gap: 22px;
}

.answering-grid {
  align-items: start;
}

.question-stage {
  padding: 30px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.92) 0%, rgba(248, 251, 255, 0.92) 100%);
}

.stage-head {
  display: flex;
  align-items: center;
  gap: 18px;
  margin-bottom: 24px;
}

.stage-index {
  width: 80px;
  height: 80px;
  border-radius: 26px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #193d65 0%, #2276cf 100%);
  color: #fff;
  font-size: 28px;
  font-weight: 800;
  box-shadow: 0 18px 34px rgba(32, 89, 154, 0.24);
}

.stage-caption span {
  display: block;
  color: var(--ink-500);
  font-size: 12px;
  margin-bottom: 6px;
}

.stage-caption strong {
  display: -webkit-box;
  overflow: hidden;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  font-size: 22px;
  color: var(--ink-900);
}

.answer-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}

.compact-answer-grid {
  margin-top: 8px;
}

.answer-card {
  position: relative;
  width: 100%;
  min-height: 156px;
  padding: 24px;
  border-radius: 26px;
  border: 1px solid #d6e2ef;
  background:
    linear-gradient(180deg, #ffffff 0%, #f5f9ff 100%);
  display: flex;
  gap: 18px;
  text-align: left;
  cursor: pointer;
  overflow: hidden;
  transition: transform 0.18s ease, box-shadow 0.18s ease, border-color 0.18s ease;
}

.answer-card::before {
  content: "";
  position: absolute;
  inset: 0 auto 0 0;
  width: 8px;
  background: transparent;
  transition: background 0.18s ease;
}

.answer-card:hover {
  transform: translateY(-3px);
  border-color: #77ace4;
  box-shadow: 0 18px 38px rgba(67, 122, 186, 0.16);
}

.answer-card.selected {
  border-color: #2a81db;
  background: linear-gradient(180deg, #eef6ff 0%, #f7fbff 100%);
}

.answer-card.selected::before {
  background: var(--blue);
}

.answer-card.correct {
  border-color: #59af79;
  background: linear-gradient(180deg, #eef9f1 0%, #f9fffb 100%);
}

.answer-card.correct::before {
  background: var(--green);
}

.answer-card.wrong {
  border-color: #e17474;
  background: linear-gradient(180deg, #fff1f1 0%, #fffafb 100%);
}

.answer-card.wrong::before {
  background: var(--red);
}

.answer-key {
  width: 52px;
  height: 52px;
  border-radius: 18px;
  background: #edf5ff;
  color: var(--blue);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: 800;
  flex-shrink: 0;
}

.answer-copy {
  font-size: 16px;
  line-height: 1.85;
  color: var(--ink-900);
}

.writing-zone {
  border-radius: 24px;
  padding: 20px;
  background: #f6f9fd;
  border: 1px solid #dbe6f0;
}

.response-layout {
  display: grid;
  grid-template-columns: minmax(260px, 0.95fr) minmax(0, 1.05fr);
  gap: 16px;
  align-items: stretch;
}

.prompt-card,
.response-card {
  min-height: 100%;
}

.prompt-card {
  border-radius: 24px;
  padding: 20px;
  background: linear-gradient(180deg, #fffaf1 0%, #fff3dd 100%);
  border: 1px solid #ecd7ae;
}

.prompt-label {
  font-size: 12px;
  color: var(--ink-500);
  margin-bottom: 10px;
}

.prompt-text {
  font-size: 18px;
  line-height: 1.9;
  color: var(--ink-900);
  font-weight: 700;
}

.prompt-note {
  margin-top: 14px;
  color: var(--ink-700);
  line-height: 1.8;
}

.writing-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  color: var(--ink-700);
}

.writing-head strong {
  color: var(--ink-900);
}

.feedback-board {
  margin-top: 22px;
  padding: 18px 20px;
  border-radius: 24px;
  line-height: 1.85;
}

.feedback-board.correct {
  background: linear-gradient(180deg, #eff9ed 0%, #f8fff7 100%);
  color: #275a34;
}

.feedback-board.wrong {
  background: linear-gradient(180deg, #fff0f0 0%, #fff8f8 100%);
  color: #7b3434;
}

.feedback-title {
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 8px;
}

.feedback-line + .feedback-line {
  margin-top: 6px;
}

.action-row,
.hero-actions,
.cta-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.action-row {
  margin-top: 26px;
}

.insight-panel {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.insight-card {
  padding: 22px;
}

.insight-card.highlight {
  background:
    radial-gradient(circle at top right, rgba(255, 204, 120, 0.24), transparent 30%),
    linear-gradient(135deg, #14345a 0%, #1d588d 56%, #287cd0 100%);
  color: #fff;
  border-color: rgba(255, 255, 255, 0.08);
}

.card-title {
  font-size: 14px;
  font-weight: 700;
  margin-bottom: 16px;
}

.spotlight-score {
  font-size: 52px;
  line-height: 1;
  font-weight: 900;
}

.spotlight-note {
  margin-top: 10px;
  color: rgba(255, 255, 255, 0.76);
}

.mini-stats {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.mini-stat {
  border-radius: 18px;
  padding: 16px;
  background: #f4f8fc;
}

.mini-stat span {
  display: block;
  font-size: 12px;
  color: var(--ink-500);
  margin-bottom: 6px;
}

.mini-stat strong {
  font-size: 26px;
  color: var(--ink-900);
}

.reminder-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.reminder-item {
  display: flex;
  gap: 10px;
  align-items: flex-start;
  color: var(--ink-700);
  line-height: 1.7;
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  margin-top: 7px;
  flex-shrink: 0;
}

.dot.blue {
  background: var(--blue);
}

.dot.gold {
  background: var(--gold);
}

.dot.green {
  background: var(--green);
}

.landing-board {
  border-radius: 24px;
  padding: 22px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.12);
}

.board-top {
  margin-bottom: 16px;
}

.theme-panel {
  padding: 28px;
}

.theme-panel.emphasize {
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.92) 0%, rgba(248, 252, 255, 0.92) 100%);
}

.theme-panel.contrast {
  background:
    radial-gradient(circle at top right, rgba(255, 209, 127, 0.2), transparent 28%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.92) 0%, rgba(248, 251, 255, 0.92) 100%);
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 22px;
}

.panel-header h2 {
  margin: 8px 0 0;
  color: var(--ink-900);
  font-size: 28px;
}

.feature-grid,
.category-cards {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.feature-box,
.category-card {
  min-height: 190px;
  border-radius: 24px;
  padding: 22px;
  border: 1px solid #d8e3ef;
}

.feature-box.amber {
  background: linear-gradient(180deg, #fff8ec 0%, #fff2d7 100%);
}

.feature-box.blue {
  background: linear-gradient(180deg, #f0f7ff 0%, #ddeeff 100%);
}

.feature-box.green {
  background: linear-gradient(180deg, #eefaf2 0%, #dff2e5 100%);
}

.feature-index {
  font-size: 12px;
  letter-spacing: 0.14em;
  color: var(--ink-500);
  margin-bottom: 18px;
}

.feature-box h3 {
  margin: 0 0 10px;
  font-size: 20px;
  color: var(--ink-900);
}

.feature-box p {
  margin: 0;
  color: var(--ink-700);
  line-height: 1.85;
}

.category-card {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  background: linear-gradient(180deg, #f9fbff 0%, #edf4fb 100%);
}

.category-card span {
  color: var(--ink-500);
  font-size: 13px;
}

.category-card strong {
  font-size: 34px;
  color: var(--ink-900);
}

.category-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.cloud-chip {
  display: inline-flex;
  align-items: center;
  min-height: 40px;
  padding: 0 14px;
  border-radius: 999px;
  background: #edf4fb;
  color: var(--ink-700);
  font-size: 13px;
}

.cloud-chip.active {
  background: #dff0ff;
  color: var(--blue);
}

.mode-note {
  margin-top: 20px;
  padding: 18px 20px;
  border-radius: 20px;
  background: linear-gradient(180deg, #f4f8fd 0%, #edf4fb 100%);
  color: var(--ink-700);
  line-height: 1.8;
}

.result-dashboard {
  padding: 28px;
}

.result-main {
  display: grid;
  grid-template-columns: 280px minmax(0, 1fr);
  gap: 24px;
  align-items: center;
}

.result-score-ring {
  display: flex;
  justify-content: center;
}

.ring-center {
  text-align: center;
}

.ring-number {
  font-size: 34px;
  font-weight: 900;
  color: var(--ink-900);
}

.ring-label {
  font-size: 12px;
  color: var(--ink-500);
}

.summary-title {
  font-size: 22px;
  font-weight: 800;
  color: var(--ink-900);
  margin-bottom: 16px;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.summary-tile {
  border-radius: 22px;
  padding: 18px;
  background: #f6f9fd;
  border: 1px solid #dbe6f0;
}

.summary-tile span {
  display: block;
  font-size: 12px;
  color: var(--ink-500);
  margin-bottom: 6px;
}

.summary-tile strong {
  font-size: 28px;
  color: var(--ink-900);
}

.summary-tile .ok {
  color: var(--green);
}

.summary-tile .bad {
  color: var(--red);
}

.summary-tile .gold {
  color: var(--gold);
}

.result-cta {
  margin-top: 22px;
  padding: 24px 26px;
  border-radius: 24px;
  background: linear-gradient(135deg, #17355a 0%, #2164a1 100%);
  display: flex;
  justify-content: space-between;
  gap: 20px;
  align-items: center;
  color: #fff;
}

.cta-title {
  font-size: 24px;
  font-weight: 800;
  margin-bottom: 8px;
}

.cta-text {
  color: rgba(255, 255, 255, 0.78);
  line-height: 1.8;
}

@media (max-width: 1200px) {
  .practice-page {
    margin: 0 -16px -16px;
    padding: 20px;
  }

  .hero-banner.landing,
  .focus-band,
  .content-grid,
  .landing-grid,
  .result-main,
  .summary-grid,
  .feature-grid,
  .category-cards {
    grid-template-columns: 1fr;
  }

  .result-cta {
    flex-direction: column;
    align-items: flex-start;
  }
}

@media (max-width: 768px) {
  .practice-page {
    margin: 0 -12px -12px;
    padding: 14px;
  }

  .hero-banner,
  .focus-band,
  .question-stage,
  .insight-card,
  .theme-panel,
  .result-dashboard {
    border-radius: 22px;
    padding: 20px 18px;
  }

  .hero-copy h1 {
    font-size: 30px;
  }

  .hero-counters,
  .answer-grid,
  .mini-stats,
  .response-layout {
    grid-template-columns: 1fr;
  }

  .stage-head,
  .panel-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .stage-index {
    width: 70px;
    height: 70px;
    font-size: 24px;
  }

  .focus-right {
    justify-content: flex-start;
  }
}
</style>
