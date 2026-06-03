<template>
  <div class="pronunciation-page">
    <el-row :gutter="24">
      <el-col :span="14">
        <el-card shadow="hover">
          <template #header><span>英语发音评测</span></template>

          <el-form label-position="top">
            <el-form-item label="评测文本">
              <el-input
                v-model="refText"
                type="textarea"
                :rows="3"
                placeholder="请输入要评测的英文内容，例如：Hello, my name is Tom."
              />
            </el-form-item>
          </el-form>

          <div class="preset-wrap">
            <div class="preset-label">快速选择</div>
            <div class="preset-list">
              <el-tag
                v-for="item in presets"
                :key="item"
                class="preset-tag"
                effect="plain"
                type="info"
                @click="refText = item"
              >
                {{ item }}
              </el-tag>
            </div>
          </div>

          <el-divider />

          <div class="record-area">
            <div class="record-status">
              <div class="record-indicator" :class="{ recording: isRecording }"></div>
              <span>{{ isRecording ? '正在录音...' : '点击开始录音' }}</span>
              <span v-if="isRecording" class="record-time">{{ formatTime(recordTime) }}</span>
            </div>

            <div class="record-btns">
              <el-button
                :type="isRecording ? 'danger' : 'primary'"
                :icon="isRecording ? 'VideoPause' : 'Microphone'"
                size="large"
                round
                :disabled="!refText.trim()"
                @click="toggleRecord"
              >
                {{ isRecording ? '停止录音' : '开始录音' }}
              </el-button>
              <el-button
                v-if="audioUrl"
                size="large"
                round
                :icon="'VideoPlay'"
                @click="playAudio"
              >
                回放录音
              </el-button>
              <el-button
                v-if="audioBlob"
                type="success"
                size="large"
                round
                :loading="evaluating"
                @click="submitEvaluation"
              >
                <el-icon><Upload /></el-icon>
                提交评测
              </el-button>
            </div>

            <div v-if="isRecording" class="waveform">
              <div
                v-for="i in 20"
                :key="i"
                class="wave-bar"
                :style="{ height: `${waveHeights[i % waveHeights.length]}px` }"
              ></div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="10">
        <el-card v-if="result" shadow="hover">
          <template #header><span>评测结果（100分制）</span></template>

          <div class="score-overview">
            <el-progress
              type="dashboard"
              :percentage="Math.round(result.totalScore || 0)"
              :color="getScoreColor(result.totalScore)"
              :width="110"
            >
              <template #default>
                <div class="score-center">
                  <div class="score-big">{{ result.totalScore?.toFixed(1) }}</div>
                  <div class="score-label">综合评分 / 100</div>
                </div>
              </template>
            </el-progress>
            <div class="sub-scores">
              <div class="sub-item">
                <span class="sub-label">准确度（65%）</span>
                <el-progress
                  :percentage="Math.round(result.accuracyScore || 0)"
                  :color="getScoreColor(result.accuracyScore)"
                  :show-text="false"
                />
                <span class="sub-val">{{ result.accuracyScore?.toFixed(1) }}</span>
              </div>
              <div class="sub-item">
                <span class="sub-label">流利度（35%）</span>
                <el-progress
                  :percentage="Math.round(result.fluencyScore || 0)"
                  :color="getScoreColor(result.fluencyScore)"
                  :show-text="false"
                />
                <span class="sub-val">{{ result.fluencyScore?.toFixed(1) }}</span>
              </div>
            </div>
          </div>

          <el-divider content-position="left">单词评分</el-divider>
          <div class="word-scores">
            <el-tag
              v-for="ws in result.wordScores"
              :key="`${ws.word}-${ws.score}`"
              class="word-score-tag"
              :type="ws.status === 'correct' ? 'success' : 'danger'"
              size="large"
            >
              {{ ws.word }} <small>{{ ws.score?.toFixed(0) }}</small>
            </el-tag>
          </div>

          <el-divider v-if="result.feedback" content-position="left">AI 建议</el-divider>
          <div v-if="result.feedback" class="feedback-text">{{ result.feedback }}</div>
        </el-card>

        <el-card v-else shadow="hover">
          <el-empty description="录音并提交后，这里会显示发音评分结果" />
        </el-card>

        <el-card shadow="hover" style="margin-top: 16px;">
          <template #header>
            <div class="history-header">
              <span>评测历史</span>
              <span class="history-user">当前账号：{{ currentUsername || '未登录' }}</span>
            </div>
          </template>
          <el-scrollbar height="220px">
            <div v-for="rec in records" :key="rec.id" class="history-item">
              <div class="history-text">{{ rec.wordText }}</div>
              <div class="history-scores">
                <el-tag size="small" :type="getScoreTag(rec.totalScore)">
                  {{ rec.totalScore?.toFixed(1) }}分
                </el-tag>
                <span class="history-time">{{ formatDate(rec.createTime) }}</span>
              </div>
            </div>
            <el-empty
              v-if="!records.length"
              :image-size="60"
              :description="emptyHistoryText"
            />
          </el-scrollbar>
          <div class="history-tip">历史记录按账号隔离，不同账号之间不会互相显示。</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import request from '@/api/request'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()

