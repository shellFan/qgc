<template>
  <div class="users-page">
    <el-card shadow="never" style="margin-bottom: 16px">
      <el-form :inline="true" :model="query">
        <el-form-item label="搜索">
          <el-input v-model="query.keyword" placeholder="昵称/OpenID" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="正常" :value="1" />
            <el-option label="封禁" :value="2" />
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
        <el-table-column prop="nickname" label="昵称" width="120" />
        <el-table-column prop="openid" label="OpenID" width="180" show-overflow-tooltip />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '正常' : '封禁' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="最后登录" width="160">
          <template #default="{ row }">{{ formatTime(row.lastLoginTime) }}</template>
        </el-table-column>
        <el-table-column label="注册时间" width="160">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 1" size="small" type="danger" @click="handleDisable(row)">封禁</el-button>
            <el-button v-if="row.status === 2" size="small" type="success" @click="handleEnable(row)">解封</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination style="margin-top: 16px; justify-content: flex-end" v-model:current-page="query.page" v-model:page-size="query.size" :total="total" layout="total, prev, pager, next" @current-change="fetchList" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserList, disableUser, enableUser } from '@/api'

function formatTime(t) { return t ? new Date(t).toLocaleString() : '-' }

const query = ref({ keyword: '', status: null, page: 1, size: 20 })
const list = ref([])
const total = ref(0)
const loading = ref(false)

async function fetchList() {
  loading.value = true
  try {
    const res = await getUserList(query.value)
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch { /* ignore */ } finally { loading.value = false }
}

async function handleDisable(row) {
  await ElMessageBox.confirm(`确认封禁用户 ${row.nickname}？`, '封禁')
  await disableUser(row.id)
  ElMessage.success('已封禁')
  fetchList()
}

async function handleEnable(row) {
  await enableUser(row.id)
  ElMessage.success('已解封')
  fetchList()
}

onMounted(() => { fetchList() })
</script>