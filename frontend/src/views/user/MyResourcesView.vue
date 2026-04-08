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
        <h3>当前用户可用技能列表</h3>
        <p>资源集合由公共授权、所属部门授权和个人授权三部分自动汇总得出。</p>
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
import type { ResourceListStats, ResourceSummary } from '@/types'

const resources = ref<ResourceSummary[]>([])
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
  const page = await getAvailableResourcesApi({ pageNum: pageNum.value, pageSize: pageSize.value })
  resources.value = page.records
  total.value = page.total
  Object.assign(stats, page.stats)
}

async function invoke(code: string) {
  const result = await runtimeInvokeApi(code)
  if (result.allowed) {
    ElMessage.success(result.message)
    return
  }
  ElMessage.warning(result.message)
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
.table-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

@media (max-width: 720px) {
  .table-pagination {
    justify-content: center;
  }
}
</style>
