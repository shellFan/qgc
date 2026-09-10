<template>
  <div class="page">
    <el-tabs v-model="activeGroup" @tab-change="fetchGroup">
      <el-tab-pane v-for="g in groups" :key="g.key" :label="g.label" :name="g.key" />
    </el-tabs>

    <el-card shadow="never" v-loading="loading" style="margin-top: 16px">
      <template #header>
        <div class="card-header">
          <span>{{ currentLabel }} 配置</span>
          <el-button type="primary" size="small" @click="saveGroup">保存配置</el-button>
        </div>
      </template>

      <el-form label-width="160px">
        <el-form-item v-for="item in configs" :key="item.key" :label="item.label || item.key">
          <template v-if="item.valueType === 'BOOLEAN'">
            <el-switch v-model="item._value" />
          </template>
          <template v-else-if="item.valueType === 'NUMBER'">
            <el-input-number v-model="item._value" :min="0" style="width: 200px" />
          </template>
          <template v-else>
            <el-input v-model="item._value" style="width: 400px" />
          </template>
          <div v-if="item.description" class="config-desc">{{ item.description }}</div>
        </el-form-item>
        <el-empty v-if="!configs.length" description="暂无配置项" />
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { getSystemConfigByGroup, updateSystemConfig } from '@/api'

const groups = [
  { key: 'AUDIT', label: '审核' },
  { key: 'LIMIT', label: '限额' },
  { key: 'POINT', label: '积分' },
  { key: 'LEVEL', label: '等级' },
  { key: 'BADGE', label: '徽章' },
  { key: 'RATE_LIMIT', label: '限流' },
  { key: 'WITHDRAW', label: '提现' }
]

const activeGroup = ref('AUDIT')
const configs = ref([])
const loading = ref(false)

const currentLabel = computed(() => {
  const g = groups.find(g => g.key === activeGroup.value)
  return g ? g.label : ''
})

async function fetchGroup() {
  loading.value = true
  try {
    const res = await getSystemConfigByGroup(activeGroup.value)
    const items = res.data || []
    configs.value = items.map(item => {
      let val = item.value
      if (item.valueType === 'BOOLEAN') val = val === 'true' || val === true
      else if (item.valueType === 'NUMBER') val = Number(val) || 0
      return { ...item, _value: val }
    })
  } catch { /* ignore */ } finally { loading.value = false }
}

async function saveGroup() {
  try {
    const items = configs.value.map(item => {
      let val = item._value
      if (item.valueType === 'BOOLEAN') val = val ? 'true' : 'false'
      else if (item.valueType === 'NUMBER') val = String(val)
      return { key: item.key, value: val, group: activeGroup.value }
    })
    await updateSystemConfig({ group: activeGroup.value, items })
    ElMessage.success('保存成功')
  } catch { ElMessage.error('保存失败') }
}

onMounted(() => { fetchGroup() })
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.config-desc { font-size: 12px; color: #999; margin-top: 4px; }
</style>