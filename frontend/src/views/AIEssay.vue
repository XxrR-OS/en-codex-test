<template>
  <div class="essay-page">
    <section class="essay-hero">
      <div class="hero-copy">
        <span class="hero-kicker">AI作文批改</span>
        <h1>把总分、短板和前后改写差异直接推到页面中心</h1>
        <p>这个页面不再只是一个输入框加结果卡片，而是围绕“写作诊断”重新组织，让重点信息更容易被看到。</p>
        <div class="hero-actions">
          <el-button type="primary" size="large" :loading="loading" @click="submitEssay" :disabled="!form.content">
            开始批改
          </el-button>
          <el-button size="large" @click="resetForm">清空内容</el-button>
        </div>
      </div>

      <div class="hero-scoreboard">
        <div class="score-tile">
          <span>当前词数</span>
          <strong>{{ wordCount }}</strong>
        </div>
        <div class="score-tile">
          <span>历史记录</span>
          <strong>{{ historyList.length }}</strong>
        </div>
        <div class="score-tile accent" v-if="result">
          <span>最近得分</span>
          <strong>{{ formatScore(result.totalScore) }}</strong>
        </div>
      </div>
    </section>

    <div class="editor-layout">
      <section class="editor-stage">
        <div class="panel-header">
          <div>
            <span class="panel-kicker">写作输入</span>
            <h2>作文编辑区</h2>
          </div>
          <div class="editor-meta">
            <span class="meta-chip">建议 100-400 词</span>
            <span class="meta-chip">最长 2000 字符</span>
          </div>
        </div>

        <el-form :model="form" label-position="top" class="essay-form">
          <el-form-item label="作文题目">
            <el-input v-model="form.topic" placeholder="请输入作文题目，例如：The Importance of Technology" size="large" />
          </el-form-item>
          <el-form-item label="作文内容">
            <el-input
              v-model="form.content"
              type="textarea"
              :rows="18"
              placeholder="请在这里输入你的英语作文内容..."
              show-word-limit
              maxlength="2000"
            />
          </el-form-item>
        </el-form>

        <div class="editor-footer">
          <div class="footer-chip">单词数 {{ wordCount }}</div>
          <div class="footer-chip">字符数 {{ form.content.length }}</div>
          <div class="footer-chip" v-if="result?.createTime">最近批改 {{ formatDate(result.createTime) }}</div>
        </div>
      </section>

      <aside class="history-stage">
        <div class="panel-header compact">
          <div>
            <span class="panel-kicker">作文档案</span>
            <h2>历史记录</h2>
          </div>
          <el-button link @click="loadHistory">刷新</el-button>
        </div>

        <div class="history-list" v-loading="historyLoading">
          <button
            v-for="essay in historyList"
            :key="essay.id"
            type="button"
            class="history-card"
            @click="viewHistory(essay)"
          >
            <div class="history-card-title">{{ essay.topic }}</div>
            <div class="history-card-meta">
              <el-tag size="small" :type="getScoreTag(essay.totalScore)">{{ formatScore(essay.totalScore) }}分</el-tag>
              <span>{{ formatDate(essay.createTime) }}</span>
            </div>
          </button>

          <el-empty v-if="!historyLoading && !historyList.length" :image-size="70" description="暂无历史记录" />
        </div>
      </aside>
    </div>

    <section v-if="loading" class="result-shell loading-shell">
      <div class="panel-header">
        <div>
          <span class="panel-kicker">AI诊断中</span>
          <h2>正在生成评分与修改建议</h2>
        </div>
      </div>
      <el-skeleton :rows="10" animated />
    </section>

    <section v-else-if="result" class="result-shell">
      <div class="result-top">
        <div class="top-main">
          <span class="panel-kicker">批改结果</span>
          <h2>{{ result.topic || form.topic || '未命名作文' }}</h2>
          <p>{{ scoreSummary }}</p>
        </div>
        <div class="total-score-card">
          <el-progress
            type="dashboard"
            :percentage="Math.round(Number(result.totalScore || 0))"
            :color="getScoreColor(result.totalScore)"
            :width="118"
          >
            <template #default>
              <div class="dashboard-center">
                <div class="dashboard-score">{{ formatScore(result.totalScore) }}</div>
                <div class="dashboard-label">总分</div>
              </div>
            </template>
          </el-progress>
        </div>
      </div>

      <div class="result-grid">
        <section class="score-panel">
          <div class="panel-header compact">
            <div>
              <span class="panel-kicker">维度评分</span>
              <h2>四项评分重点</h2>
            </div>
          </div>

          <div class="dimension-grid">
            <article class="dimension-card grammar">
              <span>语法</span>
              <strong>{{ formatScore(result.grammarScore) }}</strong>
              <el-progress :percentage="Math.round(Number(result.grammarScore || 0))" :show-text="false" :color="getScoreColor(result.grammarScore)" />
            </article>
            <article class="dimension-card content">
              <span>内容</span>
              <strong>{{ formatScore(result.contentScore) }}</strong>
              <el-progress :percentage="Math.round(Number(result.contentScore || 0))" :show-text="false" :color="getScoreColor(result.contentScore)" />
            </article>
            <article class="dimension-card structure">
              <span>结构</span>
              <strong>{{ formatScore(result.structureScore) }}</strong>
              <el-progress :percentage="Math.round(Number(result.structureScore || 0))" :show-text="false" :color="getScoreColor(result.structureScore)" />
            </article>
            <article class="dimension-card vocabulary">
              <span>词汇</span>
              <strong>{{ formatScore(result.vocabularyScore) }}</strong>
              <el-progress :percentage="Math.round(Number(result.vocabularyScore || 0))" :show-text="false" :color="getScoreColor(result.vocabularyScore)" />
            </article>
          </div>

          <div class="insight-ribbon">
            <div class="insight-box">
              <span>优势项</span>
              <strong>{{ strongestDimension.label }}</strong>
            </div>
            <div class="insight-box warning">
              <span>优先改进</span>
              <strong>{{ weakestDimension.label }}</strong>
            </div>
          </div>
        </section>

        <section class="feedback-panel">
          <div class="panel-header compact">
            <div>
              <span class="panel-kicker">AI反馈</span>
              <h2>批改意见</h2>
            </div>
          </div>
          <div class="feedback-copy">{{ result.feedback || '暂无反馈' }}</div>
        </section>
      </div>

      <section class="compare-shell" v-if="displayOriginalContent || result.correction">
        <div class="panel-header">
          <div>
            <span class="panel-kicker">前后对照</span>
            <h2>原文与优化稿并排展示</h2>
          </div>
          <div class="compare-tags">
            <span class="meta-chip neutral">左：原文</span>
            <span class="meta-chip success">右：优化稿</span>
          </div>
        </div>

        <div class="compare-grid">
          <article class="compare-card original">
            <div class="compare-title">批改前原文</div>
            <div class="paragraph-stack">
              <p v-for="(line, index) in originalParagraphs" :key="`origin-${index}`">{{ line }}</p>
            </div>
          </article>

          <article class="compare-card revised">
            <div class="compare-title">批改后优化稿</div>
            <div class="paragraph-stack">
              <p v-for="(line, index) in correctionParagraphs" :key="`rev-${index}`">{{ line }}</p>
            </div>
          </article>
        </div>
      </section>
    </section>

    <section v-else class="result-shell empty-shell">
      <el-empty description="输入作文后点击 AI 批改，可查看评分、反馈和前后对照">
        <template #image>
          <div class="empty-mark">✍️</div>
        </template>
      </el-empty>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/api/request'
