<template>
  <div class="create-page">
    <van-nav-bar left-arrow @click-left="$router.back()" title="发起筹款" fixed placeholder />

    <van-form @submit="onSubmit" class="create-form">
      <!-- 分类选择 -->
      <div class="form-section">
        <div class="form-label">选择分类 <span class="required">*</span></div>
        <div class="category-grid">
          <div
            v-for="cat in categories"
            :key="cat.id"
            class="category-chip"
            :class="{ active: form.categoryId === cat.id }"
            @click="selectCategory(cat)"
          >
            <span class="chip-icon">{{ cat.icon }}</span>
            <span class="chip-name">{{ cat.name }}</span>
          </div>
        </div>
      </div>

      <!-- 基本信息 -->
      <div class="form-section">
        <div class="form-label">筹款标题 <span class="required">*</span></div>
        <van-field v-model="form.title" placeholder="给你的筹款起个响亮的名字" maxlength="30" show-word-limit class="form-field" />
        <div class="form-label form-label-mt">筹款介绍 <span class="required">*</span></div>
        <van-field v-model="form.description" type="textarea" placeholder="说说你的穷鬼故事..." rows="4" maxlength="500" show-word-limit class="form-field" />
      </div>

      <!-- 封面图 -->
      <div class="form-section">
        <div class="form-label">封面图</div>
        <van-uploader
          v-model="coverFileList"
          :max-count="1"
          :max-size="5 * 1024 * 1024"
          accept="image/*"
          @oversize="onCoverOversize"
          :after-read="afterCoverRead"
          :before-delete="beforeCoverDelete"
          class="cover-uploader"
        >
          <div class="upload-trigger">
            <van-icon name="photograph" size="24" color="var(--qgc-text-tertiary)" />
            <span>上传封面</span>
          </div>
        </van-uploader>
        <div class="upload-tip">最多1张，不超过5MB</div>
      </div>

      <!-- 金额 -->
      <div class="form-section">
        <div class="form-label">目标金额 <span class="required">*</span></div>
        <div class="amount-input-wrapper">
          <van-field v-model="form.targetAmountYuan" type="number" placeholder="输入金额" class="amount-field" />
          <span class="amount-unit">元</span>
        </div>
        <div class="amount-presets">
          <van-button v-for="amt in presetAmounts" :key="amt" size="small" round plain type="primary" class="preset-btn" @click="form.targetAmountYuan = String(amt)">{{ amt }}元</van-button>
        </div>
      </div>

      <!-- 时长 -->
      <div class="form-section">
        <div class="form-label">筹款时长 <span class="required">*</span></div>
        <div class="duration-options">
          <div
            v-for="d in durationOptions"
            :key="d.value"
            class="duration-chip"
            :class="{ active: form.durationHours === d.value }"
            @click="form.durationHours = d.value; durationName = d.text"
          >
            {{ d.text }}
          </div>
        </div>
      </div>

      <!-- 选项 -->
      <div class="form-section">
        <div class="option-row">
          <span class="option-label">允许排行榜</span>
          <van-switch v-model="form.allowRanking" size="20px" active-color="var(--qgc-primary)" />
        </div>
        <div class="option-row">
          <span class="option-label">允许评论</span>
          <van-switch v-model="form.allowComment" size="20px" active-color="var(--qgc-primary)" />
        </div>
      </div>

      <!-- 留言 -->
      <div class="form-section">
        <div class="form-label">随机骚话</div>
        <div class="shake-input" @click="shakeMessage">
          <span class="shake-text">{{ randomMsg || '摇一摇选一句' }}</span>
          <van-icon name="shake" color="var(--qgc-primary)" />
        </div>
      </div>

      <!-- 提交 -->
      <div class="submit-area">
        <van-button type="primary" block round native-type="submit" :loading="submitting" class="submit-btn">
          🚀 发起筹款
        </van-button>
      </div>
    </van-form>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { createCampaign, getCategoryList, getRandomMessages, uploadFile } from '@/api'

