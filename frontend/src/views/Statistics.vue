<template>
  <div class="statistics-page">
    <!-- 顶部总览卡片 -->
    <el-row :gutter="16" class="overview-row">
      <el-col :span="4" v-for="item in overviewCards" :key="item.label">
        <el-card shadow="hover" class="overview-card" :style="{ borderTop: `3px solid ${item.color}` }">
          <div class="ov-icon">{{ item.icon }}</div>
          <div class="ov-num" :style="{ color: item.color }">{{ item.value }}</div>
          <div class="ov-label">{{ item.label }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <!-- 近7天学习趋势图 -->
      <el-col :span="16">
        <el-card shadow="hover">
          <template #header><span>📈 近7天学习趋势</span></template>
          <div ref="chartRef" style="height: 280px;"></div>
        </el-card>
      </el-col>

      <!-- 正确率仪表盘 -->
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header><span>🎯 整体正确率</span></template>
          <div style="text-align:center; padding: 12px 0;">
            <el-progress
              type="dashboard"
              :percentage="Math.round(stats.correctRate || 0)"
              :color="getProgressColor(stats.correctRate)"
              :width="160"
            >
              <template #default>
                <div>
                  <div style="font-size:32px;font-weight:bold;color:#303133;">{{ stats.correctRate || 0 }}%</div>
                  <div style="font-size:13px;color:#909399;">答题正确率</div>
                </div>
              </template>
            </el-progress>
            <el-divider />
            <el-row :gutter="8">
              <el-col :span="12">
                <el-statistic title="答题总数" :value="stats.totalQuestions || 0" suffix="题" />
              </el-col>
              <el-col :span="12">
                <el-statistic title="答对题数" :value="stats.correctQuestions || 0" suffix="题" />
              </el-col>
            </el-row>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <!-- 薄弱知识点雷达/列表 -->
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header><span>⚠️ 薄弱知识点分析</span></template>
          <div v-if="stats.weakPoints && stats.weakPoints.length">
            <div class="weak-header">
              <div class="weak-name">知识点</div>
              <div class="weak-progress-title">正确率进度</div>
              <div class="weak-count">答题数</div>
              <div class="weak-rate">正确率</div>
            </div>
            <div v-for="wp in stats.weakPoints" :key="wp.knowledgeId" class="weak-row">
              <div class="weak-name">{{ wp.knowledgeName }}</div>
              <el-progress
                :percentage="Math.round((wp.correctRate || 0) * 100)"
                :color="getProgressColor(wp.correctRate * 100)"
                :stroke-width="12"
                style="flex:1; margin: 0 12px;"
              />
              <div class="weak-count">{{ wp.totalCount }}题</div>
              <div class="weak-rate" :style="{ color: getProgressColor(wp.correctRate * 100) }">
                {{ Math.round((wp.correctRate || 0) * 100) }}%
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无数据" :image-size="80" />
        </el-card>
      </el-col>

      <!-- 单词学习进度 -->
      <el-col :span="12">
        <el-card v-if="!wordListVisible" shadow="hover">
          <template #header><span>📖 单词学习进度</span></template>
          <div style="text-align:center; padding: 20px 0;">
            <el-progress
              type="circle"
              :percentage="wordMasteryRate"
              :color="'#409EFF'"
              :width="140"
            >
              <template #default>
                <div>
                  <div style="font-size:28px;font-weight:bold;color:#409EFF;">{{ wordMasteryRate }}%</div>
                  <div style="font-size:13px;color:#909399;">掌握率</div>
                </div>
              </template>
            </el-progress>
          </div>
          <el-row :gutter="16">
            <el-col :span="12">
              <el-card shadow="never" class="mini-stat clickable-stat" @click="openWordList('learned')">
                <el-statistic title="已学单词" :value="stats.totalWords || 0" suffix="个">
                  <template #prefix><el-icon color="#409EFF"><Notebook /></el-icon></template>
                </el-statistic>
              </el-card>
            </el-col>
            <el-col :span="12">
              <el-card shadow="never" class="mini-stat clickable-stat" @click="openWordList('mastered')">
                <el-statistic title="已掌握" :value="stats.masteredWords || 0" suffix="个">
                  <template #prefix><el-icon color="#67C23A"><Check /></el-icon></template>
                </el-statistic>
              </el-card>
            </el-col>
          </el-row>
        </el-card>
        <el-card v-else shadow="hover">
          <template #header>
            <div class="word-list-header">
              <span>{{ wordListTitle }}</span>
              <el-button size="small" @click="closeWordList">返回统计</el-button>
            </div>
          </template>
          <el-table
            :data="wordList"
            v-loading="wordListLoading"
            border
            stripe
            max-height="360"
            empty-text="暂无单词记录"
          >
            <el-table-column prop="word" label="单词" min-width="110">
              <template #default="{ row }">
                <strong>{{ row.word }}</strong>
                <span v-if="row.phonetic" class="word-phonetic"> /{{ row.phonetic }}/</span>
              </template>
            </el-table-column>
            <el-table-column prop="translation" label="释义" min-width="180" show-overflow-tooltip />
            <el-table-column prop="category" label="词库" width="80" align="center" />
            <el-table-column prop="studyCount" label="学习次数" width="90" align="center" />
            <el-table-column prop="wrongCount" label="错误次数" width="90" align="center" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import request from '@/api/request'

const stats = ref({})
const chartRef = ref(null)
let chart = null
const wordListVisible = ref(false)
const wordListType = ref('learned')
const wordListLoading = ref(false)
const wordList = ref([])

const wordListTitle = computed(() => (
  wordListType.value === 'mastered' ? '已掌握单词列表' : '已学单词列表'
))

const wordMasteryRate = computed(() => {
  if (!stats.value.totalWords) return 0
  return Math.round((stats.value.masteredWords / stats.value.totalWords) * 100)
})

const overviewCards = computed(() => [
  { icon: '🔥', label: '连续打卡', value: (stats.value.continuousDays || 0) + '天', color: '#F56C6C' },
  { icon: '⭐', label: '累计积分', value: stats.value.totalScore || 0, color: '#E6A23C' },
  { icon: '📖', label: '学习单词', value: (stats.value.totalWords || 0) + '个', color: '#409EFF' },
  { icon: '✅', label: '已掌握', value: (stats.value.masteredWords || 0) + '个', color: '#67C23A' },
  { icon: '📝', label: '答题总数', value: (stats.value.totalQuestions || 0) + '题', color: '#909399' },
  { icon: '🎯', label: '答对题数', value: (stats.value.correctQuestions || 0) + '题', color: '#7c3aed' }
])

function getProgressColor(val) {
  if (val >= 70) return '#67C23A'
  if (val >= 40) return '#E6A23C'
  return '#F56C6C'
}

async function openWordList(type) {
  wordListType.value = type
  wordListVisible.value = true
  wordListLoading.value = true
  try {
    wordList.value = await request.get(type === 'mastered' ? '/word/mastered' : '/word/learned')
  } catch (e) {
    console.error(e)
    wordList.value = []
  } finally {
    wordListLoading.value = false
  }
}

function closeWordList() {
  wordListVisible.value = false
  wordList.value = []
}

function initChart(weeklyData) {
  if (!chartRef.value) return
  chart = echarts.init(chartRef.value)
  const dates = weeklyData.map(d => d.date?.slice(5))
  const wordCounts = weeklyData.map(d => d.wordCount || 0)
  const qCounts = weeklyData.map(d => d.questionCount || 0)
  const scores = weeklyData.map(d => d.score || 0)

  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['学习单词', '答题数量', '获得积分'] },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: dates },
    yAxis: [
      { type: 'value', name: '数量' },
      { type: 'value', name: '积分' }
    ],
    series: [
      { name: '学习单词', type: 'bar', data: wordCounts, itemStyle: { color: '#409EFF' }, barMaxWidth: 30 },
      { name: '答题数量', type: 'bar', data: qCounts, itemStyle: { color: '#67C23A' }, barMaxWidth: 30 },
      { name: '获得积分', type: 'line', yAxisIndex: 1, data: scores, itemStyle: { color: '#E6A23C' }, smooth: true, lineStyle: { width: 2 } }
    ]
  })
}

