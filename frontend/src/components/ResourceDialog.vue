<template>
  <el-dialog
    :model-value="visible"
    :title="model.id ? '编辑资源' : '新建资源'"
    width="min(920px, calc(100vw - 32px))"
    @close="emit('close')"
  >
    <div class="dialog-intro">
      <h4>资源基础信息</h4>
      <p>统一维护资源类型、归属范围、运行配置与默认授权，确保后续展示和鉴权结果一致。</p>
    </div>
    <el-form label-width="110px" :model="model">
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="资源类型" required>
            <el-select v-model="model.resourceType">
              <el-option label="Skill" value="SKILL" />
              <el-option label="MCP" value="MCP" />
            </el-select>
            <div class="field-tip">
              <span class="field-tag field-tag-required">必填</span>
              选择资源类别，决定后续配置项。
            </div>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="权限级别" required>
            <el-select v-model="model.scopeLevel">
              <el-option label="公共级" value="PUBLIC" />
              <el-option label="部门级" value="DEPARTMENT" />
              <el-option label="个人级" value="PERSONAL" />
            </el-select>
            <div class="field-tip">
              <span class="field-tag field-tag-required">必填</span>
              定义资源归属范围和维护边界。
            </div>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="名称" required>
            <el-input v-model="model.name" />
            <div class="field-tip">
              <span class="field-tag field-tag-required">必填</span>
              资源展示名称，建议清晰易懂。
            </div>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="编码" required>
            <el-input v-model="model.code" />
            <div class="field-tip">
              <span class="field-tag field-tag-required">必填</span>
              资源唯一标识，建议英文小写加中划线。
            </div>
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="描述">
        <el-input v-model="model.description" type="textarea" :rows="2" />
        <div class="field-tip">
          <span class="field-tag">选填</span>
          简要说明资源用途。
        </div>
      </el-form-item>
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="所属部门" v-if="model.scopeLevel === 'DEPARTMENT'" required>
            <el-select v-model="model.ownerDepartmentId" clearable>
              <el-option v-for="item in departments" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
            <div class="field-tip">
              <span class="field-tag field-tag-required">必填</span>
              部门级资源需指定归属部门。
            </div>
          </el-form-item>
          <el-form-item label="所属用户" v-if="model.scopeLevel === 'PERSONAL'">
            <el-input-number v-model="model.ownerUserId" :min="1" />
            <div class="field-tip">
              <span class="field-tag">选填</span>
              个人级资源可指定归属用户，不填默认当前用户。
            </div>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="发布状态" required>
            <el-select v-model="model.status">
              <el-option label="草稿" value="DRAFT" />
              <el-option label="启用" value="ACTIVE" />
              <el-option label="禁用" value="DISABLED" />
            </el-select>
            <div class="field-tip">
              <span class="field-tag field-tag-required">必填</span>
              控制资源处于草稿、启用或禁用状态。
            </div>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="16">
        <el-col :span="8">
          <el-form-item label="启用">
            <el-switch v-model="model.enabled" />
            <div class="field-tip">
              <span class="field-tag">选填</span>
              关闭后资源不会参与正常使用。
            </div>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="需要申请">
            <el-switch v-model="model.approvalRequired" :disabled="model.scopeLevel !== 'DEPARTMENT'" />
            <div class="field-tip">
              <span class="field-tag">选填</span>
              仅部门级生效，开启后需审批通过才能使用。
            </div>
          </el-form-item>
        </el-col>
      </el-row>

      <el-divider>运行配置</el-divider>
      <template v-if="model.resourceType === 'SKILL'">
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="版本">
              <el-input v-model="model.version" />
              <div class="field-tip">
                <span class="field-tag">选填</span>
                记录 Skill 当前版本号。
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="入口文件">
              <el-input v-model="model.entrypoint" />
              <div class="field-tip">
                <span class="field-tag">选填</span>
                Skill 主入口文件，如 `index.ts`。
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="图标">
              <el-input v-model="model.icon" />
              <div class="field-tip">
                <span class="field-tag">选填</span>
                资源展示图标标识，如 `Book`。
              </div>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="Manifest JSON">
          <el-input v-model="model.manifestJson" type="textarea" :rows="4" />
          <div class="field-tip">
            <span class="field-tag">选填</span>
            Skill 清单配置，建议填写合法 JSON。
          </div>
        </el-form-item>
      </template>
      <template v-else>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="服务名称">
              <el-input v-model="model.serverName" />
              <div class="field-tip">
                <span class="field-tag">选填</span>
                MCP 服务名称或标识。
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="传输协议">
              <el-select v-model="model.transportType">
                <el-option label="STDIO" value="STDIO" />
                <el-option label="HTTP" value="HTTP" />
              </el-select>
              <div class="field-tip">
                <span class="field-tag">选填</span>
                选择服务连接方式。
              </div>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="命令行">
          <el-input v-model="model.commandLine" />
          <div class="field-tip">
            <span class="field-tag">选填</span>
            本地启动 MCP 的执行命令。
          </div>
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="Args JSON">
              <el-input v-model="model.argsJson" type="textarea" :rows="3" />
              <div class="field-tip">
                <span class="field-tag">选填</span>
                命令行参数，建议填写 JSON 数组。
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Env JSON">
              <el-input v-model="model.envJson" type="textarea" :rows="3" />
              <div class="field-tip">
                <span class="field-tag">选填</span>
                环境变量，建议填写 JSON 对象。
              </div>
            </el-form-item>
          </el-col>
        </el-row>
      </template>

      <el-divider>权限分配</el-divider>
      <el-row :gutter="16">
        <el-col :span="8">
          <el-form-item label="目标范围">
            <el-select v-model="model.permissionTargetScope" clearable placeholder="留空使用默认策略">
              <el-option label="公共" value="PUBLIC" />
              <el-option label="部门" value="DEPARTMENT" />
              <el-option label="个人" value="PERSONAL" />
            </el-select>
            <div class="field-tip">
              <span class="field-tag">选填</span>
              指定默认授权对象，留空走系统默认策略。
            </div>
          </el-form-item>
        </el-col>
        <el-col :span="8" v-if="model.permissionTargetScope === 'DEPARTMENT'">
          <el-form-item label="授权部门" required>
            <el-select v-model="model.permissionDepartmentId" clearable>
              <el-option v-for="item in departments" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
            <div class="field-tip">
              <span class="field-tag field-tag-required">条件必填</span>
              目标范围为部门时填写。
            </div>
          </el-form-item>
        </el-col>
        <el-col :span="8" v-if="model.permissionTargetScope === 'PERSONAL'">
          <el-form-item label="授权用户" required>
            <el-input-number v-model="model.permissionUserId" :min="1" />
            <div class="field-tip">
              <span class="field-tag field-tag-required">条件必填</span>
              目标范围为个人时填写。
            </div>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <template #footer>
      <el-button @click="emit('close')">取消</el-button>
      <el-button type="primary" @click="handleSubmit">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { reactive, watch } from 'vue'