const router = useRouter()

const form = ref({
  categoryId: null,
  title: '',
  description: '',
  targetAmountYuan: '',
  durationHours: 24,
  allowRanking: true,
  allowComment: true,
  cover: ''
})

const coverFileList = ref([])
const coverUploading = ref(false)
const categories = ref([])
const durationName = ref('24小时')
const randomMsg = ref('')
const submitting = ref(false)

const presetAmounts = [10, 50, 100, 500]
const durationOptions = [
  { text: '24小时', value: 24 },
  { text: '48小时', value: 48 },
  { text: '72小时', value: 72 }
]

function selectCategory(cat) {
  form.value.categoryId = cat.id
}

async function fetchCategories() {
  try {
    const res = await getCategoryList()
    categories.value = res.data || []
  } catch { /* ignore */ }
}

async function shakeMessage() {
  try {
    const res = await getRandomMessages()
    const msgs = res.data || []
    if (msgs.length > 0) {
      randomMsg.value = msgs[Math.floor(Math.random() * msgs.length)].content
    }
  } catch { /* ignore */ }
}

function onCoverOversize() {
  showToast('封面图大小不能超过5MB')
}

async function afterCoverRead(fileItem) {
  const item = Array.isArray(fileItem) ? fileItem[0] : fileItem
  item.status = 'uploading'
  item.message = '上传中'
  coverUploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', item.file)
    const res = await uploadFile(formData)
    const url = res?.data?.url
    if (url) {
      form.value.cover = url
      item.status = 'done'
      item.message = ''
      item.url = url
    } else {
      item.status = 'failed'
      item.message = '上传失败'
      showToast('封面图上传失败')
    }
  } catch (e) {
    item.status = 'failed'
    item.message = '上传失败'
    showToast('封面图上传失败')
  }
  coverUploading.value = false
}

function beforeCoverDelete() {
  form.value.cover = ''
  return true
}

async function onSubmit() {
  if (!form.value.categoryId) { showToast('请选择分类'); return }
  if (!form.value.title.trim()) { showToast('请输入标题'); return }
  if (!form.value.description.trim()) { showToast('请输入介绍'); return }
  if (!form.value.targetAmountYuan || parseFloat(form.value.targetAmountYuan) <= 0) { showToast('请输入目标金额'); return }
  if (coverUploading.value) { showToast('请等待封面图上传完成'); return }

  submitting.value = true
  try {
    const data = {
      categoryId: form.value.categoryId,
      title: form.value.title,
      description: form.value.description,
      targetAmount: parseFloat(form.value.targetAmountYuan).toFixed(2),
      durationHours: form.value.durationHours,
      allowRanking: form.value.allowRanking ? 1 : 0,
      allowComment: form.value.allowComment ? 1 : 0
    }
    if (form.value.cover) {
      data.cover = form.value.cover
    }
    const res = await createCampaign(data)
    showToast('发起成功！')
    router.replace(`/campaign/create-success?id=${res.data.id || res.data}`)
  } catch { /* error handled by interceptor */ } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchCategories()
})
</script>

<style scoped>
.create-page {
  min-height: 100dvh;
  background: var(--qgc-bg);
  padding-bottom: calc(var(--qgc-safe-bottom) + 20px);
}
.create-form {
  padding: var(--qgc-spacing-sm) 0;
}

/* 表单区块 */
.form-section {
  background: var(--qgc-bg-white);
  padding: var(--qgc-spacing-lg);
  margin: var(--qgc-spacing-sm) var(--qgc-spacing-lg);
  border-radius: var(--qgc-radius-md);
  box-shadow: var(--qgc-shadow-sm);
}
.form-label {
  font-size: var(--qgc-font-md);
  font-weight: 600;
  color: var(--qgc-text-primary);
  margin-bottom: var(--qgc-spacing-sm);
}
.form-label-mt {
  margin-top: var(--qgc-spacing-lg);
}
.required {
  color: var(--qgc-danger);
}
.form-field {
  border-radius: var(--qgc-radius-sm);
  background: var(--qgc-bg-grey);
}

