<template>
  <div class="page">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>敏感词管理</span>
          <div>
            <el-button size="small" @click="showImport = true">批量导入</el-button>
            <el-button type="primary" size="small" @click="openDialog()">新增敏感词</el-button>
          </div>
        </div>
      </template>

      <el-table :data="list" stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="word" label="词语" width="200" />
        <el-table-column label="分类" width="120">
          <template #default="{ row }">
            <el-tag size="small">{{ categoryMap[row.category] || row.category }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.enabled === 1 ? 'success' : 'danger'" size="small">{{ row.enabled === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button size="small" text @click="openDialog(row)">编辑</el-button>
            <el-button size="small" type="danger" text @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        style="margin-top: 16px; justify-content: flex-end"
        v-model:current-page="query.page"
        v-model:page-size="query.size"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="fetchList"
      />
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="editing ? '编辑敏感词' : '新增敏感词'" width="480px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="词语">
          <el-input v-model="form.word" placeholder="敏感词" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.category" placeholder="选择分类" style="width: 100%">
            <el-option v-for="(label, key) in categoryMap" :key="key" :label="label" :value="key" />
          </el-select>
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="form.enabled" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveForm">保存</el-button>
      </template>
    </el-dialog>

    <!-- 批量导入弹窗 -->
    <el-dialog v-model="showImport" title="批量导入敏感词" width="500px">
      <el-form :model="importForm" label-width="80px">
        <el-form-item label="分类">
          <el-select v-model="importForm.category" placeholder="选择分类" style="width: 100%">
            <el-option v-for="(label, key) in categoryMap" :key="key" :label="label" :value="key" />
          </el-select>
        </el-form-item>
        <el-form-item label="词语列表">
          <el-input v-model="importForm.words" type="textarea" placeholder="每行一个敏感词" rows="10" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showImport = false">取消</el-button>
        <el-button type="primary" @click="handleImport">导入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getSensitiveWordList, createSensitiveWord, updateSensitiveWord, deleteSensitiveWord, importSensitiveWords } from '@/api'

const categoryMap = { POLITICS: '政治', PORN: '色情', VIOLENCE: '暴力', AD: '广告', OTHER: '其他' }

const query = ref({ page: 1, size: 20 })
const list = ref([])
const total = ref(0)
const loading = ref(false)
const dialogVisible = ref(false)
const showImport = ref(false)
const editing = ref(null)
const form = ref({ word: '', category: 'OTHER', enabled: true })
const importForm = ref({ category: 'OTHER', words: '' })

async function fetchList() {
  loading.value = true
  try {
    const res = await getSensitiveWordList(query.value)
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch { /* ignore */ } finally { loading.value = false }
}

function openDialog(row) {
  if (row) {
    editing.value = row
    form.value = { word: row.word, category: row.category, enabled: row.enabled === 1 }
  } else {
    editing.value = null
    form.value = { word: '', category: 'OTHER', enabled: true }
  }
  dialogVisible.value = true
}

async function saveForm() {
  if (!form.value.word.trim()) { ElMessage.warning('请输入敏感词'); return }
  try {
    const payload = { ...form.value, enabled: form.value.enabled ? 1 : 0 }
    if (editing.value) {
      await updateSensitiveWord(editing.value.id, payload)
    } else {
      await createSensitiveWord(payload)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchList()
  } catch { ElMessage.error('保存失败') }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定删除此敏感词？', '删除确认', { type: 'warning' })
    await deleteSensitiveWord(row.id)
    ElMessage.success('已删除')
    fetchList()
  } catch { /* cancel */ }
}

async function handleImport() {
  const words = importForm.value.words.split('\n').map(w => w.trim()).filter(Boolean)
  if (!words.length) { ElMessage.warning('请输入敏感词'); return }
  try {
    await importSensitiveWords({ category: importForm.value.category, words })
    ElMessage.success(`成功导入 ${words.length} 个敏感词`)
    showImport.value = false
    importForm.value.words = ''
    fetchList()
  } catch { ElMessage.error('导入失败') }
}

onMounted(() => { fetchList() })
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>