<template>
  <div class="page">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>随机留言</span>
          <div>
            <el-button size="small" @click="showImport = true">批量导入</el-button>
            <el-button type="primary" size="small" @click="openDialog()">新增留言</el-button>
          </div>
        </div>
      </template>

      <el-table :data="list" stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="content" label="内容" min-width="300" show-overflow-tooltip />
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
    <el-dialog v-model="dialogVisible" :title="editing ? '编辑留言' : '新增留言'" width="480px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="内容">
          <el-input v-model="form.content" type="textarea" placeholder="留言内容" rows="4" />
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
    <el-dialog v-model="showImport" title="批量导入留言" width="500px">
      <el-form :model="importForm" label-width="80px">
        <el-form-item label="分类">
          <el-select v-model="importForm.category" placeholder="选择分类" style="width: 100%">
            <el-option v-for="(label, key) in categoryMap" :key="key" :label="label" :value="key" />
          </el-select>
        </el-form-item>
        <el-form-item label="留言列表">
          <el-input v-model="importForm.messages" type="textarea" placeholder="每行一条留言" rows="10" />
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
import { getRandomMessageList, createRandomMessage, updateRandomMessage, deleteRandomMessage, importRandomMessages } from '@/api'

const categoryMap = { ENCOURAGE: '鼓励', COMFORT: '安慰', CHEER: '加油', GENERAL: '通用' }

const query = ref({ page: 1, size: 20 })
const list = ref([])
const total = ref(0)
const loading = ref(false)
const dialogVisible = ref(false)
const showImport = ref(false)
const editing = ref(null)
const form = ref({ content: '', category: 'GENERAL', enabled: true })
const importForm = ref({ category: 'GENERAL', messages: '' })

async function fetchList() {
  loading.value = true
  try {
    const res = await getRandomMessageList(query.value)
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch { /* ignore */ } finally { loading.value = false }
}

function openDialog(row) {
  if (row) {
    editing.value = row
    form.value = { content: row.content, category: row.category, enabled: row.enabled === 1 }
  } else {
    editing.value = null
    form.value = { content: '', category: 'GENERAL', enabled: true }
  }
  dialogVisible.value = true
}

async function saveForm() {
  if (!form.value.content.trim()) { ElMessage.warning('请输入留言内容'); return }
  try {
    const payload = { ...form.value, enabled: form.value.enabled ? 1 : 0 }
    if (editing.value) {
      await updateRandomMessage(editing.value.id, payload)
    } else {
      await createRandomMessage(payload)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchList()
  } catch { ElMessage.error('保存失败') }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定删除此留言？', '删除确认', { type: 'warning' })
    await deleteRandomMessage(row.id)
    ElMessage.success('已删除')
    fetchList()
  } catch { /* cancel */ }
}

async function handleImport() {
  const messages = importForm.value.messages.split('\n').map(m => m.trim()).filter(Boolean)
  if (!messages.length) { ElMessage.warning('请输入留言内容'); return }
  try {
    await importRandomMessages({ category: importForm.value.category, messages })
    ElMessage.success(`成功导入 ${messages.length} 条留言`)
    showImport.value = false
    importForm.value.messages = ''
    fetchList()
  } catch { ElMessage.error('导入失败') }
}

onMounted(() => { fetchList() })
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>