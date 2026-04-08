<template>
  <div class="surface-stack">
    <div class="metric-grid">
      <div class="metric-item">
        <h4>我的可用资源</h4>
        <p>{{ resources.length }}</p>
      </div>
      <div class="metric-item">
        <h4>公共资源</h4>
        <p>{{ publicCount }}</p>
      </div>
      <div class="metric-item">
        <h4>部门资源</h4>
        <p>{{ departmentCount }}</p>
      </div>
      <div class="metric-item">
        <h4>个人资源</h4>
        <p>{{ personalCount }}</p>
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
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getAvailableResourcesApi, runtimeInvokeApi } from '@/api/modules'
import type { ResourceSummary } from '@/types'

const resources = ref<ResourceSummary[]>([])

const publicCount = computed(() => resources.value.filter((item) => item.scopeLevel === 'PUBLIC').length)
const departmentCount = computed(() => resources.value.filter((item) => item.scopeLevel === 'DEPARTMENT').length)
const personalCount = computed(() => resources.value.filter((item) => item.scopeLevel === 'PERSONAL').length)

async function loadData() {
  resources.value = await getAvailableResourcesApi()
}

async function invoke(code: string) {
  const result = await runtimeInvokeApi(code)
  if (result.allowed) {
    ElMessage.success(result.message)
    return
  }
  ElMessage.warning(result.message)
}

onMounted(loadData)
</script>
