<template>
  <div class="wrong-book-page">
    <div class="page-header">
      <h3>📕 错题本</h3>
      <p>共 {{ total }} 道错题，查漏补缺，逐一攻克</p>
    </div>

    <div v-if="loading" class="loading-wrap">
      <el-skeleton :rows="5" animated />
    </div>

    <div v-else-if="list.length === 0">
      <el-empty description="暂无错题，继续保持！" :image-size="120">
        <template #image><div style="font-size:80px;">🎉</div></template>
        <el-button type="primary" @click="$router.push('/practice')">去练习</el-button>
      </el-empty>
    </div>

    <div v-else>
      <el-card
        v-for="item in list"
        :key="item.recordId"
        shadow="hover"
        class="wrong-card"
      >
        <div class="wrong-header">
          <el-tag :type="getDiffTag(item.difficulty)" size="small">
            {{ getDiffLabel(item.difficulty) }}
          </el-tag>
          <el-tag type="info" size="small" style="margin-left:8px;">{{ item.knowledgeName || '未分类' }}</el-tag>
          <span class="wrong-time">{{ formatDate(item.answerTime) }}</span>
        </div>

        <div class="wrong-title">{{ item.title }}</div>

        <div class="wrong-answers">
          <div class="answer-item wrong">
            <el-icon color="#F56C6C"><Close /></el-icon>
            <span>我的答案：<strong>{{ item.userAnswer || '未作答' }}</strong></span>
          </div>
          <div class="answer-item correct">
            <el-icon color="#67C23A"><Check /></el-icon>
            <span>正确答案：<strong>{{ item.correctAnswer }}</strong></span>
          </div>
        </div>

        <div class="analysis" v-if="item.analysis">
          💡 <span>{{ item.analysis }}</span>
        </div>
      </el-card>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="loadWrongQuestions"
        style="margin-top:20px; justify-content:center;"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/api/request'
import dayjs from 'dayjs'

const list = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = 10
const loading = ref(false)

function getDiffTag(d) { return d === 1 ? 'success' : d === 2 ? 'warning' : 'danger' }
function getDiffLabel(d) { return d === 1 ? '简单' : d === 2 ? '中等' : '困难' }
function formatDate(t) { return t ? dayjs(t).format('MM-DD HH:mm') : '' }

async function loadWrongQuestions(page = 1) {
  loading.value = true
  try {
    const res = await request.get('/question/wrong', { params: { page, size: pageSize } })
    list.value = res.records || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

onMounted(() => loadWrongQuestions(1))
</script>

<style scoped>
.wrong-book-page { max-width: 900px; margin: 0 auto; }
.page-header { margin-bottom: 20px; }
.page-header h3 { font-size: 20px; color: #303133; margin-bottom: 4px; }
.page-header p { color: #909399; font-size: 14px; }
.loading-wrap { padding: 24px; }
.wrong-card { margin-bottom: 16px; }
.wrong-header { display: flex; align-items: center; margin-bottom: 12px; }
.wrong-time { margin-left: auto; font-size: 12px; color: #909399; }
.wrong-title { font-size: 16px; color: #303133; line-height: 1.7; margin-bottom: 14px; font-weight: 500; }
.wrong-answers { display: flex; gap: 16px; flex-wrap: wrap; margin-bottom: 10px; }
.answer-item { display: flex; align-items: center; gap: 6px; font-size: 14px; padding: 6px 12px; border-radius: 6px; }
.answer-item.wrong { background: #fef0f0; color: #F56C6C; }
.answer-item.correct { background: #f0f9eb; color: #67C23A; }
.answer-item strong { font-weight: 600; }
.analysis { font-size: 13px; color: #606266; background: #faf8f0; border-left: 3px solid #E6A23C; padding: 8px 12px; border-radius: 0 6px 6px 0; }
</style>
