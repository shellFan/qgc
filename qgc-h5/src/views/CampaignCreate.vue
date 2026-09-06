<template>
  <div class="create-page">
    <van-nav-bar left-arrow @click-left="$router.back()" title="发起筹款" fixed placeholder />

    <van-form @submit="onSubmit" class="create-form">
      <!-- 分类选择 -->
      <van-cell-group inset>
        <van-field v-model="categoryName" is-link readonly label="分类" placeholder="选择分类" @click="showCategoryPicker = true" required />
      </van-cell-group>

      <van-popup v-model:show="showCategoryPicker" round position="bottom">
        <van-picker :columns="categoryColumns" @confirm="onCategoryConfirm" @cancel="showCategoryPicker = false" />
      </van-popup>

      <!-- 基本信息 -->
      <van-cell-group inset style="margin-top: 12px">
        <van-field v-model="form.title" label="标题" placeholder="给你的筹款起个响亮的名字" maxlength="30" show-word-limit required />
        <van-field v-model="form.description" label="介绍" type="textarea" placeholder="说说你的穷鬼故事..." rows="4" maxlength="500" show-word-limit required />
      </van-cell-group>

      <!-- 金额 -->
      <van-cell-group inset style="margin-top: 12px">
        <van-field v-model="form.targetAmountYuan" label="目标金额" type="number" placeholder="单位：元" required>
          <template #button>元</template>
        </van-field>
      </van-cell-group>

      <!-- 时长 -->
      <van-cell-group inset style="margin-top: 12px">
        <van-field v-model="durationName" is-link readonly label="筹款时长" placeholder="选择时长" @click="showDurationPicker = true" required />
      </van-cell-group>

      <van-popup v-model:show="showDurationPicker" round position="bottom">
        <van-picker :columns="durationColumns" @confirm="onDurationConfirm" @cancel="showDurationPicker = false" />
      </van-popup>

      <!-- 选项 -->
      <van-cell-group inset style="margin-top: 12px">
        <van-cell title="允许排行榜" center>
          <template #right-icon><van-switch v-model="form.allowRanking" size="20px" /></template>
        </van-cell>
        <van-cell title="允许评论" center>
          <template #right-icon><van-switch v-model="form.allowComment" size="20px" /></template>
        </van-cell>
      </van-cell-group>

      <!-- 留言 -->
      <van-cell-group inset style="margin-top: 12px">
        <van-field v-model="randomMsg" label="随机骚话" placeholder="摇一摇选一句" readonly is-link @click="shakeMessage" />
      </van-cell-group>

      <!-- 提交 -->
      <div style="padding: 20px 16px">
        <van-button type="danger" block round native-type="submit" :loading="submitting">
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
import { createCampaign, getCategoryList, getRandomMessages } from '@/api'
import { yuanToFen } from '@/utils/money'

const router = useRouter()

const form = ref({
  categoryId: null,
  title: '',
  description: '',
  targetAmountYuan: '',
  durationHours: 24,
  allowRanking: true,
  allowComment: true
})

const categories = ref([])
const categoryName = ref('')
const showCategoryPicker = ref(false)
const durationName = ref('24小时')
const showDurationPicker = ref(false)
const randomMsg = ref('')
const submitting = ref(false)

const categoryColumns = ref([])
const durationColumns = [
  { text: '24小时', value: 24 },
  { text: '48小时', value: 48 },
  { text: '72小时', value: 72 }
]

function onCategoryConfirm({ selectedOptions }) {
  const opt = selectedOptions[0]
  form.value.categoryId = opt.value
  categoryName.value = opt.text
  showCategoryPicker.value = false
}

function onDurationConfirm({ selectedOptions }) {
  const opt = selectedOptions[0]
  form.value.durationHours = opt.value
  durationName.value = opt.text
  showDurationPicker.value = false
}

async function fetchCategories() {
  try {
    const res = await getCategoryList()
    categories.value = res.data || []
    categoryColumns.value = categories.value.map(c => ({ text: c.icon + ' ' + c.name, value: c.id }))
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

async function onSubmit() {
  if (!form.value.categoryId) { showToast('请选择分类'); return }
  if (!form.value.title.trim()) { showToast('请输入标题'); return }
  if (!form.value.description.trim()) { showToast('请输入介绍'); return }
  if (!form.value.targetAmountYuan || parseFloat(form.value.targetAmountYuan) <= 0) { showToast('请输入目标金额'); return }

  submitting.value = true
  try {
    const data = {
      categoryId: form.value.categoryId,
      title: form.value.title,
      description: form.value.description,
      targetAmount: yuanToFen(form.value.targetAmountYuan),
      durationHours: form.value.durationHours,
      allowRanking: form.value.allowRanking ? 1 : 0,
      allowComment: form.value.allowComment ? 1 : 0
    }
    const res = await createCampaign(data)
    showToast('发起成功！')
    router.replace(`/campaign/${res.data.id || res.data}`)
  } catch { /* error handled by interceptor */ } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchCategories()
})
</script>

<style scoped>
.create-page { min-height: 100vh; background: #f5f5f5; }
.create-form { padding-top: 8px; }
</style>