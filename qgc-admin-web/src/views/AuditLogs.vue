<template>
  <div class="page">
    <el-card shadow="never" style="margin-bottom: 16px">
      <el-form :inline="true" :model="query">
        <el-form-item label="操作者">
          <el-input v-model="query.operator" placeholder="操作者" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item label="模块">
          <el-select v-model="query.module" placeholder="全部" clearable style="width: 140px">
            <el-option label="筹款" value="CAMPAIGN" />
            <el-option label="用户" value="USER" />
            <el-option label="提现" value="WITHDRAW" />
            <el-option label="评论" value="COMMENT" />
            <el-option label="系统" value="SYSTEM" />
            <el-option label="管理员" value="MANAGER" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchList">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <el-table :data="list" stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="operator" label="操作者" width="120" />
        <el-table-column prop="action" label="操作" width="150" />
        <el-table-column label="模块" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ moduleMap[row.module] || row.module }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ip" label="IP" width="140" />
        <el-table-column label="时间" width="160">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button size="small" text @click="viewDetail(row)">详情</el-button>
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

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="日志详情" width="560px">
      <div v-if="detail" class="detail-content">
        <p><b>ID:</b> {{ detail.id }}</p>
        <p><b>操作者:</b> {{ detail.operator }}</p>
        <p><b>操作:</b> {{ detail.action }}</p>
        <p><b>模块:</b> {{ moduleMap[detail.module] || detail.module }}</p>
        <p><b>IP:</b> {{ detail.ip }}</p>
        <p><b>时间:</b> {{ formatTime(detail.createTime) }}</p>
        <p v-if="detail.detail"><b>详情:</b></p>
        <pre v-if="detail.detail" class="detail-json">{{ formatDetail(detail.detail) }}</pre>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAuditLogList, getAuditLogDetail } from '@/api'

const moduleMap = { CAMPAIGN: '筹款', USER: '用户', WITHDRAW: '提现', COMMENT: '评论', SYSTEM: '系统', MANAGER: '管理员' }
function formatTime(t) { return t ? new Date(t).toLocaleString() : '-' }
function formatDetail(d) {
  if (typeof d === 'string') return d
  try { return JSON.stringify(d, null, 2) } catch { return String(d) }
}

const query = ref({ operator: '', module: '', page: 1, size: 20 })
const list = ref([])
const total = ref(0)
const loading = ref(false)
const detailVisible = ref(false)
const detail = ref(null)

async function fetchList() {
  loading.value = true
  try {
    const res = await getAuditLogList(query.value)
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch { /* ignore */ } finally { loading.value = false }
}

async function viewDetail(row) {
  try {
    const res = await getAuditLogDetail(row.id)
    detail.value = res.data || row
  } catch {
    detail.value = row
  }
  detailVisible.value = true
}

onMounted(() => { fetchList() })
</script>

<style scoped>
.detail-content p { margin-bottom: 8px; line-height: 1.6; }
.detail-json { background: #f5f5f5; padding: 12px; border-radius: 4px; font-size: 12px; overflow-x: auto; }
</style>