<template>
  <div class="surface-stack">
    <div class="metric-grid">
      <div class="metric-item">
        <h4>当前可管理资源</h4>
        <p>{{ resources.length }}</p>
      </div>
      <div class="metric-item">
        <h4>Skill 数量</h4>
        <p>{{ skillCount }}</p>
      </div>
      <div class="metric-item">
        <h4>MCP 数量</h4>
        <p>{{ mcpCount }}</p>
      </div>
    </div>

    <div class="page-card page-panel">
      <div class="toolbar">
        <div class="section-heading">
          <h3>Skill / MCP 资源列表</h3>
          <p>集中维护资源定义、归属范围、启用状态和默认授权策略。</p>
        </div>
        <div class="toolbar-main">
          <el-input v-model="keyword" placeholder="按名称或编码过滤" class="search-input" clearable />
          <el-button type="primary" @click="openCreate">新建资源</el-button>
        </div>
      </div>
      <div class="data-table">
        <el-table :data="filteredResources" border>
          <el-table-column prop="name" label="名称" min-width="180" />
          <el-table-column label="编码" min-width="180">
            <template #default="{ row }">
              <span class="code-text">{{ row.code }}</span>
            </template>
          </el-table-column>
        <el-table-column prop="resourceType" label="类型" width="90" />
        <el-table-column prop="scopeLevel" label="范围" width="110" />
        <el-table-column prop="status" label="状态" width="110" />
        <el-table-column label="启用" width="90">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'info'">{{ row.enabled ? '是' : '否' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="需要申请" width="110">
          <template #default="{ row }">
            <el-tag :type="row.approvalRequired ? 'warning' : 'info'">{{ row.approvalRequired ? '是' : '否' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-space wrap>
              <el-button link type="primary" @click="openEdit(row.id)">编辑</el-button>
              <el-button link type="warning" @click="toggleEnabled(row)">
                {{ row.enabled ? '禁用' : '启用' }}
              </el-button>
              <el-button link type="danger" @click="remove(row.id)">删除</el-button>
            </el-space>
          </template>
        </el-table-column>
        </el-table>
      </div>
    </div>

    <ResourceDialog
      :visible="dialogVisible"
      :departments="departments"
      :detail="currentDetail"
      @close="dialogVisible = false"
      @submit="handleSubmit"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import ResourceDialog from '@/components/ResourceDialog.vue'
import {
  createResourceApi,
  deleteResourceApi,
  getAdminResourceDetailApi,
  getAdminResourcesApi,
  getDepartmentsApi,
  toggleResourceEnabledApi,
  updateResourceApi
} from '@/api/modules'
import type { Department, ResourceDetail, ResourceSummary, ResourceUpsertRequest } from '@/types'

const resources = ref<ResourceSummary[]>([])
const departments = ref<Department[]>([])
const keyword = ref('')
const dialogVisible = ref(false)
const currentDetail = ref<ResourceDetail | null>(null)

const filteredResources = computed(() =>
  resources.value.filter((item) =>
    [item.name, item.code].some((value) => value?.toLowerCase().includes(keyword.value.toLowerCase()))
  )
)
const skillCount = computed(() => resources.value.filter((item) => item.resourceType === 'SKILL').length)
const mcpCount = computed(() => resources.value.filter((item) => item.resourceType === 'MCP').length)

async function loadData() {
  ;[resources.value, departments.value] = await Promise.all([getAdminResourcesApi(), getDepartmentsApi()])
}

function openCreate() {
  currentDetail.value = null
  dialogVisible.value = true
}

async function openEdit(id: number) {
  currentDetail.value = await getAdminResourceDetailApi(id)
  dialogVisible.value = true
}

async function handleSubmit(payload: ResourceUpsertRequest, id?: number) {
  if (id) {
    await updateResourceApi(id, payload)
    ElMessage.success('资源已更新')
  } else {
    await createResourceApi(payload)
    ElMessage.success('资源已创建')
  }
  dialogVisible.value = false
  await loadData()
}

async function toggleEnabled(row: ResourceSummary) {
  await toggleResourceEnabledApi(row.id, !row.enabled)
  ElMessage.success(row.enabled ? '资源已禁用' : '资源已启用')
  await loadData()
}

async function remove(id: number) {
  await ElMessageBox.confirm('删除后资源不会再出现在当前列表，确认继续吗？', '删除确认')
  await deleteResourceApi(id)
  ElMessage.success('资源已删除')
  await loadData()
}

onMounted(loadData)
</script>

<style scoped>
.search-input {
  width: 300px;
}

@media (max-width: 720px) {
  .search-input {
    width: 100%;
  }
}
</style>
