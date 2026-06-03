<template>
  <div class="word-study-page">

    <!-- ===== 顶部工具栏 ===== -->
    <div class="top-toolbar">
      <!-- 按钮1：选择单词本 -->
      <el-button class="toolbar-btn" @click="openBookSelector">
        <el-icon><Collection /></el-icon>
        <span>{{ currentBookName }}</span>
        <el-icon class="arrow-icon"><ArrowDown /></el-icon>
      </el-button>

      <!-- 按钮2：选择页数 -->
      <el-button class="toolbar-btn" @click="openPageSelector">
        <el-icon><Document /></el-icon>
        <span>第 {{ currentPage }} 页 / 共 {{ totalPages }} 页</span>
        <el-icon class="arrow-icon"><ArrowDown /></el-icon>
      </el-button>

      <!-- 按钮3：错题本 -->
      <el-button class="toolbar-btn" @click="openErrorBook">
        <el-icon><WarningFilled /></el-icon>
        <span>错题本</span>
        <el-badge :value="errorWords.length" :hidden="errorWords.length === 0" class="error-badge" />
      </el-button>

      <!-- 按钮4：开始/暂停 -->
      <el-button
        class="toolbar-btn start-btn"
        :class="{ 'is-active': isStudying }"
        @click="toggleStudy"
      >
        <el-icon>
          <component :is="isStudying ? VideoPause : VideoPlay" />
        </el-icon>
        <span>{{ isStudying ? '暂停' : '开始学习' }}</span>
      </el-button>
    </div>

    <!-- ===== 主体：非错题本模式 ===== -->
    <div v-if="!showErrorBook" class="study-body">

      <!-- 进度条 -->
      <div class="progress-bar-wrap">
        <el-progress
          :percentage="progress"
          :stroke-width="8"
          :show-text="false"
          color="#4F8EF7"
          track-color="#e8edf5"
          style="flex:1"
        />
        <span class="progress-label">{{ studiedCount }} / {{ words.length }} 词</span>
      </div>

      <!-- 单词卡片区 -->
      <div class="card-area" v-if="currentWord && !finished">
        <!-- 卡片容器（翻转效果） -->
        <div
          class="flip-card"
          :class="{ flipped: isFlipped, blurred: !isStudying }"
          @click="isStudying && handleFlip()"
        >
          <!-- 正面 -->
          <div class="flip-front">
            <div class="word-phonetic-row">
              <span class="big-word">{{ currentWord.word }}</span>
              <el-button
                circle
                text
                class="audio-btn"
                @click.stop="playAudio"
                title="朗读"
              >
                <el-icon><Headset /></el-icon>
              </el-button>
            </div>
            <div class="phonetic-text" v-if="currentWord.phonetic">
              /{{ currentWord.phonetic }}/
            </div>
            <div class="category-tag">
              <el-tag size="small" type="info" round>{{ currentWord.category || '通用' }}</el-tag>
            </div>
            <div class="flip-hint" v-if="isStudying">
              <el-icon><Bottom /></el-icon> 点击卡片查看释义
            </div>
            <div class="flip-hint disabled" v-else>点击「开始学习」解锁单词</div>
          </div>

          <!-- 背面 -->
          <div class="flip-back">
            <div class="back-word">{{ currentWord.word }}</div>
            <div class="phonetic-text" v-if="currentWord.phonetic">/{{ currentWord.phonetic }}/</div>
            <div class="translation-text">{{ currentWord.translation }}</div>
            <div class="example-block" v-if="currentWord.example">
              <div class="example-en">{{ currentWord.example }}</div>
              <div class="example-zh">{{ currentWord.exampleTrans }}</div>
            </div>
            <!-- 操作按钮（翻面后显示） -->
            <div class="answer-btns" v-if="isStudying">
              <el-button
                type="danger"
                round
                size="large"
                :loading="submitting"
                @click.stop="markWord(0)"
              >
                😓 不认识
              </el-button>
              <el-button
                type="success"
                round
                size="large"
                :loading="submitting"
                @click.stop="markWord(1)"
              >
                😄 认识了
              </el-button>
            </div>
          </div>
        </div>

        <!-- 单词导航 -->
        <div class="word-nav">
          <el-button circle :disabled="currentIndex === 0" @click="prevWord">
            <el-icon><ArrowLeft /></el-icon>
          </el-button>
          <span class="nav-counter">{{ currentIndex + 1 }} / {{ words.length }}</span>
          <el-button circle :disabled="currentIndex >= words.length - 1" @click="nextWord">
            <el-icon><ArrowRight /></el-icon>
          </el-button>
        </div>
      </div>

      <!-- 学完了 -->
      <div class="finish-area" v-else-if="finished">
        <el-result
          icon="success"
          title="🎉 本页单词全部学完！"
          :sub-title="`已学 ${studiedCount} 词，记住了 ${masteredCount} 词`"
        >
          <template #extra>
            <el-button type="primary" size="large" @click="startSpelling">
              <el-icon><EditPen /></el-icon> 单词拼写
            </el-button>
            <el-button size="large" @click="goNextPage" :disabled="currentPage >= totalPages">
              <el-icon><ArrowRight /></el-icon> 继续学习
            </el-button>
          </template>
        </el-result>
      </div>

      <!-- 空状态 -->
      <div class="finish-area" v-else>
        <el-empty description="当前页暂无单词，请换一个单词本或页码" />
      </div>

      <!-- 右侧单词列表 -->
      <div class="word-list-panel">
        <div class="panel-title">本页单词列表</div>
        <el-scrollbar height="420px">
          <div
            v-for="(w, idx) in words"
            :key="w.id"
            class="word-list-item"
            :class="{
              active: idx === currentIndex,
              mastered: wordStatus[idx] === 1,
              wrong: wordStatus[idx] === 0
            }"
            @click="jumpToWord(idx)"
          >
            <span class="item-num">{{ (currentPage - 1) * pageSize + idx + 1 }}</span>
            <span class="item-word">{{ w.word }}</span>
            <span class="item-trans">{{ w.translation }}</span>
            <el-icon v-if="wordStatus[idx] === 1" class="status-icon ok"><Check /></el-icon>
            <el-icon v-else-if="wordStatus[idx] === 0" class="status-icon err"><Close /></el-icon>
            <el-tag v-else-if="idx === currentIndex" type="primary" size="small" effect="dark">当前</el-tag>
          </div>
        </el-scrollbar>
      </div>
    </div>

    <!-- ===== 错题本视图 ===== -->
    <div v-else class="error-book-view">
      <div class="error-book-header">
        <span class="error-book-title">
          <el-icon><WarningFilled /></el-icon> 单词错题本
        </span>
        <el-button @click="closeErrorBook" type="primary" plain round>
          <el-icon><ArrowLeft /></el-icon> 返回学习
        </el-button>
      </div>

      <el-table
        :data="errorWords"
        v-loading="errorLoading"
        border
        stripe
        style="width:100%"
        class="error-table"
        @row-click="showWordDetail"
      >
        <el-table-column label="单词" prop="word" min-width="130">
          <template #default="{ row }">
            <span class="error-word-text">{{ row.word }}</span>
            <span class="error-phonetic" v-if="row.phonetic"> /{{ row.phonetic }}/</span>
          </template>
        </el-table-column>
        <el-table-column label="释义" prop="translation" min-width="200" />
        <el-table-column label="错误次数" prop="wrongCount" width="100" align="center">
          <template #default="{ row }">
            <el-tag type="danger" effect="dark">{{ row.wrongCount }} 次</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="词典" prop="category" width="100" align="center">
          <template #default="{ row }">
            <el-tag type="info" size="small">{{ row.category || '通用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="90" align="center">
          <template #default="{ row }">
            <el-button
              type="danger"
              text
              size="small"
              @click.stop="removeErrorWord(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="error-empty" v-if="!errorLoading && errorWords.length === 0">
        <el-empty description="太棒了！错题本是空的 🎉" />
      </div>
    </div>

    <!-- ===== 弹窗：选择单词本 ===== -->
    <el-dialog
      v-model="bookDialogVisible"
      title="选择单词本"
      width="620px"
      destroy-on-close
    >
      <div class="book-selector">
        <div class="book-section-title">📚 预设词库</div>
        <div class="book-grid">
          <div
            v-for="book in presetBooks"
            :key="book.category"
            class="book-card"
            :class="{ selected: tempSelectedBook?.category === book.category }"
            @click="selectBook(book)"
          >
            <div class="book-icon">{{ book.icon }}</div>
            <div class="book-name">{{ book.name }}</div>
            <div class="book-desc">{{ book.desc }}</div>
          </div>
        </div>

        <el-divider />

        <div class="book-section-title">🗂 自定义单词本</div>
        <div class="custom-book-area">
          <el-input
            v-model="newBookName"
            placeholder="输入自定义单词本名称"
            style="width:220px;margin-right:10px"
            clearable
          />
          <el-button type="primary" @click="addCustomBook" :disabled="!newBookName.trim()">
            添加
          </el-button>
        </div>
        <div class="book-grid" style="margin-top:12px" v-if="customBooks.length > 0">
          <div
            v-for="book in customBooks"
            :key="book.category"
            class="book-card custom"
            :class="{ selected: tempSelectedBook?.category === book.category }"
            @click="selectBook(book)"
          >
            <div class="book-icon">📖</div>
            <div class="book-name">{{ book.name }}</div>
            <div class="book-actions">
              <el-button
                type="primary"
                text
                size="small"
                @click.stop="openWordManager(book)"
              >管理单词</el-button>
              <el-button
                type="danger"
                text
                size="small"
                @click.stop="removeCustomBook(book)"
              >删除</el-button>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="bookDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmBook">确定</el-button>
      </template>
    </el-dialog>

    <!-- ===== 弹窗：选择页码 ===== -->
    <el-dialog
      v-model="pageDialogVisible"
      title="选择页码"
      width="440px"
      destroy-on-close
    >
      <div class="page-selector">
        <div class="page-size-row">
          <span>每页单词数：</span>
          <el-radio-group v-model="tempPageSize">
            <el-radio-button :value="10">10词</el-radio-button>
            <el-radio-button :value="15">15词</el-radio-button>
            <el-radio-button :value="20">20词</el-radio-button>
            <el-radio-button :value="30">30词</el-radio-button>
          </el-radio-group>
        </div>
        <el-divider />
        <div class="page-grid-label">选择页码：（共 {{ calcTotalPages }} 页）</div>
        <el-scrollbar height="200px">
          <div class="page-grid">
            <div
              v-for="p in calcTotalPages"
              :key="p"
              class="page-item"
              :class="{ selected: p === tempPage }"
              @click="tempPage = p"
            >
              第 {{ p }} 页
            </div>
          </div>
        </el-scrollbar>
      </div>
      <template #footer>
        <el-button @click="pageDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmPage">确定跳转</el-button>
      </template>
    </el-dialog>

    <!-- ===== 弹窗：单词详情 ===== -->
    <el-dialog
      v-model="wordDetailVisible"
      :title="detailWord?.word || '单词详情'"
      width="480px"
      destroy-on-close
    >
      <div class="word-detail" v-if="detailWord">
        <div class="detail-word">{{ detailWord.word }}</div>
        <div class="detail-phonetic" v-if="detailWord.phonetic">/{{ detailWord.phonetic }}/</div>
        <el-divider />
        <div class="detail-row">
          <span class="detail-label">释义</span>
          <span class="detail-value">{{ detailWord.translation }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">词典</span>
          <el-tag type="info" size="small">{{ detailWord.category || '通用' }}</el-tag>
        </div>
        <div class="detail-row">
          <span class="detail-label">错误次数</span>
          <el-tag type="danger">{{ detailWord.wrongCount }} 次</el-tag>
        </div>
        <div class="detail-row">
          <span class="detail-label">学习次数</span>
          <span class="detail-value">{{ detailWord.studyCount }} 次</span>
        </div>
      </div>
    </el-dialog>

    <!-- ===== 弹窗：单词拼写 ===== -->
    <el-dialog
      v-model="spellingVisible"
      title="单词拼写练习"
      width="540px"
      :close-on-click-modal="false"
      destroy-on-close
      @close="closeSpelling"
    >
      <!-- 练习中 -->
      <div v-if="!spellingFinished" class="spelling-body">
        <!-- 进度 -->
        <div class="spelling-progress">
          <el-progress
            :percentage="Math.round((spellingIndex / spellingWords.length) * 100)"
            :stroke-width="6"
            :show-text="false"
            color="#4F8EF7"
            style="flex:1"
          />
          <span class="spelling-counter">{{ spellingIndex + 1 }} / {{ spellingWords.length }}</span>
        </div>

        <!-- 当前单词释义提示 -->
        <div class="spelling-clue" v-if="spellingCurrentWord">
          <div class="clue-label">请根据释义拼写单词：</div>
          <div class="clue-translation">{{ spellingCurrentWord.translation }}</div>
          <div class="clue-phonetic" v-if="spellingCurrentWord.phonetic">
            /{{ spellingCurrentWord.phonetic }}/
          </div>
        </div>

        <!-- 输入框 -->
        <div class="spelling-input-wrap">
          <el-input
            ref="spellingInputRef"
            v-model="spellingInput"
            size="large"
            placeholder="请输入单词..."
            :disabled="spellingResult !== null"
            :class="{
              'spell-correct': spellingResult === true,
              'spell-wrong': spellingResult === false
            }"
            clearable
            autofocus
          />
        </div>

        <!-- 答题结果 -->
        <div class="spelling-feedback" v-if="spellingResult !== null">
          <div v-if="spellingResult === true" class="feedback-correct">
            <el-icon><Check /></el-icon> 正确！
          </div>
          <div v-else class="feedback-wrong">
            <el-icon><Close /></el-icon> 错误，正确拼写：
            <span class="correct-answer">{{ spellingCurrentWord?.word }}</span>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="spelling-actions">
          <el-button
            v-if="spellingResult === null"
            type="primary"
            size="large"
            :disabled="!spellingInput.trim()"
            @click="submitSpelling"
          >
            确认（Enter）
          </el-button>
          <el-button
            v-else
            type="primary"
            size="large"
            @click="nextSpelling"
          >
            {{ spellingIndex + 1 < spellingWords.length ? '下一个（Enter）' : '查看结果（Enter）' }}
          </el-button>
          <el-button size="large" @click="closeSpelling">退出练习</el-button>
        </div>

        <!-- 当前得分 -->
        <div class="spelling-score-row">
          <el-tag type="success">✓ 正确 {{ spellingStats.correct }}</el-tag>
          <el-tag type="danger">✗ 错误 {{ spellingStats.wrong }}</el-tag>
        </div>
      </div>

      <!-- 练习结束 -->
      <div v-else class="spelling-result">
        <el-result
          :icon="spellingStats.wrong === 0 ? 'success' : 'warning'"
          :title="spellingStats.wrong === 0 ? '全部拼写正确！🎉' : '拼写练习完成'"
          :sub-title="`正确 ${spellingStats.correct} 个 / 错误 ${spellingStats.wrong} 个，共 ${spellingWords.length} 词`"
        >
          <template #extra>
            <el-button type="primary" @click="startSpelling">再来一遍</el-button>
            <el-button @click="closeSpelling">关闭</el-button>
          </template>
        </el-result>
      </div>
    </el-dialog>

    <!-- ===== 弹窗：自定义单词本管理 ===== -->
    <el-dialog
      v-model="wordManagerVisible"
      :title="`管理单词本：${managingBook?.name || ''}`"
      width="680px"
      destroy-on-close
    >
      <!-- 手动添加单词 -->
      <div class="wm-add-section">
        <div class="wm-section-title">✏️ 手动添加单词</div>
        <div class="wm-add-row">
          <el-input v-model="wmNewWord.word" placeholder="单词 *" style="width:140px" clearable />
          <el-input v-model="wmNewWord.translation" placeholder="释义 *" style="flex:1" clearable />
          <el-input v-model="wmNewWord.phonetic" placeholder="音标（选填）" style="width:160px" clearable />
          <el-button type="primary" @click="addWordToBook" :disabled="!wmNewWord.word.trim() || !wmNewWord.translation.trim()">
            添加
          </el-button>
        </div>
      </div>

      <el-divider />

      <!-- 导入文件 -->
      <div class="wm-import-section">
        <div class="wm-section-title">
          📥 批量导入
          <el-button text type="primary" size="small" @click="importFormatHintVisible = true">
            查看格式要求
          </el-button>
        </div>
        <div class="wm-import-row">
          <input
            ref="fileInputRef"
            type="file"
            accept=".csv,.xlsx,.xls"
            style="display:none"
            @change="handleFileImport"
          />
          <el-button @click="fileInputRef.click()">
            <el-icon><Upload /></el-icon> 选择 Excel / CSV 文件
          </el-button>
          <span class="wm-import-tip" v-if="importCount > 0">
            已导入 {{ importCount }} 个单词
          </span>
        </div>
      </div>

      <el-divider />

      <!-- 单词列表 -->
      <div class="wm-word-list">
        <div class="wm-section-title">📋 已有单词（{{ wmWords.length }} 个）</div>
        <el-scrollbar height="240px">
          <el-empty v-if="wmWords.length === 0" description="还没有单词，快来添加吧" :image-size="60" />
          <div
            v-for="(w, idx) in wmWords"
            :key="idx"
            class="wm-word-item"
          >
            <span class="wm-word-text">{{ w.word }}</span>
            <span class="wm-phonetic" v-if="w.phonetic">/{{ w.phonetic }}/</span>
            <span class="wm-translation">{{ w.translation }}</span>
            <el-button type="danger" text size="small" @click="removeWordFromBook(idx)">删除</el-button>
          </div>
        </el-scrollbar>
      </div>

      <template #footer>
        <el-button @click="wordManagerVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- ===== 弹窗：导入格式说明 ===== -->
    <el-dialog
      v-model="importFormatHintVisible"
      title="导入格式要求"
      width="500px"
      destroy-on-close
    >
      <div class="format-hint">
        <p>支持 <strong>CSV</strong> 和 <strong>Excel（.xlsx / .xls）</strong> 格式文件。</p>
        <p>文件第一行为表头，从第二行开始为数据，列顺序如下：</p>
        <el-table :data="formatExample" border size="small" style="margin:12px 0">
          <el-table-column label="A列（必填）" prop="word" />
          <el-table-column label="B列（必填）" prop="translation" />
          <el-table-column label="C列（选填）" prop="phonetic" />
        </el-table>
        <div class="format-example">
          <div class="format-row header">
            <span>word</span><span>translation</span><span>phonetic</span>
          </div>
          <div class="format-row">
            <span>apple</span><span>苹果</span><span>ˈæpəl</span>
          </div>
          <div class="format-row">
            <span>book</span><span>书；预订</span><span>bʊk</span>
          </div>
          <div class="format-row muted">
            <span>orange</span><span>橙子</span><span>（可留空）</span>
          </div>
        </div>
        <el-alert type="info" :closable="false" show-icon style="margin-top:12px">
          CSV 文件请使用 UTF-8 编码保存，避免中文乱码。
        </el-alert>
      </div>
      <template #footer>
        <el-button type="primary" @click="importFormatHintVisible = false">我知道了</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, nextTick, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Collection, Document, WarningFilled, VideoPlay, VideoPause,
  ArrowDown, ArrowLeft, ArrowRight, Headset, Bottom,
  Check, Close, EditPen, Upload
} from '@element-plus/icons-vue'
import request from '@/api/request'

// ============================
// 单词本配置
// ============================
const presetBooks = [
  { name: 'CET-4', category: 'CET4', icon: '📘', desc: '大学英语四级' },
  { name: 'CET-6', category: 'CET6', icon: '📗', desc: '大学英语六级' },
  { name: '专四', category: 'TEM4', icon: '📙', desc: '英语专业四级' },
  { name: '专八', category: 'TEM8', icon: '📕', desc: '英语专业八级' },
  { name: '考研英语', category: 'KAOYAN', icon: '🎓', desc: '考研核心词汇' },
  { name: '雅思', category: 'IELTS', icon: '🌏', desc: 'IELTS 核心词汇' },
  { name: '托福', category: 'TOEFL', icon: '🗽', desc: 'TOEFL 核心词汇' },
  { name: '全部', category: '', icon: '📚', desc: '浏览全部单词' },
]

const customBooks = ref(JSON.parse(localStorage.getItem('customWordBooks') || '[]'))
const newBookName = ref('')

function addCustomBook() {
  const name = newBookName.value.trim()
  if (!name) return
  if (customBooks.value.find(b => b.name === name)) {
    ElMessage.warning('该单词本已存在')
    return
  }
  customBooks.value.push({ name, category: 'CUSTOM_' + Date.now(), icon: '📖', desc: '自定义' })
  localStorage.setItem('customWordBooks', JSON.stringify(customBooks.value))
  newBookName.value = ''
  ElMessage.success('已添加自定义单词本')
}

function removeCustomBook(book) {
  customBooks.value = customBooks.value.filter(b => b.category !== book.category)
  localStorage.setItem('customWordBooks', JSON.stringify(customBooks.value))
}

// ============================
// 自定义单词本 - 单词管理
// ============================
const wordManagerVisible = ref(false)
const managingBook = ref(null)
const wmWords = ref([])
const wmNewWord = reactive({ word: '', translation: '', phonetic: '' })
const fileInputRef = ref(null)
const importCount = ref(0)
const importFormatHintVisible = ref(false)

const formatExample = [
  { word: 'word（单词）', translation: 'translation（释义）', phonetic: 'phonetic（音标）' }
]

function openWordManager(book) {
  managingBook.value = book
  const key = `customBookWords_${book.category}`
  wmWords.value = JSON.parse(localStorage.getItem(key) || '[]')
  importCount.value = 0
  wmNewWord.word = ''
  wmNewWord.translation = ''
  wmNewWord.phonetic = ''
  wordManagerVisible.value = true
}

function saveWmWords() {
  if (!managingBook.value) return
  localStorage.setItem(`customBookWords_${managingBook.value.category}`, JSON.stringify(wmWords.value))
  // 若当前正在学习这个自定义单词本，刷新单词列表
  if (selectedBook.value.category === managingBook.value.category) {
    loadWords()
  }
}

function addWordToBook() {
  const word = wmNewWord.word.trim()
  const translation = wmNewWord.translation.trim()
  if (!word || !translation) return
  if (wmWords.value.find(w => w.word.toLowerCase() === word.toLowerCase())) {
    ElMessage.warning('该单词已存在')
    return
  }
  wmWords.value.push({ word, translation, phonetic: wmNewWord.phonetic.trim() })
  saveWmWords()
  wmNewWord.word = ''
  wmNewWord.translation = ''
  wmNewWord.phonetic = ''
  ElMessage.success('添加成功')
}

function removeWordFromBook(idx) {
  wmWords.value.splice(idx, 1)
  saveWmWords()
}

async function handleFileImport(e) {
  const file = e.target.files?.[0]
  if (!file) return
  e.target.value = ''  // 允许再次选同一文件
  const ext = file.name.split('.').pop().toLowerCase()
  let rows = []
  if (ext === 'csv') {
    const text = await file.text()
    rows = text.split('\n').map(line => line.replace(/\r/g, '').split(','))
  } else if (ext === 'xlsx' || ext === 'xls') {
    try {
      const XLSX = await import('xlsx')
      const buf = await file.arrayBuffer()
      const wb = XLSX.read(buf, { type: 'array' })
      const ws = wb.Sheets[wb.SheetNames[0]]
      rows = XLSX.utils.sheet_to_json(ws, { header: 1, defval: '' })
    } catch {
      ElMessage.error('Excel 解析失败，请先执行 npm install xlsx，或改用 CSV 格式')
      return
    }
  } else {
    ElMessage.error('仅支持 .csv / .xlsx / .xls 文件')
    return
  }
  let added = 0
  for (let i = 1; i < rows.length; i++) {
    const row = rows[i]
    const word = String(row[0] || '').trim()
    const translation = String(row[1] || '').trim()
    const phonetic = String(row[2] || '').trim()
    if (!word || !translation) continue
    if (wmWords.value.find(w => w.word.toLowerCase() === word.toLowerCase())) continue
    wmWords.value.push({ word, translation, phonetic })
    added++
  }
  saveWmWords()
  importCount.value = added
  if (added > 0) {
    ElMessage.success(`成功导入 ${added} 个单词`)
  } else {
    ElMessage.warning('未发现可导入的新单词（重复或格式有误）')
  }
}

// ============================
// 状态
// ============================
const words = ref([])
const currentIndex = ref(0)
const isFlipped = ref(false)
const submitting = ref(false)
const finished = ref(false)
const studiedCount = ref(0)
const masteredCount = ref(0)
const wordStatus = ref([])   // 每个单词的学习结果：1=认识, 0=不认识, null=未学

// 学习中/暂停
const isStudying = ref(false)

// 单词本
const selectedBook = ref({ name: 'CET-4', category: 'CET4', icon: '📘', desc: '大学英语四级' })
const tempSelectedBook = ref(null)
const currentBookName = computed(() => selectedBook.value.name)
const bookDialogVisible = ref(false)

// 分页
const currentPage = ref(1)
const pageSize = ref(15)
const totalWords = ref(0)
const totalPages = computed(() => Math.max(1, Math.ceil(totalWords.value / pageSize.value)))
const tempPage = ref(1)
const tempPageSize = ref(15)
const calcTotalPages = computed(() => Math.max(1, Math.ceil(totalWords.value / tempPageSize.value)))
const pageDialogVisible = ref(false)

// 错题本
const showErrorBook = ref(false)
const errorWords = ref([])
const errorLoading = ref(false)
const wordDetailVisible = ref(false)
const detailWord = ref(null)

// 单词拼写
const spellingVisible = ref(false)
const spellingIndex = ref(0)
const spellingInput = ref('')
const spellingResult = ref(null)  // null=未答, true=正确, false=错误
const spellingWords = ref([])     // 本页需要拼写的单词列表
const spellingStats = ref({ correct: 0, wrong: 0 })
const spellingFinished = ref(false)
const spellingInputRef = ref(null)

async function focusSpellingInput() {
  await nextTick()
  spellingInputRef.value?.focus?.()
}

function isCustomBook() {
  return selectedBook.value.category?.startsWith('CUSTOM_')
}

function customErrorBookKey() {
  return `customWordErrors_${selectedBook.value.category}`
}

function loadCustomErrorWords() {
  errorWords.value = JSON.parse(localStorage.getItem(customErrorBookKey()) || '[]')
}

function saveCustomErrorWords() {
  localStorage.setItem(customErrorBookKey(), JSON.stringify(errorWords.value))
}

async function refreshErrorWords() {
  if (isCustomBook()) {
    loadCustomErrorWords()
    return
  }
  const res = await request.get('/word/errors')
  errorWords.value = res || []
}

function updateCustomErrorWord(word, mastered) {
  if (!word) return

  const index = errorWords.value.findIndex(item => item.wordId === word.id)
  if (index === -1) {
    errorWords.value.push({
      wordId: word.id,
      word: word.word,
      phonetic: word.phonetic || '',
      translation: word.translation || '',
      category: word.category || selectedBook.value.name || '自定义',
      wrongCount: mastered === 0 ? 1 : 0,
      studyCount: 1
    })
  } else {
    const item = errorWords.value[index]
    item.studyCount = (item.studyCount || 0) + 1
    if (mastered === 0) {
      item.wrongCount = (item.wrongCount || 0) + 1
    }
  }

  errorWords.value = errorWords.value.filter(item => (item.wrongCount || 0) > 0)
  saveCustomErrorWords()
}

async function recordWordStudy(word, mastered) {
  if (!word) return

  if (isCustomBook()) {
    updateCustomErrorWord(word, mastered)
    return
  }

  await request.post('/word/study', {
    wordId: word.id,
    mastered
  })

  if (mastered === 0 || showErrorBook.value) {
    await refreshErrorWords()
  }
}

// ============================
// 计算属性
// ============================
const currentWord = computed(() => {
  if (finished.value || currentIndex.value >= words.value.length) return null
  return words.value[currentIndex.value]
})

const progress = computed(() => {
  if (!words.value.length) return 0
  return Math.round((studiedCount.value / words.value.length) * 100)
})

// ============================
// 加载单词
// ============================
async function loadWords() {
  finished.value = false
  currentIndex.value = 0
  isFlipped.value = false
  studiedCount.value = 0
  masteredCount.value = 0
  showErrorBook.value = false

  try {
    await refreshErrorWords()
  } catch (e) {
    errorWords.value = []
  }

  // ---- 自定义单词本：从 localStorage 读取，前端分页 ----
  if (isCustomBook()) {
    const all = JSON.parse(
      localStorage.getItem(`customBookWords_${selectedBook.value.category}`) || '[]'
    )
    totalWords.value = all.length
    const start = (currentPage.value - 1) * pageSize.value
    const slice = all.slice(start, start + pageSize.value)
    // 补全字段，保证与后端数据结构一致
    words.value = slice.map((w, i) => ({
      id: `custom_${selectedBook.value.category}_${start + i}`,
      word: w.word,
      phonetic: w.phonetic || '',
      translation: w.translation,
      category: selectedBook.value.name,
      example: '',
      exampleTrans: '',
      audioUrl: '',
    }))
    wordStatus.value = new Array(words.value.length).fill(null)
    return
  }

  // ---- 预设单词本：走后端接口 ----
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
    }
    if (selectedBook.value.category) {
      params.category = selectedBook.value.category
    }
    const res = await request.get('/word/list', { params })
    words.value = res?.records || []
    totalWords.value = res?.total || 0
    wordStatus.value = new Array(words.value.length).fill(null)
  } catch (e) {
    ElMessage.error('加载单词失败，请检查网络')
  }
}