/* 分类选择 */
.category-grid {
  display: flex;
  flex-wrap: wrap;
  gap: var(--qgc-spacing-sm);
}
.category-chip {
  display: flex;
  align-items: center;
  gap: var(--qgc-spacing-xs);
  padding: var(--qgc-spacing-sm) var(--qgc-spacing-md);
  border-radius: var(--qgc-radius-full);
  background: var(--qgc-bg-grey);
  font-size: var(--qgc-font-sm);
  color: var(--qgc-text-secondary);
  cursor: pointer;
  transition: all 0.2s;
}
.category-chip.active {
  background: var(--qgc-primary-light);
  color: var(--qgc-primary);
  font-weight: 600;
  border: 1px solid var(--qgc-primary);
}
.chip-icon { font-size: 18px; }
.chip-name { white-space: nowrap; }

/* 封面上传 */
.cover-uploader {
  width: 100%;
}
.cover-uploader :deep(.van-uploader__wrapper) {
  width: 100%;
}
.upload-trigger {
  width: 100%;
  height: 120px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--qgc-spacing-sm);
  background: var(--qgc-bg-grey);
  border-radius: var(--qgc-radius-md);
  border: 1px dashed var(--qgc-border);
  color: var(--qgc-text-tertiary);
  font-size: var(--qgc-font-sm);
}
.upload-tip {
  font-size: var(--qgc-font-xs);
  color: var(--qgc-text-tertiary);
  margin-top: var(--qgc-spacing-xs);
}

/* 金额输入 */
.amount-input-wrapper {
  display: flex;
  align-items: center;
  background: var(--qgc-bg-grey);
  border-radius: var(--qgc-radius-sm);
  overflow: hidden;
}
.amount-field {
  flex: 1;
  background: transparent;
}
.amount-field :deep(.van-field__control) {
  font-size: 24px;
  font-weight: 700;
  color: var(--qgc-primary);
}
.amount-unit {
  padding-right: var(--qgc-spacing-md);
  font-size: var(--qgc-font-lg);
  color: var(--qgc-text-secondary);
  font-weight: 500;
}
.amount-presets {
  display: flex;
  gap: var(--qgc-spacing-sm);
  margin-top: var(--qgc-spacing-sm);
  flex-wrap: wrap;
}
.preset-btn {
  font-size: var(--qgc-font-sm);
}

/* 时长选择 */
.duration-options {
  display: flex;
  gap: var(--qgc-spacing-sm);
}
.duration-chip {
  flex: 1;
  text-align: center;
  padding: var(--qgc-spacing-sm) 0;
  border-radius: var(--qgc-radius-sm);
  background: var(--qgc-bg-grey);
  font-size: var(--qgc-font-md);
  color: var(--qgc-text-secondary);
  cursor: pointer;
  transition: all 0.2s;
}
.duration-chip.active {
  background: var(--qgc-primary);
  color: #fff;
  font-weight: 600;
}

/* 选项行 */
.option-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--qgc-spacing-sm) 0;
}
.option-row + .option-row {
  border-top: 1px solid var(--qgc-border-light);
  padding-top: var(--qgc-spacing-md);
}
.option-label {
  font-size: var(--qgc-font-md);
  color: var(--qgc-text-primary);
}

/* 随机骚话 */
.shake-input {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--qgc-spacing-md);
  background: var(--qgc-bg-grey);
  border-radius: var(--qgc-radius-sm);
  cursor: pointer;
}
.shake-text {
  font-size: var(--qgc-font-sm);
  color: var(--qgc-text-secondary);
  flex: 1;
  margin-right: var(--qgc-spacing-sm);
}

/* 提交 */
.submit-area {
  padding: var(--qgc-spacing-xl) var(--qgc-spacing-lg);
}
.submit-btn {
  height: 48px;
  font-size: var(--qgc-font-lg);
  font-weight: 600;
}
</style>