import dayjs from 'dayjs'

const form = ref({ topic: '', content: '' })
const loading = ref(false)
const historyLoading = ref(false)
const result = ref(null)
const historyList = ref([])

const dimensions = computed(() => [
  { key: 'grammarScore', label: '语法', value: Number(result.value?.grammarScore || 0) },
  { key: 'contentScore', label: '内容', value: Number(result.value?.contentScore || 0) },
  { key: 'structureScore', label: '结构', value: Number(result.value?.structureScore || 0) },
  { key: 'vocabularyScore', label: '词汇', value: Number(result.value?.vocabularyScore || 0) }
])

const wordCount = computed(() => {
  if (!form.value.content.trim()) {
    return 0
  }
  return form.value.content.trim().split(/\s+/).length
})

const displayOriginalContent = computed(() => result.value?.content || form.value.content || '')
const originalParagraphs = computed(() => splitParagraphs(displayOriginalContent.value))
const correctionParagraphs = computed(() => splitParagraphs(result.value?.correction || '暂无优化稿'))
const strongestDimension = computed(() => {
  return [...dimensions.value].sort((a, b) => b.value - a.value)[0] || { label: '暂无', value: 0 }
})
const weakestDimension = computed(() => {
  return [...dimensions.value].sort((a, b) => a.value - b.value)[0] || { label: '暂无', value: 0 }
})
const scoreSummary = computed(() => {
  const totalScore = Number(result.value?.totalScore || 0)
  if (totalScore >= 85) {
    return '整体完成度较高，重点可以转向更高级的句式变化与表达精炼。'
  }
  if (totalScore >= 70) {
    return '主体表达比较清楚，当前应优先修正细节语法和段落衔接。'
  }
  if (totalScore >= 60) {
    return '基础框架已具备，建议先把明显错误压下去，再提升表达层次。'
  }
  return '目前更适合先稳住基础句型和结构，再逐步提升内容与词汇质量。'
})

