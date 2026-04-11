<template>
  <div class="page-card page-panel">
    <div class="section-heading">
      <h3>待审批部门技能申请</h3>
      <p>部门管理员仅能查看并处理本人负责部门的申请记录。</p>
    </div>
    <div class="data-table">
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
              <el-button
                link
                type="success"
                :data-testid="`approve-application-${row.id}`"
                @click="review(row.id, true)"
              >
                通过
              </el-button>
              <el-button
                link
                type="danger"
                :data-testid="`reject-application-${row.id}`"
                @click="review(row.id, false)"
              >
                驳回
              </el-button>
            </el-space>
            <span class="muted-text" v-else>已处理</span>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog
      v-model="reviewDialogVisible"
      title="审批处理"
      width="420px"
      data-testid="review-dialog"
      @closed="closeReviewDialog"
    >
      <el-form label-position="top">
        <el-form-item :label="`请输入${reviewApproved ? '通过' : '驳回'}说明`">
          <el-input
            v-model="reviewComment"
            type="textarea"
            :rows="4"
            :placeholder="reviewApproved ? '如：业务场景合理，批准开通' : '如：当前阶段不开放此工具'"
            name="reviewComment"
            aria-label="审批说明"
            data-testid="review-comment"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button data-testid="review-cancel" @click="closeReviewDialog">取消</el-button>
        <el-button
          :type="reviewApproved ? 'success' : 'danger'"
          data-testid="review-submit"
          @click="submitReview"
        >
          {{ reviewApproved ? '通过' : '驳回' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getReviewApplicationsApi, reviewApplicationApi } from '@/api/modules'
import { useApplicationStore } from '@/stores/application'
import type { AccessRequestItem, AccessRequestStatus } from '@/types'

const applications = ref<AccessRequestItem[]>([])
const applicationStore = useApplicationStore()
const reviewDialogVisible = ref(false)
const reviewComment = ref('')
const pendingReviewId = ref<number | null>(null)
const reviewApproved = ref(true)

async function loadData() {
  applications.value = await getReviewApplicationsApi()
  applicationStore.syncPendingReviewCount(applications.value)
}

function statusTag(status: AccessRequestStatus) {
  return status === 'APPROVED' ? 'success' : status === 'REJECTED' ? 'danger' : 'warning'
}

function review(id: number, approved: boolean) {
  pendingReviewId.value = id
  reviewApproved.value = approved
  reviewComment.value = ''
  reviewDialogVisible.value = true
}

function closeReviewDialog() {
  reviewDialogVisible.value = false
  reviewComment.value = ''
  pendingReviewId.value = null
}

async function submitReview() {
  if (!pendingReviewId.value) {
    return
  }
  await reviewApplicationApi(pendingReviewId.value, {
    approved: reviewApproved.value,
    reviewComment: reviewComment.value.trim() || undefined
  })
  ElMessage.success('审批已提交')
  closeReviewDialog()
  await loadData()
}

onMounted(loadData)
</script>
