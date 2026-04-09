<template>
  <div class="surface-stack">
    <div class="metric-grid">
      <div class="metric-item">
        <h4>我的可用资源</h4>
        <p>{{ stats.total }}</p>
      </div>
      <div class="metric-item">
        <h4>公共资源</h4>
        <p>{{ stats.publicCount }}</p>
      </div>
      <div class="metric-item">
        <h4>部门资源</h4>
        <p>{{ stats.departmentCount }}</p>
      </div>
      <div class="metric-item">
        <h4>个人资源</h4>
        <p>{{ stats.personalCount }}</p>
      </div>
    </div>
    <div class="page-card page-panel">
      <div class="section-heading">
        <h3>运行时鉴权测试</h3>
        <p>输入任意资源编码发起运行时校验。可用来验证“无权限返回友好提示”是否生效。</p>
      </div>
      <div class="runtime-tester">
        <el-input
          v-model="testCode"
          placeholder="例如：sales-analytics-mcp"
          clearable
          @keyup.enter="invokeManual"
        />
        <el-button type="primary" @click="invokeManual">验证权限</el-button>
      </div>
      <p class="tester-tip">
        建议用 <code>hr_user</code> 登录后输入 <code>sales-analytics-mcp</code>，应返回无权限友好提示。
      </p>
    </div>
    <div class="page-card page-panel">
      <div class="toolbar">
        <div class="section-heading">
          <h3>当前用户可用资源列表</h3>
          <p>资源集合由公共授权、所属部门授权和个人授权三部分自动汇总得出。</p>
        </div>
        <div class="toolbar-main">
          <el-radio-group v-model="resourceTypeFilter" @change="handleSearch">
            <el-radio-button value="ALL">所有</el-radio-button>
            <el-radio-button value="SKILL">Skill</el-radio-button>
            <el-radio-button value="MCP">MCP</el-radio-button>
          </el-radio-group>
          <el-input
            v-model="keyword"
            placeholder="按名称或编码搜索"
            class="search-input"
            clearable
            @clear="handleSearch"
            @keyup.enter="handleSearch"
          />
          <el-button @click="handleSearch">搜索</el-button>
        </div>
      </div>
      <div class="data-table">
        <el-table :data="resources" border>
          <el-table-column prop="name" label="资源名称" min-width="200" />
          <el-table-column label="编码" min-width="180">
            <template #default="{ row }">
              <span class="code-text">{{ row.code }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="resourceType" label="类型" width="90" />
          <el-table-column label="范围" width="100">
            <template #default="{ row }">
              <el-tag>{{ row.scopeLevel }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="description" label="说明" min-width="220" />
          <el-table-column label="模拟调用" width="150">
            <template #default="{ row }">
              <el-button type="primary" plain @click="invoke(row.code)">运行鉴权</el-button>
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
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getAvailableResourcesApi, runtimeInvokeApi } from '@/api/modules'
import type { ResourceListStats, ResourceSummary, ResourceType } from '@/types'

type ResourceTypeFilter = 'ALL' | ResourceType

const resources = ref<ResourceSummary[]>([])
const testCode = ref('')
const keyword = ref('')
const resourceTypeFilter = ref<ResourceTypeFilter>('ALL')
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

async function loadData() {
  const page = await getAvailableResourcesApi({
    pageNum: pageNum.value,
    pageSize: pageSize.value,
    keyword: keyword.value.trim() || undefined,
    resourceType: getSelectedResourceType()
  })
  resources.value = page.records
  total.value = page.total
  Object.assign(stats, page.stats)
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

async function invoke(code: string) {
  const result = await runtimeInvokeApi(code)
  if (result.allowed) {
    ElMessage.success(result.message)
    return
  }
  ElMessage.warning(result.message)
}

function invokeManual() {
  const code = testCode.value.trim()
  if (!code) {
    ElMessage.warning('请输入要验证的资源编码')
    return
  }
  invoke(code)
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

onMounted(loadData)
</script>

<style scoped>
.runtime-tester {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.runtime-tester :deep(.el-input) {
  width: min(420px, 100%);
}

.tester-tip {
  margin: 12px 0 0;
  color: #5e6b81;
  font-size: 13px;
}

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
