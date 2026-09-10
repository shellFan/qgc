<template>
  <div class="ads-page">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>广告管理</span>
          <el-button type="primary" size="small" @click="showAdd = true">新增广告</el-button>
        </div>
      </template>

      <el-table :data="ads" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="title" label="标题" width="150" />
        <el-table-column prop="positionCode" label="位置" width="140">
          <template #default="{ row }">
            <el-tag size="small">{{ positionMap[row.positionCode] || row.positionCode }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="图片" width="100">
          <template #default="{ row }">
            <el-image v-if="row.imageUrl" :src="row.imageUrl" style="width:60px;height:40px" fit="cover" />
          </template>
        </el-table-column>
        <el-table-column prop="linkUrl" label="链接" show-overflow-tooltip />
        <el-table-column prop="viewCount" label="展示" width="70" />
        <el-table-column prop="clickCount" label="点击" width="70" />
        <el-table-column prop="enabled" label="状态" width="80">
          <template #default="{ row }">
            <el-switch :model-value="row.enabled === 1" @change="toggleAd(row)" />
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="60" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button size="small" text @click="editAd(row)">编辑</el-button>
            <el-button size="small" text type="danger" @click="deleteAd(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="showAdd" :title="editingAd ? '编辑广告' : '新增广告'" width="500px">
      <el-form :model="adForm" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="adForm.title" placeholder="广告标题" />
        </el-form-item>
        <el-form-item label="位置">
          <el-select v-model="adForm.positionCode" placeholder="选择广告位">
            <el-option v-for="(label, code) in positionMap" :key="code" :label="label" :value="code" />
          </el-select>
        </el-form-item>
        <el-form-item label="图片URL">
          <el-input v-model="adForm.imageUrl" placeholder="图片链接" />
        </el-form-item>
        <el-form-item label="跳转链接">
          <el-input v-model="adForm.linkUrl" placeholder="点击跳转URL(可选)" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="adForm.sort" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="adForm.enabled" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAdd = false">取消</el-button>
        <el-button type="primary" @click="saveAd">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAdList, createAd, updateAd, deleteAd as deleteAdApi, toggleAd as toggleAdApi } from '@/api'

const positionMap = {
  HOME_TOP: '首页顶部',
  HOME_LIST: '首页列表',
  HOME_FEED: '首页Feed',
  CAMPAIGN_DETAIL_BOTTOM: '详情底部',
  DETAIL_BOTTOM: '详情底部',
  PAY_SUCCESS: '支付成功',
  PROOF_PAGE: '返图页',
  CREATE_SUCCESS: '创建成功'
}

const ads = ref([])
const showAdd = ref(false)
const editingAd = ref(null)
const adForm = ref({ title: '', positionCode: 'HOME_TOP', imageUrl: '', linkUrl: '', sort: 0, enabled: true })

async function fetchAds() {
  try {
    const res = await getAdList()
    ads.value = res?.data || []
  } catch { /* error handled */ }
}

function editAd(row) {
  editingAd.value = row
  adForm.value = {
    title: row.title,
    positionCode: row.positionCode,
    imageUrl: row.imageUrl,
    linkUrl: row.linkUrl || '',
    sort: row.sort || 0,
    enabled: row.enabled === 1
  }
  showAdd.value = true
}

async function saveAd() {
  try {
    const payload = {
      ...adForm.value,
      enabled: adForm.value.enabled ? 1 : 0
    }
    if (editingAd.value) {
      await updateAd(editingAd.value.id, payload)
    } else {
      await createAd(payload)
    }
    ElMessage.success('保存成功')
    showAdd.value = false
    editingAd.value = null
    fetchAds()
  } catch { ElMessage.error('保存失败') }
}

async function toggleAd(row) {
  try {
    await toggleAdApi(row.id)
    row.enabled = row.enabled === 1 ? 0 : 1
    ElMessage.success('状态已更新')
  } catch { ElMessage.error('操作失败') }
}

async function deleteAd(row) {
  try {
    await ElMessageBox.confirm('确定删除此广告?', '提示', { type: 'warning' })
    await deleteAdApi(row.id)
    ElMessage.success('已删除')
    fetchAds()
  } catch { /* cancel */ }
}

onMounted(() => { fetchAds() })
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>