// ============================
// 学习操作
// ============================
function toggleStudy() {
  isStudying.value = !isStudying.value
  if (isStudying.value && words.value.length === 0) {
    loadWords()
  }
}

function handleFlip() {
  if (!isStudying.value) return
  if (!isFlipped.value) {
    // 正面 → 翻到背面
    isFlipped.value = true
  } else {
    // 背面已显示，Enter 默认"认识"
    markWord(1)
  }
}

async function markWord(mastered) {
  if (!currentWord.value || submitting.value) return
  submitting.value = true
  try {
    // 自定义单词本不走后端，直接记录本地状态
    await recordWordStudy(currentWord.value, mastered)
    const previousStatus = wordStatus.value[currentIndex.value]
    if (previousStatus === null) {
      studiedCount.value++
    }
    if (previousStatus !== 1 && mastered === 1) {
      masteredCount.value++
    } else if (previousStatus === 1 && mastered !== 1) {
      masteredCount.value = Math.max(0, masteredCount.value - 1)
    }
    wordStatus.value[currentIndex.value] = mastered

    isFlipped.value = false
    if (wordStatus.value.every(status => status !== null)) {
      finished.value = true
    } else {
      currentIndex.value = findNextUnstudiedIndex(currentIndex.value)
    }
  } catch (e) {
    ElMessage.error('记录失败，请重试')
  } finally {
    submitting.value = false
  }
}