function splitParagraphs(text) {
  const normalized = String(text || '').replace(/\r/g, '').trim()
  if (!normalized) {
    return ['暂无内容']
  }
  return normalized
    .split('\n')
    .map(item => item.trim())
    .filter(Boolean)
}

function formatDate(value) {
  return value ? dayjs(value).format('MM-DD HH:mm') : ''
}

function formatScore(score) {
  return Number(score || 0).toFixed(1)
}

function getScoreColor(score) {
  const numeric = Number(score || 0)
  if (numeric >= 80) {
    return '#2f9d5a'
  }
  if (numeric >= 60) {
    return '#e1982f'
  }
  return '#d45b5b'
}

function getScoreTag(score) {
  const numeric = Number(score || 0)
  if (numeric >= 80) {
    return 'success'
  }
  if (numeric >= 60) {
    return 'warning'
  }
  return 'danger'
}

function resetForm() {
  form.value = { topic: '', content: '' }
  result.value = null
}

async function submitEssay() {
  if (!form.value.topic.trim()) {
    ElMessage.warning('请输入作文题目')
    return
  }
  if (wordCount.value < 30) {
    ElMessage.warning('作文内容太少，请至少写30个单词')
    return
  }

  loading.value = true
  result.value = null
  try {
    const response = await request.post('/essay/correct', form.value)
    result.value = {
      ...response,
      topic: response.topic || form.value.topic,
      content: response.content || form.value.content
    }
    ElMessage.success('批改完成')
    await loadHistory()
  } catch (error) {
    ElMessage.error('AI批改失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

function viewHistory(essay) {
  result.value = { ...essay }
  form.value.topic = essay.topic || ''
  form.value.content = essay.content || ''
}

async function loadHistory() {
  historyLoading.value = true
  try {
    const response = await request.get('/essay/list', { params: { page: 1, size: 10 } })
    historyList.value = response.records || []
  } catch (error) {
    historyList.value = []
  } finally {
    historyLoading.value = false
  }
}

onMounted(loadHistory)
</script>

<style scoped>
.essay-page {
  --ink-900: #142a45;
  --ink-700: #3d5772;
  --ink-500: #73879d;
  --line: rgba(182, 201, 221, 0.78);
  --shadow: 0 28px 60px rgba(49, 80, 118, 0.12);
  display: flex;
  flex-direction: column;
  gap: 22px;
  font-family: "Source Han Sans SC", "PingFang SC", "Microsoft YaHei", sans-serif;
}

.essay-hero,
.editor-stage,
.history-stage,
.result-shell,
.compare-shell,
.score-panel,
.feedback-panel {
  border-radius: 30px;
  border: 1px solid var(--line);
  box-shadow: var(--shadow);
}

.essay-hero {
  display: grid;
  grid-template-columns: minmax(0, 1.45fr) minmax(300px, 0.75fr);
  gap: 24px;
  padding: 34px;
  background:
    radial-gradient(circle at top right, rgba(255, 204, 120, 0.24), transparent 26%),
    linear-gradient(135deg, #112d4c 0%, #1a4e81 50%, #267bd0 100%);
  color: #fff;
}

.hero-kicker,
.panel-kicker {
  display: inline-flex;
  align-items: center;
  min-height: 32px;
  padding: 0 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.12);
  font-size: 12px;
  letter-spacing: 0.1em;
}

.hero-copy h1 {
  margin: 10px 0 12px;
  font-size: 38px;
  line-height: 1.18;
}

.hero-copy p {
  margin: 0;
  max-width: 740px;
  color: rgba(255, 255, 255, 0.8);
  line-height: 1.9;
}

.hero-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin-top: 24px;
}

.hero-scoreboard {
  display: grid;
  gap: 14px;
}

.score-tile {
  border-radius: 22px;
  padding: 18px 20px;
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.12);
}

.score-tile span {
  display: block;
  color: rgba(255, 255, 255, 0.72);
  font-size: 12px;
  margin-bottom: 8px;
}

.score-tile strong {
  font-size: 30px;
  color: #fff;
}

.score-tile.accent {
  background: rgba(255, 217, 150, 0.16);
}

.editor-layout,
.result-grid,
.compare-grid {
  display: grid;
  gap: 22px;
}

.editor-layout {
  grid-template-columns: minmax(0, 1.45fr) minmax(300px, 0.75fr);
}

.result-grid {
  grid-template-columns: minmax(0, 1.1fr) minmax(320px, 0.9fr);
}

.editor-stage,
.history-stage,
.result-shell,
.compare-shell,
.score-panel,
.feedback-panel {
  background: rgba(255, 255, 255, 0.9);
}

.editor-stage,
.history-stage,
.result-shell,
.compare-shell,
.score-panel,
.feedback-panel {
  padding: 28px;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 18px;
  margin-bottom: 22px;
}

.panel-header.compact {
  margin-bottom: 16px;
}

.panel-header h2 {
  margin: 8px 0 0;
  font-size: 28px;
  color: var(--ink-900);
}

.editor-meta,
.compare-tags {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.meta-chip,
.footer-chip {
  display: inline-flex;
  align-items: center;
  min-height: 36px;
  padding: 0 14px;
  border-radius: 999px;
  background: #edf4fb;
  color: var(--ink-700);
  font-size: 13px;
}

.meta-chip.success {
  background: #e3f5ea;
  color: #2f8b54;
}

.meta-chip.neutral {
  background: #eef2f7;
  color: #5f748a;
}

.editor-footer {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin-top: 14px;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-height: 240px;
}

.history-card {
  width: 100%;
  padding: 16px;
  border-radius: 22px;
  border: 1px solid #dbe6f0;
  background: linear-gradient(180deg, #ffffff 0%, #f6faff 100%);
  text-align: left;
  cursor: pointer;
  transition: transform 0.18s ease, box-shadow 0.18s ease, border-color 0.18s ease;
}

.history-card:hover {
  transform: translateY(-2px);
  border-color: #7db1e5;
  box-shadow: 0 16px 32px rgba(63, 121, 183, 0.14);
}

.history-card-title {
  color: var(--ink-900);
  font-size: 15px;
  font-weight: 700;
  line-height: 1.7;
  margin-bottom: 8px;
}

.history-card-meta {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
  color: var(--ink-500);
  font-size: 12px;
}

.loading-shell {
  min-height: 320px;
}

.result-top {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 210px;
  gap: 16px;
  align-items: center;
  margin-bottom: 18px;
}

.top-main h2 {
  margin: 6px 0 8px;
  font-size: 20px;
  line-height: 1.45;
  color: var(--ink-900);
  max-height: 88px;
  overflow: auto;
}

.top-main p {
  margin: 0;
  color: var(--ink-700);
  line-height: 1.55;
  font-size: 13px;
  max-width: 760px;
}

.total-score-card {
  border-radius: 20px;
  padding: 14px;
  background:
    radial-gradient(circle at top right, rgba(255, 207, 124, 0.2), transparent 28%),
    linear-gradient(135deg, #163457 0%, #1b558b 55%, #2980d2 100%);
  display: flex;
  justify-content: center;
}

.dashboard-center {
  text-align: center;
}

.dashboard-score {
  font-size: 24px;
  font-weight: 900;
  color: #fff;
}

.dashboard-label {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.76);
}

.score-panel {
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.94) 0%, rgba(247, 250, 255, 0.94) 100%);
}

.dimension-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.dimension-card {
  border-radius: 24px;
  padding: 18px 18px 16px;
  border: 1px solid #dbe6f0;
}

.dimension-card.grammar {
  background: linear-gradient(180deg, #fff6ed 0%, #fff0dc 100%);
}

.dimension-card.content {
  background: linear-gradient(180deg, #eef7ff 0%, #deefff 100%);
}

.dimension-card.structure {
  background: linear-gradient(180deg, #f0fbf6 0%, #e1f5ea 100%);
}

.dimension-card.vocabulary {
  background: linear-gradient(180deg, #f8f3ff 0%, #efe5ff 100%);
}

.dimension-card span {
  display: block;
  color: var(--ink-500);
  font-size: 13px;
  margin-bottom: 8px;
}

.dimension-card strong {
  display: block;
  color: var(--ink-900);
  font-size: 30px;
  margin-bottom: 10px;
}

.insight-ribbon {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
  margin-top: 18px;
}

.insight-box {
  border-radius: 22px;
  padding: 18px 20px;
  background: #edf4fb;
}

.insight-box.warning {
  background: #fff3e1;
}

.insight-box span {
  display: block;
  color: var(--ink-500);
  font-size: 12px;
  margin-bottom: 8px;
}

.insight-box strong {
  color: var(--ink-900);
  font-size: 24px;
}

.feedback-copy {
  border-radius: 24px;
  padding: 20px;
  background: linear-gradient(180deg, #f8fbff 0%, #eef4fb 100%);
  color: var(--ink-700);
  border: 1px solid #dbe6f0;
  line-height: 1.95;
  white-space: pre-wrap;
}

.compare-shell {
  margin-top: 22px;
}

.compare-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.compare-card {
  border-radius: 26px;
  padding: 22px;
  border: 1px solid #dbe6f0;
}

.compare-card.original {
  background: linear-gradient(180deg, #ffffff 0%, #f7fafe 100%);
}

.compare-card.revised {
  background: linear-gradient(180deg, #f2fcf4 0%, #e6f7eb 100%);
  border-color: #cfe5d6;
}

.compare-title {
  font-size: 18px;
  font-weight: 800;
  color: var(--ink-900);
  margin-bottom: 14px;
}

.paragraph-stack {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.paragraph-stack p {
  margin: 0;
  padding: 16px 18px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.72);
  color: var(--ink-700);
  line-height: 1.9;
}

.empty-shell {
  min-height: 280px;
}

.empty-mark {
  font-size: 72px;
}

@media (max-width: 1200px) {
  .essay-hero,
  .editor-layout,
  .result-top,
  .result-grid,
  .compare-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .essay-hero,
  .editor-stage,
  .history-stage,
  .result-shell,
  .compare-shell,
  .score-panel,
  .feedback-panel {
    border-radius: 22px;
    padding: 20px 18px;
  }

  .hero-copy h1,
  .top-main h2,
  .panel-header h2 {
    font-size: 28px;
  }

  .hero-scoreboard,
  .dimension-grid,
  .insight-ribbon {
    grid-template-columns: 1fr;
  }

  .panel-header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
