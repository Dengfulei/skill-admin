<template>
  <el-dialog
    :model-value="visible"
    :title="model.id ? '编辑资源' : '新建资源'"
    width="860px"
    @close="emit('close')"
  >
    <el-form label-width="110px" :model="model">
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="资源类型">
            <el-select v-model="model.resourceType">
              <el-option label="Skill" value="SKILL" />
              <el-option label="MCP" value="MCP" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="权限级别">
            <el-select v-model="model.scopeLevel">
              <el-option label="公共级" value="PUBLIC" />
              <el-option label="部门级" value="DEPARTMENT" />
              <el-option label="个人级" value="PERSONAL" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="名称">
            <el-input v-model="model.name" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="编码">
            <el-input v-model="model.code" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="描述">
        <el-input v-model="model.description" type="textarea" :rows="2" />
      </el-form-item>
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="所属部门" v-if="model.scopeLevel === 'DEPARTMENT'">
            <el-select v-model="model.ownerDepartmentId" clearable>
              <el-option v-for="item in departments" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="所属用户" v-if="model.scopeLevel === 'PERSONAL'">
            <el-input-number v-model="model.ownerUserId" :min="1" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="发布状态">
            <el-select v-model="model.status">
              <el-option label="草稿" value="DRAFT" />
              <el-option label="启用" value="ACTIVE" />
              <el-option label="禁用" value="DISABLED" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="16">
        <el-col :span="8">
          <el-form-item label="启用">
            <el-switch v-model="model.enabled" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="需要申请">
            <el-switch v-model="model.approvalRequired" :disabled="model.scopeLevel !== 'DEPARTMENT'" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-divider>运行配置</el-divider>
      <template v-if="model.resourceType === 'SKILL'">
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="版本">
              <el-input v-model="model.version" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="入口文件">
              <el-input v-model="model.entrypoint" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="图标">
              <el-input v-model="model.icon" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="Manifest JSON">
          <el-input v-model="model.manifestJson" type="textarea" :rows="4" />
        </el-form-item>
      </template>
      <template v-else>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="服务名称">
              <el-input v-model="model.serverName" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="传输协议">
              <el-select v-model="model.transportType">
                <el-option label="STDIO" value="STDIO" />
                <el-option label="HTTP" value="HTTP" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="命令行">
          <el-input v-model="model.commandLine" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="Args JSON">
              <el-input v-model="model.argsJson" type="textarea" :rows="3" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Env JSON">
              <el-input v-model="model.envJson" type="textarea" :rows="3" />
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
          </el-form-item>
        </el-col>
        <el-col :span="8" v-if="model.permissionTargetScope === 'DEPARTMENT'">
          <el-form-item label="授权部门">
            <el-select v-model="model.permissionDepartmentId" clearable>
              <el-option v-for="item in departments" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="8" v-if="model.permissionTargetScope === 'PERSONAL'">
          <el-form-item label="授权用户">
            <el-input-number v-model="model.permissionUserId" :min="1" />
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