function findNextUnstudiedIndex(fromIndex) {
  const total = wordStatus.value.length
  for (let offset = 1; offset <= total; offset++) {
    const index = (fromIndex + offset) % total
    if (wordStatus.value[index] === null) {
      return index
    }
  }
  return fromIndex
}

function jumpToWord(idx) {
  currentIndex.value = idx
  isFlipped.value = false
  finished.value = false
}

function prevWord() {
  if (currentIndex.value > 0) {
    currentIndex.value--
    isFlipped.value = false
  }
}

function nextWord() {
  if (currentIndex.value < words.value.length - 1) {
    currentIndex.value++
    isFlipped.value = false
  }
}

// ============================
// 单词本弹窗
// ============================
function openBookSelector() {
  tempSelectedBook.value = { ...selectedBook.value }
  bookDialogVisible.value = true
}

function selectBook(book) {
  tempSelectedBook.value = book
}

function confirmBook() {
  if (tempSelectedBook.value) {
    selectedBook.value = { ...tempSelectedBook.value }
    currentPage.value = 1
    loadWords()
  }
  bookDialogVisible.value = false
}

// ============================
// 分页弹窗
// ============================
function openPageSelector() {
  tempPage.value = currentPage.value
  tempPageSize.value = pageSize.value
  pageDialogVisible.value = true
}

