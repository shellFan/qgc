<template>
  <div class="proof-create-page">
    <van-nav-bar left-arrow @click-left="$router.back()" title="发布返图" fixed placeholder />

    <van-form @submit="onSubmit">
      <van-cell-group inset>
        <van-field v-model="form.title" label="标题" placeholder="返图标题" maxlength="30" show-word-limit required />
        <van-field v-model="form.content" label="内容" type="textarea" placeholder="说说你的穷鬼成果..." rows="4" maxlength="500" show-word-limit required />
      </van-cell-group>

      <div style="padding: 20px 16px">
        <van-button type="danger" block round native-type="submit" :loading="submitting">
          发布返图
        </van-button>
      </div>
    </van-form>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import { createProof } from '@/api'

const route = useRoute()
const router = useRouter()

const form = ref({ title: '', content: '' })
const submitting = ref(false)

async function onSubmit() {
  if (!form.value.title.trim()) { showToast('请输入标题'); return }
  if (!form.value.content.trim()) { showToast('请输入内容'); return }

  submitting.value = true
  try {
    await createProof({
      campaignId: route.params.campaignId,
      title: form.value.title,
      content: form.value.content
    })
    showToast('返图发布成功')
    router.back()
  } catch { /* error handled */ } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.proof-create-page { min-height: 100vh; background: #f5f5f5; }
</style>