<template>
  <div class="page">
    <el-card shadow="never">
      <template #header>
        <span>分享模板</span>
      </template>

      <el-table :data="list" stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="scene" label="场景" width="150">
          <template #default="{ row }">
            <el-tag size="small">{{ sceneMap[row.scene] || row.scene }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" width="200" />
        <el-table-column prop="content" label="内容" min-width="300" show-overflow-tooltip />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button size="small" text @click="openDialog(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="dialogVisible" title="编辑分享模板" width="560px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="场景">
          <el-input :model-value="sceneMap[form.scene] || form.scene" disabled />
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="分享标题" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="form.content" type="textarea" placeholder="分享内容模板" rows="5" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveForm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getShareTemplateList, updateShareTemplate } from '@/api'

const sceneMap = {
  CAMPAIGN_SHARE: '筹款分享',
  PROOF_SHARE: '返图分享',
  INVITE: '邀请',
  DEFAULT: '默认'
}

const list = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const editingId = ref(null)
const form = ref({ scene: '', title: '', content: '' })

async function fetchList() {
  loading.value = true
  try {
    const res = await getShareTemplateList()
    list.value = res.data || []
  } catch { /* ignore */ } finally { loading.value = false }
}

function openDialog(row) {
  editingId.value = row.id
  form.value = { scene: row.scene, title: row.title, content: row.content }
  dialogVisible.value = true
}

async function saveForm() {
  if (!form.value.title.trim()) { ElMessage.warning('请输入标题'); return }
  try {
    await updateShareTemplate(editingId.value, { title: form.value.title, content: form.value.content })
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchList()
  } catch { ElMessage.error('保存失败') }
}

onMounted(() => { fetchList() })
</script>

<style scoped>
</style>