import type { Department, ResourceDetail, ResourceUpsertRequest, ScopeLevel } from '@/types'

interface ResourceDialogModel extends ResourceUpsertRequest {
  id?: number
  permissionTargetScope?: ScopeLevel
  permissionDepartmentId?: number
  permissionUserId?: number
}

const props = defineProps<{
  visible: boolean
  departments: Department[]
  detail?: ResourceDetail | null
}>()

const emit = defineEmits<{
  close: []
  submit: [payload: ResourceUpsertRequest, id?: number]
}>()

const model = reactive<ResourceDialogModel>({
  resourceType: 'SKILL',
  name: '',
  code: '',
  description: '',
  scopeLevel: 'PUBLIC',
  status: 'ACTIVE',
  enabled: true,
  approvalRequired: false,
  manifestJson: '{}',
  transportType: 'STDIO'
})

watch(
  () => props.detail,
  (detail) => {
    if (!detail) {
      Object.assign(model, {
        id: undefined,
        resourceType: 'SKILL',
        name: '',
        code: '',
        description: '',
        scopeLevel: 'PUBLIC',
        ownerDepartmentId: undefined,
        ownerUserId: undefined,
        status: 'ACTIVE',
        enabled: true,
        approvalRequired: false,
        version: '',
        manifestJson: '{}',
        entrypoint: '',
        icon: '',
        serverName: '',
        transportType: 'STDIO',
        commandLine: '',
        argsJson: '',
        envJson: '',
        endpointUrl: '',
        headersJson: '',
        permissionTargetScope: undefined,
        permissionDepartmentId: undefined,
        permissionUserId: undefined
      })
      return
    }
    const firstPermission = detail.permissions?.[0]
    Object.assign(model, {
      id: detail.resource.id,
      resourceType: detail.resource.resourceType,
      name: detail.resource.name,
      code: detail.resource.code,
      description: detail.resource.description,
      scopeLevel: detail.resource.scopeLevel,
      ownerDepartmentId: detail.resource.ownerDepartmentId,
      ownerUserId: detail.resource.ownerUserId,
      status: detail.resource.status,
      enabled: detail.resource.enabled,
      approvalRequired: detail.resource.approvalRequired,
      version: detail.skillConfig?.version,
      manifestJson: detail.skillConfig?.manifestJson || '{}',
      entrypoint: detail.skillConfig?.entrypoint,
      icon: detail.skillConfig?.icon,
      serverName: detail.mcpConfig?.serverName,
      transportType: detail.mcpConfig?.transportType || 'STDIO',
      commandLine: detail.mcpConfig?.commandLine,
      argsJson: detail.mcpConfig?.argsJson,
      envJson: detail.mcpConfig?.envJson,
      endpointUrl: detail.mcpConfig?.endpointUrl,
      headersJson: detail.mcpConfig?.headersJson,
      permissionTargetScope: firstPermission?.targetScope,
      permissionDepartmentId: firstPermission?.departmentId,
      permissionUserId: firstPermission?.userId
    })
  },
  { immediate: true }
)