const refText = ref('')
const isRecording = ref(false)
const evaluating = ref(false)
const result = ref(null)
const records = ref([])
const audioBlob = ref(null)
const audioUrl = ref('')
const audioMimeType = ref('')
const recordTime = ref(0)
const waveHeights = ref(Array.from({ length: 20 }, () => 8))

let mediaRecorder = null
let recordTimer = null
let waveTimer = null
let audioChunks = []

const presets = [
  'Hello, how are you?',
  'The weather is beautiful today.',
  'I enjoy learning English every day.',
  'Technology has changed our lives significantly.',
  'Practice makes perfect.'
]

const currentUsername = computed(() => userStore.userInfo?.username || '')
const emptyHistoryText = computed(() => {
  if (!currentUsername.value) return '请先登录后查看历史记录'
  return `当前账号 ${currentUsername.value} 暂无历史记录`
})

function getScoreColor(score) {
  if (!score) return '#909399'
  if (score >= 80) return '#67C23A'
  if (score >= 60) return '#E6A23C'
  return '#F56C6C'
}

function getScoreTag(score) {
  if (!score) return 'info'
  if (score >= 80) return 'success'
  if (score >= 60) return 'warning'
  return 'danger'
}

function formatTime(seconds) {
  return `${String(Math.floor(seconds / 60)).padStart(2, '0')}:${String(seconds % 60).padStart(2, '0')}`
}

function formatDate(time) {
  return dayjs(time).format('MM-DD HH:mm')
}

async function toggleRecord() {
  if (isRecording.value) {
    stopRecord()
  } else {
    await startRecord()
  }
}

async function startRecord() {
  try {
    const stream = await navigator.mediaDevices.getUserMedia({ audio: true })
    audioChunks = []
    const supportedMimeType = [
      'audio/webm;codecs=opus',
      'audio/webm',
      'audio/mp4'
    ].find(type => window.MediaRecorder?.isTypeSupported?.(type)) || ''

    mediaRecorder = supportedMimeType
      ? new MediaRecorder(stream, { mimeType: supportedMimeType })
      : new MediaRecorder(stream)

    audioMimeType.value = mediaRecorder.mimeType || supportedMimeType || 'audio/webm'
    mediaRecorder.ondataavailable = event => audioChunks.push(event.data)
    mediaRecorder.onstop = () => {
      audioBlob.value = new Blob(audioChunks, { type: audioMimeType.value })
      audioUrl.value = URL.createObjectURL(audioBlob.value)
      stream.getTracks().forEach(track => track.stop())
    }

    mediaRecorder.start()
    isRecording.value = true
    recordTime.value = 0
    recordTimer = setInterval(() => recordTime.value++, 1000)
    waveTimer = setInterval(() => {
      waveHeights.value = Array.from({ length: 20 }, () => 4 + Math.random() * 28)
    }, 150)
  } catch (error) {
    ElMessage.error('无法获取麦克风权限，请检查浏览器设置')
  }
}

function stopRecord() {
  if (mediaRecorder && mediaRecorder.state !== 'inactive') mediaRecorder.stop()
  isRecording.value = false
  clearInterval(recordTimer)
  clearInterval(waveTimer)
  waveHeights.value = Array.from({ length: 20 }, () => 8)
}