function confirmPage() {
  pageSize.value = tempPageSize.value
  currentPage.value = tempPage.value
  loadWords()
  pageDialogVisible.value = false
}

// ============================
// 错题本
// ============================
async function openErrorBook() {
  showErrorBook.value = true
  isStudying.value = false
  errorLoading.value = true
  try {
    await refreshErrorWords()
  } catch (e) {
    ElMessage.error('加载错题本失败')
  } finally {
    errorLoading.value = false
  }
}

function closeErrorBook() {
  showErrorBook.value = false
}

async function removeErrorWord(row) {
  try {
    await ElMessageBox.confirm(
      `确认从错题本移除「${row.word}」？这将清除该单词的错误记录。`,
      '提示',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    if (isCustomBook()) {
      errorWords.value = errorWords.value.filter(w => w.wordId !== row.wordId)
      saveCustomErrorWords()
    } else {
      await request.delete(`/word/errors/${row.wordId}`)
      errorWords.value = errorWords.value.filter(w => w.wordId !== row.wordId)
    }
    ElMessage.success('已移除')
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

function showWordDetail(row) {
  detailWord.value = row
  wordDetailVisible.value = true
}

// ============================
// 音频朗读
// ============================
function playAudio() {
  if (!currentWord.value) return
  const word = currentWord.value.word
  const url = currentWord.value.audioUrl
  if (url) {
    new Audio(url).play()
  } else {
    const utter = new SpeechSynthesisUtterance(word)
    utter.lang = 'en-US'
    speechSynthesis.speak(utter)
  }
}

// ============================
// 继续下一页
// ============================
function goNextPage() {
  if (currentPage.value < totalPages.value) {
    currentPage.value++
    isStudying.value = false
    loadWords()
  }
}

// ============================
// 单词拼写
// ============================
const spellingCurrentWord = computed(() =>
  spellingWords.value[spellingIndex.value] || null
)

/** localStorage key，用页码+单词本区分进度 */
function spellingProgressKey() {
  return `spelling_progress_${selectedBook.value.category || 'all'}_page${currentPage.value}`
}

function saveSpellingProgress() {
  if (spellingFinished.value) return
  localStorage.setItem(spellingProgressKey(), JSON.stringify({
    savedWords: spellingWords.value,
    index: spellingIndex.value,
    stats: spellingStats.value,
    input: spellingInput.value,
    result: spellingResult.value
  }))
}

function startSpelling() {
  const saved = localStorage.getItem(spellingProgressKey())
  if (saved) {
    try {
      const { savedWords, index, stats, input, result } = JSON.parse(saved)
      // 校验保存的单词 id 集合是否与当前页一致
      const currentIds = new Set(words.value.map(w => w.id))
      if (
        Array.isArray(savedWords) &&
        savedWords.length === words.value.length &&
        savedWords.every(w => currentIds.has(w.id)) &&
        index < savedWords.length
      ) {
        spellingWords.value = savedWords
        spellingIndex.value = index
        spellingStats.value = stats || { correct: 0, wrong: 0 }
        spellingInput.value = input || ''
        spellingResult.value = typeof result === 'boolean' ? result : null
        spellingFinished.value = false
        spellingVisible.value = true
        focusSpellingInput()
        ElMessage.info(`已从第 ${index + 1} 个单词继续上次练习`)
        return
      }
    } catch (e) {
      // 解析失败则全新开始
    }
  }
  // 全新开始
  spellingWords.value = [...words.value].sort(() => Math.random() - 0.5)
  spellingIndex.value = 0
  spellingInput.value = ''
  spellingResult.value = null
  spellingStats.value = { correct: 0, wrong: 0 }
  spellingFinished.value = false
  spellingVisible.value = true
  focusSpellingInput()
}

async function submitSpelling() {
  if (!spellingCurrentWord.value) return
  const correct = spellingInput.value.trim().toLowerCase() === spellingCurrentWord.value.word.toLowerCase()
  if (!correct) {
    try {
      await recordWordStudy(spellingCurrentWord.value, 0)
    } catch (e) {
      ElMessage.warning('已判定拼写错误，但加入错题本失败，请稍后重试')
    }
  }
  spellingResult.value = correct
  if (correct) {
    spellingStats.value.correct++
  } else {
    spellingStats.value.wrong++
  }
  saveSpellingProgress()
}

function nextSpelling() {
  spellingResult.value = null
  spellingInput.value = ''
  if (spellingIndex.value + 1 >= spellingWords.value.length) {
    spellingFinished.value = true
    localStorage.removeItem(spellingProgressKey())
  } else {
    spellingIndex.value++
    saveSpellingProgress()
    focusSpellingInput()
  }
}

function closeSpelling() {
  // 中途退出和右上角关闭保持一致：保存当前单词、输入、判题状态和统计。
  if (!spellingFinished.value && spellingWords.value.length > 0) {
    saveSpellingProgress()
  }
  spellingVisible.value = false
}

/** 全局 keydown 监听，仅在拼写弹窗打开时生效 */
function handleSpellingKeydown(e) {
  if (e.key !== 'Enter') return
  e.preventDefault()
  if (spellingResult.value === null) {
    if (spellingInput.value.trim()) submitSpelling()
  } else {
    nextSpelling()
  }
}

// 动态绑定/解绑 Enter 键监听
watch(spellingVisible, (visible) => {
  if (visible) {
    window.addEventListener('keydown', handleSpellingKeydown)
    focusSpellingInput()
  } else {
    window.removeEventListener('keydown', handleSpellingKeydown)
  }
})

watch(spellingInput, () => {
  if (spellingVisible.value && !spellingFinished.value && spellingWords.value.length > 0) {
    saveSpellingProgress()
  }
})

onMounted(() => {
  loadWords()
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleSpellingKeydown)
})
</script>

<style scoped>
/* ===== 页面容器 ===== */
.word-study-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 16px 32px;
  font-family: 'PingFang SC', 'Helvetica Neue', sans-serif;
}

/* ===== 顶部工具栏 ===== */
.top-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 20px;
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.toolbar-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 9px 18px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  background: #f4f6fb;
  border: 1.5px solid #e4e9f5;
  transition: all 0.2s;
  height: auto;
  cursor: pointer;
}
.toolbar-btn:hover {
  background: #ecf0fc;
  border-color: #4F8EF7;
  color: #4F8EF7;
}
.toolbar-btn .arrow-icon {
  font-size: 11px;
  opacity: 0.5;
  margin-left: 2px;
}