function handleSubmit() {
  const permissions = model.permissionTargetScope
    ? [
        {
          targetScope: model.permissionTargetScope,
          departmentId: model.permissionDepartmentId,
          userId: model.permissionUserId
        }
      ]
    : []

  emit('submit', {
    resourceType: model.resourceType,
    name: model.name,
    code: model.code,
    description: model.description,
    scopeLevel: model.scopeLevel,
    ownerDepartmentId: model.ownerDepartmentId,
    ownerUserId: model.ownerUserId,
    status: model.status,
    enabled: model.enabled,
    approvalRequired: model.approvalRequired,
    version: model.version,
    manifestJson: model.manifestJson,
    entrypoint: model.entrypoint,
    icon: model.icon,
    serverName: model.serverName,
    transportType: model.transportType,
    commandLine: model.commandLine,
    argsJson: model.argsJson,
    envJson: model.envJson,
    endpointUrl: model.endpointUrl,
    headersJson: model.headersJson,
    permissions
  }, model.id)
}
</script>

<style scoped>
.dialog-intro {
  margin-bottom: 18px;
}

.dialog-intro h4 {
  margin: 0 0 6px;
  color: #0f172a;
  font-size: 18px;
  line-height: 1.2;
}

.dialog-intro p {
  margin: 0;
  color: #5e6b81;
  font-size: 14px;
  line-height: 1.7;
}

.field-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  margin-top: 6px;
  color: #7a869a;
  font-size: 12px;
  line-height: 1.5;
}

.field-tag {
  flex-shrink: 0;
  padding: 1px 8px;
  border-radius: 999px;
  background: #eef2ff;
  color: #4f46e5;
  font-size: 12px;
  line-height: 20px;
}

.field-tag-required {
  background: #fff1f2;
  color: #e11d48;
}

@media (max-width: 768px) {
  :deep(.el-form-item__content > .el-select),
  :deep(.el-form-item__content > .el-input-number) {
    width: 100%;
  }

  .field-tip {
    align-items: flex-start;
    flex-direction: column;
    gap: 4px;
  }
}
</style>
