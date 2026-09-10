<template>
  <div class="proof-create-page">
    <van-nav-bar left-arrow @click-left="$router.back()" title="发布返图" fixed placeholder />

    <div class="form-card">
      <!-- 标题 -->
      <div class="form-group">
        <label class="form-label">返图标题 <span class="required">*</span></label>
        <van-field v-model="form.title" placeholder="请输入返图标题" :maxlength="50" show-word-limit clearable />
      </div>

      <!-- 描述 -->
      <div class="form-group">
        <label class="form-label">详细说明</label>
        <van-field v-model="form.description" type="textarea" placeholder="请描述返图内容，让大家了解资金使用情况" rows="4" :maxlength="500" show-word-limit :autosize="{ minHeight: 80 }" />
      </div>

      <!-- 金额 -->
      <div class="form-group">
        <label class="form-label">使用金额</label>
        <div class="amount-input-box">
          <span class="amount-prefix">¥</span>
          <input v-model="form.amount" type="number" placeholder="0.00" class="amount-input" inputmode="decimal" />
          <span class="amount-suffix">元</span>
        </div>
        <p class="form-hint">填写本次返图对应的资金使用金额</p>
      </div>

      <!-- 图片上传 -->
      <div class="form-group">
        <label class="form-label">上传图片 <span class="required">*</span></label>
        <van-uploader v-model="fileList" :max-count="9" :max-size="10 * 1024 * 1024" @oversize="onOversize" multiple />
        <p class="form-hint">最多上传9张图片，单张不超过10MB</p>
      </div>
    </div>

    <!-- 提交按钮 -->
    <div class="submit-bar">
      <van-button type="primary" block round :loading="submitting" @click="handleSubmit">
        发布返图
      </van-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast, showSuccessToast } from 'vant'
import { createProof } from '@/api'

const route = useRoute()
const router = useRouter()

const form = ref({
  title: '',
  description: '',
  amount: ''
})
const fileList = ref([])
const submitting = ref(false)

function onOversize() {
  showToast('图片大小不能超过10MB')
}

async function handleSubmit() {
  if (!form.value.title.trim()) {
    return showToast('请输入返图标题')
  }
  if (fileList.value.length === 0) {
    return showToast('请上传至少一张图片')
  }

  submitting.value = true
  try {
    const campaignId = route.params.id || route.params.campaignId
    const data = {
      campaignId,
      title: form.value.title.trim(),
      description: form.value.description.trim(),
      amount: form.value.amount ? parseFloat(form.value.amount) : null,
      images: fileList.value.map(f => f.url || f.content).filter(Boolean)
    }
    await createProof(data)
    showSuccessToast('发布成功')
    setTimeout(() => {
      router.back()
    }, 800)
  } catch (e) {
    showToast(e?.response?.data?.message || '发布失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  if (!route.params.id && !route.params.campaignId) {
    showToast('缺少筹款ID')
  }
})
</script>

<style scoped>
.proof-create-page {
  min-height: 100dvh;
  background: var(--qgc-bg);
  padding-bottom: 80px;
}

/* 表单卡片 */
.form-card {
  background: var(--qgc-bg-white);
  margin: var(--qgc-spacing-sm) var(--qgc-spacing-md);
  border-radius: var(--qgc-radius-md);
  padding: var(--qgc-spacing-lg);
  box-shadow: var(--qgc-shadow-sm);
}

.form-group {
  margin-bottom: var(--qgc-spacing-lg);
}
.form-group:last-child {
  margin-bottom: 0;
}

.form-label {
  display: block;
  font-size: var(--qgc-font-md);
  font-weight: 600;
  color: var(--qgc-text-primary);
  margin-bottom: var(--qgc-spacing-xs);
}
.required {
  color: var(--qgc-danger);
}

.form-hint {
  font-size: var(--qgc-font-xs);
  color: var(--qgc-text-tertiary);
  margin: 4px 0 0;
}

/* 金额输入 */
.amount-input-box {
  display: flex;
  align-items: center;
  background: var(--qgc-bg-grey);
  border-radius: var(--qgc-radius-sm);
  padding: 0 var(--qgc-spacing-md);
  height: 48px;
  border: 1px solid var(--qgc-border-light);
  transition: border-color 0.2s;
}
.amount-input-box:focus-within {
  border-color: var(--qgc-primary);
}
.amount-prefix {
  font-size: 20px;
  font-weight: 700;
  color: var(--qgc-primary);
  margin-right: 4px;
}
.amount-input {
  flex: 1;
  border: none;
  background: transparent;
  font-size: 24px;
  font-weight: 700;
  color: var(--qgc-primary);
  outline: none;
  -webkit-appearance: none;
  min-width: 0;
}
.amount-input::placeholder {
  color: var(--qgc-text-placeholder);
  font-size: 16px;
  font-weight: 400;
}
.amount-suffix {
  font-size: var(--qgc-font-md);
  color: var(--qgc-text-secondary);
  margin-left: 4px;
}

/* Vant field 覆盖 */
:deep(.van-field) {
  background: var(--qgc-bg-grey);
  border-radius: var(--qgc-radius-sm);
  padding: 10px 12px;
  border: 1px solid var(--qgc-border-light);
}
:deep(.van-field:focus-within) {
  border-color: var(--qgc-primary);
}
:deep(.van-field__control) {
  font-size: 16px !important;
}

/* 上传组件 */
:deep(.van-uploader__upload) {
  background: var(--qgc-bg-grey);
  border: 1px dashed var(--qgc-border);
  border-radius: var(--qgc-radius-sm);
}
:deep(.van-uploader__preview) {
  border-radius: var(--qgc-radius-sm);
}

/* 提交栏 */
.submit-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: var(--qgc-spacing-sm) var(--qgc-spacing-md);
  padding-bottom: calc(var(--qgc-spacing-sm) + env(safe-area-inset-bottom));
  background: var(--qgc-bg-white);
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.06);
  z-index: 10;
}
</style>