onMounted(async () => {
  try {
    stats.value = await request.get('/statistics/overview')
    await nextTick()
    initChart(stats.value.weeklyData || [])
  } catch (e) {
    console.error(e)
  }
})
</script>

<style scoped>
.statistics-page { max-width: 1200px; margin: 0 auto; }
.overview-row { margin-bottom: 4px; }
.overview-card { text-align: center; padding: 8px 0; }
.ov-icon { font-size: 28px; margin-bottom: 6px; }
.ov-num { font-size: 22px; font-weight: bold; margin-bottom: 4px; }
.ov-label { font-size: 12px; color: #909399; }
.weak-header { display: flex; align-items: center; gap: 8px; padding: 0 0 8px; color: #909399; font-size: 12px; font-weight: 600; border-bottom: 1px solid #ebeef5; }
.weak-progress-title { flex: 1; margin: 0 12px; }
.weak-row { display: flex; align-items: center; gap: 8px; padding: 10px 0; border-bottom: 1px solid #f0f0f0; }
.weak-row:last-child { border-bottom: none; }
.weak-name { width: 100px; font-size: 13px; color: #303133; flex-shrink: 0; }
.weak-count { width: 36px; font-size: 12px; color: #909399; flex-shrink: 0; }
.weak-rate { width: 40px; font-size: 13px; font-weight: 600; flex-shrink: 0; text-align: right; }
.mini-stat { text-align: center; }
.clickable-stat { cursor: pointer; transition: transform .16s ease, box-shadow .16s ease, border-color .16s ease; }
.clickable-stat:hover { transform: translateY(-2px); border-color: #409EFF; box-shadow: 0 8px 22px rgba(64, 158, 255, .16); }
.word-list-header { display: flex; align-items: center; justify-content: space-between; }
.word-phonetic { color: #909399; font-size: 12px; font-weight: 400; }
</style>
