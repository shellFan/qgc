<template>
  <div class="risk-page">
    <el-row :gutter="20">
      <el-col :span="14">
        <el-card shadow="hover">
          <template #header><span>限流配置</span></template>
          <el-table :data="rateLimits" stripe style="width: 100%">
            <el-table-column prop="name" label="接口" width="150" />
            <el-table-column prop="maxCount" label="最大次数" width="100" />
            <el-table-column prop="timeWindow" label="时间窗口(秒)" width="120" />
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button size="small" text @click="editLimit(row)">编辑</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card shadow="hover">
          <template #header><span>风控记录</span></template>
          <el-table :data="records" stripe style="width: 100%" max-height="400">
            <el-table-column prop="userId" label="用户ID" width="70" />
            <el-table-column prop="action" label="行为" width="100" />
            <el-table-column prop="reason" label="原因" show-overflow-tooltip />
            <el-table-column prop="status" label="状态" width="70">
              <template #default="{ row }">
                <el-tag :type="row.status === 0 ? 'warning' : 'success'" size="small">{{ row.status === 0 ? '待处理' : '已处理' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="70">
              <template #default="{ row }">
                <el-button v-if="row.status === 0" size="small" text type="primary" @click="handleRecord(row.id)">处理</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 编辑限流弹窗 -->
    <el-dialog v-model="showEdit" title="编辑限流配置" width="400px">
      <el-form :model="limitForm" label-width="100px">
        <el-form-item label="接口名称">
          <el-input v-model="limitForm.name" disabled />
        </el-form-item>
        <el-form-item label="最大次数">
          <el-input-number v-model="limitForm.maxCount" :min="1" :max="1000" />
        </el-form-item>
        <el-form-item label="时间窗口(秒)">
          <el-input-number v-model="limitForm.timeWindow" :min="1" :max="86400" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEdit = false">取消</el-button>
        <el-button type="primary" @click="saveLimit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

const rateLimits = ref([])
const records = ref([])
const showEdit = ref(false)
const limitForm = ref({ key: '', name: '', maxCount: 10, timeWindow: 60 })

const nameMap = {
  'ratelimit.create_campaign': '创建筹款',
  'ratelimit.support': '投喂支持',
  'ratelimit.comment': '发表评论',
  'ratelimit.like': '点赞',
  'ratelimit.share': '分享'
}

async function fetchConfig() {
  try {
    const res = await fetch('/admin/risk/config').then(r => r.json())
    const data = res?.data || {}
    rateLimits.value = Object.entries(data).map(([key, val]) => ({
      key,
      name: nameMap[key] || key,
      maxCount: val.maxCount || val.split('/')[0] || 10,
      timeWindow: val.timeWindow || val.split('/')[1] || 60
    }))
  } catch { /* ignore */ }
}

async function fetchRecords() {
  try {
    const res = await fetch('/admin/risk/records?page=1&size=50').then(r => r.json())
    records.value = res?.data?.records || res?.data || []
  } catch { /* ignore */ }
}

function editLimit(row) {
  limitForm.value = { key: row.key, name: row.name, maxCount: row.maxCount, timeWindow: row.timeWindow }
  showEdit.value = true
}

async function saveLimit() {
  try {
    await fetch('/admin/risk/config', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ key: limitForm.value.key, maxCount: limitForm.value.maxCount, timeWindow: limitForm.value.timeWindow })
    })
    ElMessage.success('保存成功')
    showEdit.value = false
    fetchConfig()
  } catch { ElMessage.error('保存失败') }
}

async function handleRecord(id) {
  try {
    await fetch(`/admin/risk/record/${id}`, { method: 'POST' })
    ElMessage.success('已处理')
    fetchRecords()
  } catch { ElMessage.error('操作失败') }
}

onMounted(() => {
  fetchConfig()
  fetchRecords()
})
</script>

<style scoped>
.risk-page { padding: 0; }
</style>