/* 开始/暂停 按钮 */
.start-btn {
  margin-left: auto;
  background: linear-gradient(135deg, #4F8EF7, #7B6FEE);
  color: #fff;
  border-color: transparent;
}
.start-btn:hover {
  opacity: 0.9;
  color: #fff;
  border-color: transparent;
}
.start-btn.is-active {
  background: #909399;
  border-color: transparent;
  color: #fff;
}
.start-btn.is-active:hover {
  background: #7d7f83;
  color: #fff;
}
.enter-hint {
  background: rgba(255,255,255,0.25);
  border-radius: 4px;
  padding: 1px 6px;
  font-size: 11px;
  margin-left: 4px;
  font-weight: 400;
  letter-spacing: 0.5px;
}

.error-badge {
  margin-left: 2px;
}

/* ===== 主体布局 ===== */
.study-body {
  display: grid;
  grid-template-columns: 1fr 300px;
  grid-template-rows: auto 1fr;
  gap: 20px;
  align-items: start;
}

/* 进度条跨两列 */
.progress-bar-wrap {
  grid-column: 1 / 3;
  display: flex;
  align-items: center;
  gap: 14px;
  background: #fff;
  padding: 12px 20px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}
.progress-label {
  font-size: 13px;
  color: #909399;
  white-space: nowrap;
}

/* ===== 单词卡片区 ===== */
.card-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

/* 翻转卡片 */
.flip-card {
  width: 100%;
  max-width: 560px;
  height: 340px;
  perspective: 1200px;
  cursor: pointer;
  position: relative;
}
.flip-front,
.flip-back {
  position: absolute;
  inset: 0;
  border-radius: 20px;
  backface-visibility: hidden;
  -webkit-backface-visibility: hidden;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 32px 40px;
  transition: transform 0.55s cubic-bezier(0.4, 0.2, 0.2, 1);
}
.flip-front {
  background: linear-gradient(135deg, #4F8EF7 0%, #7B6FEE 100%);
  color: #fff;
  transform: rotateY(0deg);
}
.flip-back {
  background: linear-gradient(135deg, #2d3a5e 0%, #1a2340 100%);
  color: #fff;
  transform: rotateY(180deg);
}
.flip-card.flipped .flip-front {
  transform: rotateY(-180deg);
}
.flip-card.flipped .flip-back {
  transform: rotateY(0deg);
}

/* 正面内容 */
.word-phonetic-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}
.big-word {
  font-size: 52px;
  font-weight: 700;
  letter-spacing: 1px;
  line-height: 1.1;
}
.audio-btn {
  color: rgba(255,255,255,0.8) !important;
  font-size: 22px;
}
.audio-btn:hover {
  color: #fff !important;
}
.phonetic-text {
  font-size: 18px;
  opacity: 0.75;
  margin-bottom: 10px;
  letter-spacing: 0.5px;
}
.category-tag {
  margin-bottom: 18px;
}
.flip-hint {
  font-size: 13px;
  opacity: 0.6;
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 4px;
}
.flip-hint.disabled {
  opacity: 0.4;
}

/* 背面内容 */
.back-word {
  font-size: 30px;
  font-weight: 700;
  margin-bottom: 4px;
}
.translation-text {
  font-size: 22px;
  font-weight: 500;
  color: #ffd54f;
  margin: 12px 0;
  text-align: center;
  line-height: 1.5;
}
.example-block {
  background: rgba(255,255,255,0.1);
  border-radius: 10px;
  padding: 12px 16px;
  margin-top: 8px;
  text-align: left;
  width: 100%;
}
.example-en {
  font-size: 13px;
  line-height: 1.7;
  opacity: 0.9;
}
.example-zh {
  font-size: 12px;
  opacity: 0.65;
  margin-top: 4px;
}
.answer-btns {
  display: flex;
  gap: 20px;
  margin-top: 16px;
}

/* 导航按钮 */
.word-nav {
  display: flex;
  align-items: center;
  gap: 16px;
}
.nav-counter {
  font-size: 15px;
  color: #606266;
  min-width: 70px;
  text-align: center;
}

/* ===== 完成 ===== */
.finish-area {
  background: #fff;
  border-radius: 20px;
  padding: 40px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.07);
}

/* ===== 右侧单词列表 ===== */
.word-list-panel {
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.07);
  padding: 16px;
  height: fit-content;
}
.panel-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
  padding-bottom: 10px;
  border-bottom: 1px solid #f0f2f5;
}
.word-list-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 6px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.15s;
  font-size: 13px;
}
.word-list-item:hover { background: #f5f7fa; }
.word-list-item.active { background: #ecf2fe; }
.word-list-item.mastered { background: #f0fdf4; }
.word-list-item.wrong { background: #fff5f5; }
.item-num {
  width: 22px;
  color: #c0c4cc;
  font-size: 11px;
  flex-shrink: 0;
  text-align: right;
}
.item-word {
  font-weight: 600;
  color: #303133;
  width: 90px;
  flex-shrink: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.item-trans {
  color: #909399;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 12px;
}
.status-icon {
  flex-shrink: 0;
  font-size: 14px;
}
.status-icon.ok { color: #67c23a; }
.status-icon.err { color: #f56c6c; }

/* ===== 错题本 ===== */
.error-book-view {
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.07);
  padding: 24px;
}
.error-book-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}
.error-book-title {
  font-size: 18px;
  font-weight: 700;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
}
.error-table {
  border-radius: 10px;
  overflow: hidden;
}
.error-word-text {
  font-weight: 600;
  color: #303133;
}
.error-phonetic {
  color: #909399;
  font-size: 12px;
  margin-left: 4px;
}
.error-empty {
  margin-top: 40px;
}

/* ===== 单词本选择弹窗 ===== */
.book-selector {
  padding: 4px 0;
}
.book-section-title {
  font-size: 14px;
  font-weight: 600;
  color: #606266;
  margin-bottom: 14px;
}
.book-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}
.book-card {
  border: 2px solid #ebeef5;
  border-radius: 12px;
  padding: 14px 10px;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
}
.book-card:hover {
  border-color: #4F8EF7;
  background: #f0f5ff;
}
.book-card.selected {
  border-color: #4F8EF7;
  background: #ecf2fe;
  box-shadow: 0 0 0 2px rgba(79,142,247,0.2);
}
.book-icon { font-size: 28px; margin-bottom: 6px; }
.book-name { font-size: 13px; font-weight: 600; color: #303133; }
.book-desc { font-size: 11px; color: #909399; margin-top: 2px; }
.book-card.custom .book-actions {
  margin-top: 6px;
}
.custom-book-area {
  display: flex;
  align-items: center;
}

/* ===== 分页选择弹窗 ===== */
.page-selector {
  padding: 4px 0;
}
.page-size-row {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 4px;
  font-size: 14px;
  color: #606266;
}
.page-grid-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 10px;
}
.page-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 8px;
  padding: 4px;
}
.page-item {
  border: 1.5px solid #ebeef5;
  border-radius: 8px;
  padding: 8px 4px;
  text-align: center;
  font-size: 13px;
  cursor: pointer;
  color: #606266;
  transition: all 0.15s;
}
.page-item:hover {
  border-color: #4F8EF7;
  color: #4F8EF7;
  background: #f0f5ff;
}
.page-item.selected {
  border-color: #4F8EF7;
  background: #4F8EF7;
  color: #fff;
}

/* ===== 单词详情 ===== */
.word-detail { padding: 4px 0; }
.detail-word {
  font-size: 36px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 4px;
}
.detail-phonetic {
  font-size: 16px;
  color: #909399;
  margin-bottom: 4px;
}
.detail-row {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 10px 0;
  border-bottom: 1px solid #f5f7fa;
}
.detail-label {
  font-size: 13px;
  color: #909399;
  width: 70px;
  flex-shrink: 0;
}
.detail-value {
  font-size: 14px;
  color: #303133;
}

/* ===== 卡片模糊（未开始/暂停时） ===== */
.flip-card.blurred .flip-front {
  filter: blur(6px);
  user-select: none;
  pointer-events: none;
}
.flip-card.blurred .flip-front .audio-btn {
  pointer-events: none;
}

/* ===== 单词拼写弹窗 ===== */
.spelling-body {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 4px 0;
}
.spelling-progress {
  display: flex;
  align-items: center;
  gap: 12px;
}
.spelling-counter {
  font-size: 13px;
  color: #909399;
  white-space: nowrap;
}
.spelling-clue {
  background: #f4f6fb;
  border-radius: 12px;
  padding: 18px 22px;
  text-align: center;
}
.clue-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 8px;
}
.clue-translation {
  font-size: 22px;
  font-weight: 600;
  color: #303133;
  line-height: 1.4;
}
.clue-phonetic {
  font-size: 14px;
  color: #909399;
  margin-top: 6px;
}
.spelling-input-wrap {
  padding: 0 4px;
}
.spelling-input-wrap :deep(.el-input__inner) {
  font-size: 20px;
  text-align: center;
  letter-spacing: 2px;
  font-weight: 600;
}
.spell-correct :deep(.el-input__wrapper) {
  box-shadow: 0 0 0 2px #67c23a !important;
  background: #f0fdf4;
}
.spell-wrong :deep(.el-input__wrapper) {
  box-shadow: 0 0 0 2px #f56c6c !important;
  background: #fff5f5;
}
.spelling-feedback {
  text-align: center;
  font-size: 16px;
  font-weight: 600;
}
.feedback-correct {
  color: #67c23a;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}
.feedback-wrong {
  color: #f56c6c;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  flex-wrap: wrap;
}
.correct-answer {
  color: #303133;
  background: #fef3c7;
  padding: 2px 10px;
  border-radius: 6px;
  font-size: 18px;
}
.spelling-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
}
.spelling-score-row {
  display: flex;
  gap: 12px;
  justify-content: center;
}
.spelling-result {
  padding: 8px 0;
}

/* ===== 自定义单词本管理弹窗 ===== */
.wm-section-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.wm-add-section,
.wm-import-section,
.wm-word-list { padding: 2px 0; }
.wm-add-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
.wm-import-row {
  display: flex;
  align-items: center;
  gap: 14px;
}
.wm-import-tip {
  font-size: 13px;
  color: #67c23a;
  font-weight: 500;
}
.wm-word-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 6px;
  border-radius: 8px;
  font-size: 13px;
  transition: background 0.15s;
}
.wm-word-item:hover { background: #f5f7fa; }
.wm-word-text {
  font-weight: 600;
  color: #303133;
  min-width: 90px;
}
.wm-phonetic {
  color: #909399;
  font-size: 12px;
  min-width: 100px;
}
.wm-translation {
  color: #606266;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* ===== 导入格式说明弹窗 ===== */
.format-hint p { margin: 6px 0; font-size: 14px; color: #606266; }
.format-example {
  background: #f4f6fb;
  border-radius: 8px;
  padding: 10px 14px;
  font-family: monospace;
  font-size: 13px;
}
.format-row {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 8px;
  padding: 4px 0;
  border-bottom: 1px solid #ebeef5;
}
.format-row:last-child { border-bottom: none; }
.format-row.header { font-weight: 700; color: #303133; }
.format-row.muted { color: #909399; }
</style>
