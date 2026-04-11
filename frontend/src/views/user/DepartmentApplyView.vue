<template>
  <div class="page-card page-panel">
    <div class="section-heading">
      <h3>部门技能申请</h3>
      <p>查看当前部门可申请的资源，并跟踪已提交申请的审批进度。</p>
    </div>
    <el-tabs data-testid="department-apply-tabs">
      <el-tab-pane label="可申请部门技能" name="catalog">
        <div class="data-table">
          <el-table :data="catalog" border>
            <el-table-column prop="name" label="资源名称" min-width="180" />
            <el-table-column label="编码" min-width="180">
              <template #default="{ row }">
                <span class="code-text">{{ row.code }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="resourceType" label="类型" width="90" />
            <el-table-column prop="description" label="说明" min-width="220" />
            <el-table-column label="操作" width="280">
              <template #default="{ row }">
                <el-space wrap>
                  <el-button
                    type="primary"
                    plain
                    :data-testid="`apply-resource-${row.id}`"
                    @click="apply(row.id)"
                  >
                    提交申请
                  </el-button>
                  <el-button plain :data-testid="`verify-resource-${row.id}`" @click="verifyDenied(row.code)">
                    验证无权限
                  </el-button>
                </el-space>
              </template>
            </el-table-column>
          </el-table>
        </div>
        <div class="table-pagination">
          <el-pagination
            :current-page="catalogPageNum"
            :page-size="catalogPageSize"
            :page-sizes="[10, 20, 50]"
            :total="catalogTotal"
            background
            layout="total, sizes, prev, pager, next"
            @current-change="handleCatalogPageChange"
            @size-change="handleCatalogSizeChange"
          />
        </div>
      </el-tab-pane>
      <el-tab-pane label="我的申请记录" name="records">
        <div class="data-table">
          <el-table :data="applications" border>
            <el-table-column prop="resourceName" label="资源名称" min-width="180" />
            <el-table-column prop="reason" label="申请理由" min-width="220" />
            <el-table-column label="状态" width="120">
              <template #default="{ row }">
                <el-tag :type="statusTag(row.status)">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="reviewComment" label="审批意见" min-width="220" />
          </el-table>
        </div>
      </el-tab-pane>
    </el-tabs>

    <el-dialog
      v-model="applyDialogVisible"
      title="申请部门技能"
      width="420px"
      data-testid="application-dialog"
      @closed="closeApplyDialog"
    >
      <el-form label-position="top">
        <el-form-item label="申请理由">
          <el-input
            v-model="applyReason"
            type="textarea"
            :rows="4"
            placeholder="如：本周需要使用销售分析 MCP 处理线索复盘"
            name="applicationReason"
            aria-label="申请理由"
            data-testid="application-reason"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button data-testid="application-cancel" @click="closeApplyDialog">取消</el-button>
        <el-button type="primary" data-testid="application-submit" @click="submitApplication">
          提交申请
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { createApplicationApi, getApplyCatalogApi, getMyApplicationsApi, runtimeInvokeApi } from '@/api/modules'
import type { AccessRequestItem, AccessRequestStatus, ResourceSummary } from '@/types'

const catalog = ref<ResourceSummary[]>([])
const catalogPageNum = ref(1)
const catalogPageSize = ref(10)
const catalogTotal = ref(0)
const applications = ref<AccessRequestItem[]>([])
const applyDialogVisible = ref(false)
const applyReason = ref('')
const pendingApplyResourceId = ref<number | null>(null)

function statusTag(status: AccessRequestStatus) {
  return status === 'APPROVED' ? 'success' : status === 'REJECTED' ? 'danger' : 'warning'
}

async function loadData() {
  const [catalogPage, applicationList] = await Promise.all([
    getApplyCatalogApi({ pageNum: catalogPageNum.value, pageSize: catalogPageSize.value }),
    getMyApplicationsApi()
  ])
  catalog.value = catalogPage.records
  catalogTotal.value = catalogPage.total
  applications.value = applicationList
  await ensureCatalogPageInRange()
}

async function ensureCatalogPageInRange() {
  if (catalogTotal.value === 0 || catalog.value.length > 0 || catalogPageNum.value === 1) {
    return
  }
  const lastPage = Math.max(1, Math.ceil(catalogTotal.value / catalogPageSize.value))
  if (catalogPageNum.value !== lastPage) {
    catalogPageNum.value = lastPage
    await loadData()
  }
}

function apply(resourceId: number) {
  pendingApplyResourceId.value = resourceId
  applyReason.value = ''
  applyDialogVisible.value = true
}

function closeApplyDialog() {
  applyDialogVisible.value = false
  applyReason.value = ''
  pendingApplyResourceId.value = null
}

async function submitApplication() {
  if (!pendingApplyResourceId.value) {
    return
  }
  await createApplicationApi({ resourceId: pendingApplyResourceId.value, reason: applyReason.value.trim() || undefined })
  ElMessage.success('申请已提交')
  closeApplyDialog()
  await loadData()
}

async function verifyDenied(code: string) {
  const result = await runtimeInvokeApi(code)
  if (result.allowed) {
    ElMessage.success(result.message)
    return
  }
  ElMessage.warning(result.message)
}

function handleCatalogPageChange(value: number) {
  catalogPageNum.value = value
  loadData()
}

function handleCatalogSizeChange(value: number) {
  catalogPageSize.value = value
  catalogPageNum.value = 1
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