function playAudio() {
  if (!audioUrl.value) return
  const audio = new Audio(audioUrl.value)
  audio.play()
}

async function submitEvaluation() {
  if (!audioBlob.value) {
    ElMessage.warning('请先录音')
    return
  }
  if (!refText.value.trim()) {
    ElMessage.warning('请输入评测文本')
    return
  }

  evaluating.value = true
  try {
    const extension = getAudioExtension(audioMimeType.value)
    const formData = new FormData()
    formData.append('audioFile', audioBlob.value, `recording.${extension}`)
    formData.append('refText', refText.value)
    result.value = await request.post('/pronunciation/evaluate', formData)
    await loadRecords()
    ElMessage.success('评测完成')
  } catch (error) {
    // request.js already shows the backend message
  } finally {
    evaluating.value = false
  }
}

function getAudioExtension(mimeType) {
  if (mimeType.includes('mp4')) return 'mp4'
  if (mimeType.includes('ogg')) return 'ogg'
  return 'webm'
}

async function loadRecords() {
  try {
    const res = await request.get('/pronunciation/records', { params: { page: 1, size: 10 } })
    records.value = res.records || []
  } catch (error) {
    records.value = []
  }
}

onMounted(loadRecords)

onUnmounted(() => {
  clearInterval(recordTimer)
  clearInterval(waveTimer)
})
</script>

<style scoped>
.pronunciation-page { max-width: 1200px; margin: 0 auto; }
.preset-wrap { margin-bottom: 12px; }
.preset-label { font-size: 13px; color: #909399; margin-bottom: 8px; }
.preset-list { display: flex; flex-wrap: wrap; gap: 8px; }
.preset-tag { cursor: pointer; }
.preset-tag:hover { opacity: 0.8; }
.record-area { text-align: center; padding: 16px 0; }
.record-status { display: flex; align-items: center; justify-content: center; gap: 10px; margin-bottom: 20px; font-size: 16px; color: #606266; }
.record-indicator { width: 12px; height: 12px; border-radius: 50%; background: #ccc; transition: all 0.3s; }
.record-indicator.recording { background: #F56C6C; animation: pulse 1s infinite; }
@keyframes pulse { 0%, 100% { transform: scale(1); opacity: 1; } 50% { transform: scale(1.3); opacity: 0.7; } }
.record-time { color: #F56C6C; font-weight: bold; }
.record-btns { display: flex; gap: 12px; justify-content: center; flex-wrap: wrap; }
.waveform { display: flex; align-items: center; justify-content: center; gap: 3px; height: 60px; margin-top: 20px; }
.wave-bar { width: 4px; background: #409EFF; border-radius: 2px; transition: height 0.15s; min-height: 4px; }
.score-overview { display: flex; align-items: center; gap: 20px; padding: 12px 0; }
.score-center { text-align: center; }
.score-big { font-size: 26px; font-weight: bold; color: #303133; }
.score-label { font-size: 12px; color: #909399; }
.sub-scores { flex: 1; }
.sub-item { display: flex; align-items: center; gap: 8px; margin-bottom: 10px; }
.sub-label { width: 50px; font-size: 13px; color: #606266; flex-shrink: 0; }
.sub-val { width: 40px; font-size: 13px; font-weight: 600; flex-shrink: 0; text-align: right; }
.word-scores { display: flex; flex-wrap: wrap; gap: 8px; margin: 8px 0; }
.word-score-tag { font-size: 14px; }
.feedback-text { font-size: 14px; line-height: 1.8; color: #303133; background: #f8f9fa; padding: 12px; border-radius: 8px; }
.history-header { display: flex; align-items: center; justify-content: space-between; gap: 12px; }
.history-user { font-size: 12px; color: #909399; }
.history-item { padding: 8px; border-bottom: 1px solid #f0f0f0; }
.history-item:last-child { border-bottom: none; }
.history-text { font-size: 13px; color: #303133; margin-bottom: 4px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.history-scores { display: flex; align-items: center; justify-content: space-between; }
.history-time { font-size: 12px; color: #909399; }
.history-tip { margin-top: 10px; font-size: 12px; color: #909399; }
</style>
