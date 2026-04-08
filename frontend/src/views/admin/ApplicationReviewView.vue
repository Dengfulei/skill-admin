<template>
  <div class="page-card" style="padding: 20px">
    <div class="toolbar">
      <div>
        <h3 style="margin: 0 0 4px">待审批部门技能申请</h3>
        <p style="margin: 0; color: #64748b">部门管理员仅能看到本部门申请，系统管理员可查看全部。</p>
      </div>
    </div>
    <el-table :data="applications" border>
      <el-table-column prop="resourceName" label="资源名称" min-width="180" />
      <el-table-column prop="applicantUserId" label="申请人 ID" width="110" />
      <el-table-column prop="departmentId" label="部门 ID" width="110" />
      <el-table-column prop="reason" label="申请理由" min-width="220" />
      <el-table-column label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="statusTag(row.status)">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="reviewComment" label="审批意见" min-width="180" />
      <el-table-column label="操作" width="170">
        <template #default="{ row }">
          <el-space v-if="row.status === 'PENDING'">
            <el-button link type="success" @click="review(row.id, true)">通过</el-button>
            <el-button link type="danger" @click="review(row.id, false)">驳回</el-button>
          </el-space>
          <span v-else>已处理</span>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getReviewApplicationsApi, reviewApplicationApi } from '@/api/modules'
import type { AccessRequestItem, AccessRequestStatus } from '@/types'

const applications = ref<AccessRequestItem[]>([])

async function loadData() {
  applications.value = await getReviewApplicationsApi()
}

function statusTag(status: AccessRequestStatus) {
  return status === 'APPROVED' ? 'success' : status === 'REJECTED' ? 'danger' : 'warning'
}

async function review(id: number, approved: boolean) {
  const { value } = await ElMessageBox.prompt(`请输入${approved ? '通过' : '驳回'}说明`, '审批处理', {
    inputPlaceholder: approved ? '如：业务场景合理，批准开通' : '如：当前阶段不开放此工具',
    confirmButtonText: approved ? '通过' : '驳回'
  })
  await reviewApplicationApi(id, { approved, reviewComment: value })
  ElMessage.success('审批已提交')
  await loadData()
}

onMounted(loadData)
</script>
