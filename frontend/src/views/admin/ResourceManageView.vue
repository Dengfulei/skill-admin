<template>
  <div class="surface-stack">
    <div class="metric-grid">
      <div class="metric-item">
        <h4>当前可管理资源</h4>
        <p>{{ stats.total }}</p>
      </div>
      <div class="metric-item">
        <h4>Skill 数量</h4>
        <p>{{ stats.skillCount }}</p>
      </div>
      <div class="metric-item">
        <h4>MCP 数量</h4>
        <p>{{ stats.mcpCount }}</p>
      </div>
    </div>

    <div class="page-card page-panel">
      <div class="toolbar">
        <div class="section-heading">
          <h3>{{ panelTitle }}</h3>
          <p>{{ panelDescription }}</p>
        </div>
        <div class="toolbar-main">
          <el-radio-group v-model="resourceTypeFilter" @change="handleSearch">
            <el-radio-button value="ALL">所有</el-radio-button>
            <el-radio-button value="SKILL">Skill</el-radio-button>
            <el-radio-button value="MCP">MCP</el-radio-button>
          </el-radio-group>
          <el-input
            v-model="keyword"
            placeholder="按名称或编码过滤"
            class="search-input"
            clearable
            @clear="handleSearch"
            @keyup.enter="handleSearch"
          />
          <el-button @click="handleSearch">搜索</el-button>
          <el-button type="primary" @click="openCreate">新建资源</el-button>
        </div>
      </div>
      <div class="data-table">
        <el-table :data="resources" border>
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
      <div class="table-pagination">
        <el-pagination
          :current-page="pageNum"
          :page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          background
          layout="total, sizes, prev, pager, next"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </div>

    <ResourceDialog
      :visible="dialogVisible"
      :current-user="authStore.user"
      :departments="departments"
      :detail="currentDetail"
      @close="dialogVisible = false"
      @submit="handleSubmit"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRoute } from 'vue-router'
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
import { useAuthStore } from '@/stores/auth'
import type { Department, ResourceDetail, ResourceListStats, ResourceSummary, ResourceType, ResourceUpsertRequest } from '@/types'

type ResourceTypeFilter = 'ALL' | ResourceType

const resources = ref<ResourceSummary[]>([])
const departments = ref<Department[]>([])
const route = useRoute()
const authStore = useAuthStore()
const keyword = ref('')
const resourceTypeFilter = ref<ResourceTypeFilter>('ALL')
const dialogVisible = ref(false)
const currentDetail = ref<ResourceDetail | null>(null)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const stats = reactive<ResourceListStats>({
  total: 0,
  skillCount: 0,
  mcpCount: 0,
  publicCount: 0,
  departmentCount: 0,
  personalCount: 0
})

const isPersonalManageMode = computed(() => route.path === '/user/manage-resources')
const panelTitle = computed(() => (isPersonalManageMode.value ? '我的个人资源' : 'Skill / MCP 资源列表'))
const panelDescription = computed(() =>
  isPersonalManageMode.value
    ? '创建和维护仅本人可用的个人 Skill / MCP 资源。'
    : '集中维护资源定义、归属范围、启用状态和默认授权策略。'
)

async function loadData() {
  const [resourcePage, departmentList] = await Promise.all([
    getAdminResourcesApi({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: keyword.value.trim() || undefined,
      resourceType: getSelectedResourceType()
    }),
    getDepartmentsApi()
  ])
  resources.value = resourcePage.records
  total.value = resourcePage.total
  Object.assign(stats, resourcePage.stats)
  departments.value = departmentList
  await ensurePageInRange()
}

async function ensurePageInRange() {
  if (total.value === 0 || resources.value.length > 0 || pageNum.value === 1) {
    return
  }
  const lastPage = Math.max(1, Math.ceil(total.value / pageSize.value))
  if (pageNum.value !== lastPage) {
    pageNum.value = lastPage
    await loadData()
  }
}

function getSelectedResourceType() {
  return resourceTypeFilter.value === 'ALL' ? undefined : resourceTypeFilter.value
}

function handleSearch() {
  pageNum.value = 1
  loadData()
}

function handlePageChange(value: number) {
  pageNum.value = value
  loadData()
}

function handleSizeChange(value: number) {
  pageSize.value = value
  pageNum.value = 1
  loadData()
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

.table-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

@media (max-width: 720px) {
  .search-input {
    width: 100%;
  }

  .table-pagination {
    justify-content: center;
  }
}
</style>
