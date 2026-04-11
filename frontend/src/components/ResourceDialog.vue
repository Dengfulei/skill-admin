<template>
  <el-dialog
    class="resource-dialog"
    :model-value="visible"
    :title="dialogTitle"
    width="min(980px, calc(100vw - 24px))"
    data-testid="resource-dialog"
    @close="emit('close')"
  >
    <div class="dialog-stage">
      <header class="dialog-hero">
        <p class="dialog-kicker">{{ manageModeLabel }}</p>
        <div class="dialog-hero-main">
          <div>
            <h3>{{ heroTitle }}</h3>
            <p>{{ heroDescription }}</p>
          </div>
        </div>
        <div class="hero-meta">
          <span class="meta-pill">{{ resourceTypeLabel }}</span>
          <span class="meta-pill">{{ scopeLevelLabel }}</span>
          <span class="meta-pill">{{ availabilityLabel }}</span>
          <span class="meta-pill">{{ runtimeLabel }}</span>
          <span class="meta-pill meta-pill-accent">{{ permissionSummaryTitle }}</span>
        </div>
      </header>

      <el-form label-position="top" :model="model" class="resource-form">
        <section class="form-section">
          <div class="section-head">
            <div>
              <p>基础信息</p>
              <h4>先确定资源身份</h4>
            </div>
            <span>这些配置决定资源归属、展示和状态。</span>
          </div>

          <div class="form-grid form-grid-two">
            <el-form-item label="资源类型" required>
              <el-select
                v-model="model.resourceType"
                placeholder="请选择资源类型"
                data-testid="resource-type"
              >
                <el-option label="Skill" value="SKILL" />
                <el-option label="MCP" value="MCP" />
              </el-select>
              <div class="field-hint">决定后续运行配置字段。</div>
            </el-form-item>

            <el-form-item label="权限级别" required>
              <el-select
                v-model="model.scopeLevel"
                placeholder="请选择权限级别"
                data-testid="resource-scope-level"
              >
                <el-option
                  v-for="item in availableScopeOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
              <div class="field-hint">决定资源范围和默认开放方式。</div>
            </el-form-item>

            <el-form-item label="名称" required>
              <el-input
                v-model="model.name"
                placeholder="例如：部门知识检索 Skill"
                name="resourceName"
                aria-label="资源名称"
                data-testid="resource-name"
              />
            </el-form-item>

            <el-form-item label="编码" required>
              <el-input
                v-model="model.code"
                placeholder="例如：dept-knowledge-search"
                name="resourceCode"
                aria-label="资源编码"
                data-testid="resource-code"
              />
              <div class="field-hint">建议使用英文小写和中划线。</div>
            </el-form-item>
          </div>

          <div class="form-grid form-grid-two">
            <el-form-item v-if="model.scopeLevel === 'DEPARTMENT'" label="所属部门" required>
              <el-select
                v-model="model.ownerDepartmentId"
                clearable
                placeholder="请选择归属部门"
                data-testid="resource-owner-department"
              >
                <el-option
                  v-for="item in selectableDepartments"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                />
              </el-select>
              <div class="field-hint">部门级资源创建后不可改部门。</div>
            </el-form-item>

            <el-form-item v-if="model.scopeLevel === 'PERSONAL'" label="所属用户">
              <el-input
                :model-value="personalOwnerLabel"
                disabled
                name="resourceOwnerUser"
                aria-label="所属用户"
                data-testid="resource-owner-user"
              />
              <div class="field-hint">个人级资源默认仅本人可用。</div>
            </el-form-item>

            <el-form-item label="发布状态" required>
              <el-select
                v-model="model.status"
                placeholder="请选择状态"
                data-testid="resource-status"
              >
                <el-option label="草稿" value="DRAFT" />
                <el-option label="启用" value="ACTIVE" />
                <el-option label="禁用" value="DISABLED" />
              </el-select>
            </el-form-item>
          </div>

          <el-form-item label="描述">
            <el-input
              v-model="model.description"
              type="textarea"
              :rows="3"
              placeholder="一句话说明这个资源的用途。"
              name="resourceDescription"
              aria-label="资源描述"
              data-testid="resource-description"
            />
          </el-form-item>

          <div class="toggle-row">
            <div class="toggle-line">
              <div>
                <strong>启用资源</strong>
                <p>关闭后暂不参与正常使用。</p>
              </div>
              <el-switch
                v-model="model.enabled"
                aria-label="启用资源"
                data-testid="resource-enabled"
              />
            </div>

            <div class="toggle-line" :class="{ 'is-muted': model.scopeLevel !== 'DEPARTMENT' }">
              <div>
                <strong>需要申请</strong>
                <p>仅部门级支持，开启后按审批发放。</p>
              </div>
              <el-switch
                v-model="model.approvalRequired"
                :disabled="model.scopeLevel !== 'DEPARTMENT'"
                aria-label="需要申请"
                data-testid="resource-approval-required"
              />
            </div>
          </div>
        </section>

        <section class="form-section">
          <div class="section-head">
            <div>
              <p>运行配置</p>
              <h4>{{ runtimeSectionTitle }}</h4>
            </div>
            <span>{{ runtimeSectionDescription }}</span>
          </div>

          <transition name="fade-swap" mode="out-in">
            <div v-if="model.resourceType === 'SKILL'" key="skill" class="section-body">
              <div class="form-grid form-grid-three">
                <el-form-item label="版本">
                  <el-input
                    v-model="model.version"
                    placeholder="例如：1.2.0"
                    name="resourceVersion"
                    aria-label="资源版本"
                    data-testid="resource-version"
                  />
                </el-form-item>

                <el-form-item label="入口文件">
                  <el-input
                    v-model="model.entrypoint"
                    placeholder="例如：src/index.ts"
                    name="resourceEntrypoint"
                    aria-label="资源入口文件"
                    data-testid="resource-entrypoint"
                  />
                </el-form-item>

                <el-form-item label="图标">
                  <el-input
                    v-model="model.icon"
                    placeholder="例如：BookOpen"
                    name="resourceIcon"
                    aria-label="资源图标"
                    data-testid="resource-icon"
                  />
                </el-form-item>
              </div>

              <el-form-item label="Manifest JSON">
                <el-input
                  v-model="model.manifestJson"
                  type="textarea"
                  :rows="5"
                  placeholder='例如：{"name":"search-skill","tools":[]}'
                  name="resourceManifestJson"
                  aria-label="Manifest JSON"
                  data-testid="resource-manifest-json"
                />
                <div class="field-hint">建议填写合法 JSON 对象。</div>
              </el-form-item>
            </div>

            <div v-else key="mcp" class="section-body">
              <div class="form-grid form-grid-two">
                <el-form-item label="服务名称">
                  <el-input
                    v-model="model.serverName"
                    placeholder="例如：doc-search-service"
                    name="resourceServerName"
                    aria-label="服务名称"
                    data-testid="resource-server-name"
                  />
                </el-form-item>

                <el-form-item label="传输协议">
                  <el-select
                    v-model="model.transportType"
                    placeholder="请选择传输协议"
                    data-testid="resource-transport-type"
                  >
                    <el-option label="STDIO" value="STDIO" />
                    <el-option label="HTTP" value="HTTP" />
                  </el-select>
                </el-form-item>
              </div>

              <div class="mode-note">
                <strong>{{ usesHttpTransport ? 'HTTP 模式' : 'STDIO 模式' }}</strong>
                <span>{{ transportSummary }}</span>
              </div>

              <transition name="fade-swap" mode="out-in">
                <div v-if="usesHttpTransport" key="http" class="form-grid form-grid-two">
                  <el-form-item label="Endpoint URL">
                    <el-input
                      v-model="model.endpointUrl"
                      placeholder="例如：https://mcp.example.com/api"
                      name="resourceEndpointUrl"
                      aria-label="Endpoint URL"
                      data-testid="resource-endpoint-url"
                    />
                  </el-form-item>

                  <el-form-item label="Headers JSON">
                    <el-input
                      v-model="model.headersJson"
                      type="textarea"
                      :rows="4"
                      placeholder='例如：{"Authorization":"Bearer xxx"}'
                      name="resourceHeadersJson"
                      aria-label="Headers JSON"
                      data-testid="resource-headers-json"
                    />
                    <div class="field-hint">建议使用 JSON 对象。</div>
                  </el-form-item>
                </div>

                <div v-else key="stdio" class="section-body">
                  <el-form-item label="命令行">
                    <el-input
                      v-model="model.commandLine"
                      placeholder="例如：npx -y @modelcontextprotocol/server-filesystem"
                      name="resourceCommandLine"
                      aria-label="命令行"
                      data-testid="resource-command-line"
                    />
                  </el-form-item>

                  <div class="form-grid form-grid-two">
                    <el-form-item label="Args JSON">
                      <el-input
                        v-model="model.argsJson"
                        type="textarea"
                        :rows="4"
                        placeholder='例如：["/workspace","--readonly"]'
                        name="resourceArgsJson"
                        aria-label="Args JSON"
                        data-testid="resource-args-json"
                      />
                      <div class="field-hint">建议使用 JSON 数组。</div>
                    </el-form-item>

                    <el-form-item label="Env JSON">
                      <el-input
                        v-model="model.envJson"
                        type="textarea"
                        :rows="4"
                        placeholder='例如：{"NODE_ENV":"production"}'
                        name="resourceEnvJson"
                        aria-label="Env JSON"
                        data-testid="resource-env-json"
                      />
                      <div class="field-hint">建议使用 JSON 对象。</div>
                    </el-form-item>
                  </div>
                </div>
              </transition>
            </div>
          </transition>
        </section>

        <section class="form-section">
          <div class="section-head">
            <div>
              <p>权限策略</p>
              <h4>确认默认开放方式</h4>
            </div>
            <span>保存后会按当前策略立即生效。</span>
          </div>

          <div class="policy-summary">
            <strong>{{ permissionSummaryTitle }}</strong>
            <p>{{ permissionSummaryText }}</p>
          </div>

          <template v-if="model.scopeLevel === 'PUBLIC'">
            <div class="empty-note">公共级资源会自动授权给全员，无需额外设置。</div>
          </template>

          <template v-else-if="model.scopeLevel === 'PERSONAL'">
            <div class="empty-note">个人级资源只绑定当前用户，不会对外开放。</div>
          </template>

          <template v-else-if="model.approvalRequired">
            <div class="empty-note">审批模式下不预置默认权限，成员需申请后使用。</div>
          </template>

          <div v-else class="form-grid form-grid-two">
            <el-form-item label="目标范围">
              <el-select
                v-model="model.permissionTargetScope"
                clearable
                placeholder="留空使用默认策略"
                data-testid="resource-permission-target-scope"
              >
                <el-option label="所属部门" value="DEPARTMENT" />
                <el-option label="指定成员" value="PERSONAL" />
              </el-select>
              <div class="field-hint">留空时按系统默认策略处理。</div>
            </el-form-item>

            <el-form-item v-if="model.permissionTargetScope === 'DEPARTMENT'" label="授权部门">
              <el-input
                :model-value="selectedDepartmentName"
                disabled
                name="resourcePermissionDepartment"
                aria-label="授权部门"
                data-testid="resource-permission-department"
              />
            </el-form-item>

            <el-form-item v-else-if="model.permissionTargetScope === 'PERSONAL'" label="授权用户" required>
              <el-input-number
                v-model="model.permissionUserId"
                :min="1"
                aria-label="授权用户"
                data-testid="resource-permission-user-id"
              />
              <div class="field-hint">仅支持所属部门成员，后端会再次校验。</div>
            </el-form-item>
          </div>
        </section>
      </el-form>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <span class="footer-text">{{ permissionSummaryTitle }}</span>
        <div class="footer-actions">
          <el-button data-testid="resource-cancel" @click="emit('close')">取消</el-button>
          <el-button type="primary" data-testid="resource-save" @click="handleSubmit">保存资源</el-button>
        </div>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, reactive, watch } from 'vue'
import type {
  AuthenticatedUser,
  Department,
  ResourceDetail,
  ResourceType,
  ResourceUpsertRequest,
  ScopeLevel
} from '@/types'

interface ResourceDialogModel extends ResourceUpsertRequest {
  id?: number
  permissionTargetScope?: ScopeLevel
  permissionDepartmentId?: number
  permissionUserId?: number
}

const props = defineProps<{
  visible: boolean
  currentUser: AuthenticatedUser | null
  departments: Department[]
  detail?: ResourceDetail | null
  manageMode: 'shared' | 'personal'
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

const isSystemAdmin = computed(() => Boolean(props.currentUser?.systemAdmin))
const isDepartmentAdmin = computed(() => Boolean(props.currentUser?.departmentAdminIds?.length))
const usesHttpTransport = computed(() => model.transportType === 'HTTP')
const resourceTypeLabel = computed(() => getResourceTypeLabel(model.resourceType))
const scopeLevelLabel = computed(() => getScopeLevelLabel(model.scopeLevel))
const availabilityLabel = computed(() => (model.enabled ? '已启用' : '已停用'))
const manageModeLabel = computed(() => (props.manageMode === 'personal' ? '个人资源管理' : '共享资源管理'))
const dialogTitle = computed(() => (model.id ? '编辑资源' : '新建资源'))
const heroTitle = computed(() => (model.id ? model.name || `编辑 ${resourceTypeLabel.value} 资源` : `创建 ${resourceTypeLabel.value} 资源`))
const heroDescription = computed(() => {
  if (props.manageMode === 'personal') {
    return '补充最少但必要的信息，系统会自动处理个人归属关系。'
  }
  return '按顺序填写基础信息、运行方式和权限策略，保存后即可统一维护。'
})
const runtimeLabel = computed(() => {
  if (model.resourceType === 'SKILL') {
    return 'Skill 配置'
  }
  return usesHttpTransport.value ? 'HTTP 接入' : 'STDIO 接入'
})
const runtimeSectionTitle = computed(() => (model.resourceType === 'SKILL' ? '补充 Skill 运行信息' : '选择 MCP 接入方式'))
const runtimeSectionDescription = computed(() => (
  model.resourceType === 'SKILL' ? '只保留常用运行字段。' : '根据协议展示对应配置项。'
))
const transportSummary = computed(() => (
  usesHttpTransport.value
    ? '适合远端服务或统一网关接入。'
    : '适合本地命令启动和代理进程。'
))
const availableScopeOptions = computed(() => {
  if (props.manageMode === 'personal') {
    return [{ label: '个人级', value: 'PERSONAL' as ScopeLevel }]
  }
  const options: Array<{ label: string; value: ScopeLevel }> = []
  if (isSystemAdmin.value) {
    options.push({ label: '公共级', value: 'PUBLIC' })
  }
  if (isDepartmentAdmin.value) {
    options.push({ label: '部门级', value: 'DEPARTMENT' })
  }
  return options
})
const selectableDepartments = computed(() => {
  if (isSystemAdmin.value) {
    return props.departments
  }
  const departmentAdminIds = props.currentUser?.departmentAdminIds ?? []
  return props.departments.filter((item) => departmentAdminIds.includes(item.id))
})
const selectedDepartmentName = computed(() => {
  return props.departments.find((item) => item.id === model.ownerDepartmentId)?.name ?? '未选择'
})
const personalOwnerLabel = computed(() => props.currentUser?.displayName || props.currentUser?.username || '')
const permissionSummaryTitle = computed(() => {
  if (model.scopeLevel === 'PUBLIC') {
    return '公共级默认全员可用'
  }
  if (model.scopeLevel === 'PERSONAL') {
    return '个人级仅本人可用'
  }
  if (model.approvalRequired) {
    return '部门级按审批发放'
  }
  if (model.permissionTargetScope === 'DEPARTMENT') {
    return '部门级预置到所属部门'
  }
  if (model.permissionTargetScope === 'PERSONAL') {
    return '部门级定向授权到个人'
  }
  return '部门级按默认策略开放'
})
const permissionSummaryText = computed(() => {
  if (model.scopeLevel === 'PUBLIC') {
    return '适合公共能力或统一服务，保存后自动向全员开放。'
  }
  if (model.scopeLevel === 'PERSONAL') {
    return '适合个人工具或实验资源，系统只绑定当前用户。'
  }
  if (model.approvalRequired) {
    return '适合需要精确控制访问的资源，成员需提交申请后使用。'
  }
  if (model.permissionTargetScope === 'DEPARTMENT') {
    return `保存后直接授权给${selectedDepartmentName.value}。`
  }
  if (model.permissionTargetScope === 'PERSONAL') {
    return '保存后仅对指定用户预置可用权限。'
  }
  return '未单独指定时，系统会沿用部门级默认开放策略。'
})

function getDefaultScopeLevel(): ScopeLevel {
  return availableScopeOptions.value[0]?.value ?? 'PERSONAL'
}

function normalizeModel() {
  if (!availableScopeOptions.value.some((item) => item.value === model.scopeLevel)) {
    model.scopeLevel = getDefaultScopeLevel()
  }

  if (model.scopeLevel !== 'DEPARTMENT') {
    model.ownerDepartmentId = undefined
    model.approvalRequired = false
    model.permissionTargetScope = undefined
    model.permissionDepartmentId = undefined
    model.permissionUserId = undefined
  } else {
    if (model.approvalRequired) {
      model.permissionTargetScope = undefined
      model.permissionDepartmentId = undefined
      model.permissionUserId = undefined
    }
    if (!isSystemAdmin.value) {
      const fallbackDepartmentId = selectableDepartments.value[0]?.id
      if (!model.ownerDepartmentId || !selectableDepartments.value.some((item) => item.id === model.ownerDepartmentId)) {
        model.ownerDepartmentId = fallbackDepartmentId
      }
    }
    if (model.approvalRequired) {
      model.permissionDepartmentId = undefined
      model.permissionUserId = undefined
    } else if (model.permissionTargetScope === 'DEPARTMENT') {
      model.permissionDepartmentId = model.ownerDepartmentId
      model.permissionUserId = undefined
    } else if (model.permissionTargetScope === 'PERSONAL') {
      model.permissionDepartmentId = undefined
    } else {
      model.permissionDepartmentId = undefined
      model.permissionUserId = undefined
    }
  }

  if (model.scopeLevel !== 'PERSONAL') {
    model.ownerUserId = undefined
    return
  }
  model.ownerUserId = props.currentUser?.id
}

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
        scopeLevel: getDefaultScopeLevel(),
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
      normalizeModel()
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
    normalizeModel()
  },
  { immediate: true }
)

watch(
  () => [props.currentUser, props.departments, props.manageMode],
  () => {
    normalizeModel()
  }
)

watch(
  () => [model.scopeLevel, model.ownerDepartmentId, model.permissionTargetScope, model.approvalRequired],
  () => {
    normalizeModel()
  }
)

function handleSubmit() {
  const permissions = model.scopeLevel === 'DEPARTMENT' && model.permissionTargetScope && !model.approvalRequired
    ? [
        {
          targetScope: model.permissionTargetScope,
          departmentId: model.permissionTargetScope === 'DEPARTMENT' ? model.ownerDepartmentId : undefined,
          userId: model.permissionUserId
        }
      ]
    : []

  emit(
    'submit',
    {
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
    },
    model.id
  )
}

function getResourceTypeLabel(resourceType?: ResourceType) {
  return resourceType === 'MCP' ? 'MCP' : 'Skill'
}

function getScopeLevelLabel(scopeLevel?: ScopeLevel) {
  switch (scopeLevel) {
    case 'PUBLIC':
      return '公共级'
    case 'DEPARTMENT':
      return '部门级'
    default:
      return '个人级'
  }
}

</script>

<style scoped>
.dialog-stage {
  display: grid;
  gap: 20px;
}

.dialog-hero {
  display: grid;
  gap: 10px;
  padding-bottom: 20px;
  border-bottom: 1px solid rgba(15, 23, 42, 0.08);
}

.dialog-kicker {
  margin: 0;
  color: #60708a;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.dialog-hero-main h3 {
  margin: 0;
  color: #0f172a;
  font-size: 28px;
  line-height: 1.12;
  letter-spacing: -0.04em;
}

.dialog-hero-main p {
  max-width: 680px;
  margin: 8px 0 0;
  color: #66758b;
  font-size: 14px;
  line-height: 1.7;
}

.hero-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.meta-pill {
  display: inline-flex;
  align-items: center;
  min-height: 30px;
  padding: 0 12px;
  border-radius: 999px;
  background: #f3f6fb;
  color: #42526a;
  font-size: 12px;
  font-weight: 700;
}

.meta-pill-accent {
  background: rgba(21, 94, 239, 0.08);
  color: #155eef;
}

.resource-form {
  display: grid;
  gap: 16px;
}

.form-section {
  display: grid;
  gap: 18px;
  padding: 22px 0 4px;
}

.form-section + .form-section {
  border-top: 1px solid rgba(15, 23, 42, 0.08);
}

.section-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.section-head p {
  margin: 0 0 4px;
  color: #60708a;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.section-head h4 {
  margin: 0;
  color: #0f172a;
  font-size: 20px;
  line-height: 1.2;
  letter-spacing: -0.03em;
}

.section-head span {
  color: #74839a;
  font-size: 13px;
  line-height: 1.6;
}

.section-body {
  display: grid;
  gap: 16px;
}

.form-grid {
  display: grid;
  gap: 16px;
}

.form-grid-two {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.form-grid-three {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.field-hint {
  margin-top: 8px;
  color: #7a869a;
  font-size: 12px;
  line-height: 1.6;
}

.toggle-row {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.toggle-line {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  min-height: 72px;
  padding: 0 16px;
  border-radius: 16px;
  background: #f7f9fc;
}

.toggle-line strong {
  display: block;
  color: #152033;
  font-size: 15px;
}

.toggle-line p {
  margin: 4px 0 0;
  color: #6f7e95;
  font-size: 13px;
  line-height: 1.6;
}

.toggle-line.is-muted {
  opacity: 0.7;
}

.mode-note,
.policy-summary {
  display: grid;
  gap: 6px;
  padding: 14px 16px;
  border-radius: 16px;
  background: #f7f9fc;
}

.mode-note strong,
.policy-summary strong {
  color: #102042;
  font-size: 14px;
}

.mode-note span,
.policy-summary p {
  margin: 0;
  color: #67768d;
  font-size: 13px;
  line-height: 1.65;
}

.empty-note {
  padding: 14px 16px;
  border-radius: 16px;
  background: #f7f9fc;
  color: #5f6f88;
  font-size: 13px;
  line-height: 1.7;
}

.dialog-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding-top: 10px;
  border-top: 1px solid rgba(15, 23, 42, 0.08);
}

.footer-text {
  color: #66758b;
  font-size: 13px;
  line-height: 1.6;
}

.footer-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.fade-swap-enter-active,
.fade-swap-leave-active {
  transition: opacity 0.18s ease, transform 0.18s ease;
}

.fade-swap-enter-from,
.fade-swap-leave-to {
  opacity: 0;
  transform: translateY(6px);
}

:deep(.el-dialog) {
  border-radius: 26px;
}

:deep(.el-dialog__header) {
  padding: 24px 28px 8px;
}

:deep(.el-dialog__title) {
  color: #111827;
  font-size: 28px;
  font-weight: 700;
  letter-spacing: -0.03em;
}

:deep(.el-dialog__body) {
  max-height: calc(100vh - 180px);
  padding: 12px 28px 20px;
  overflow: auto;
}

:deep(.el-dialog__footer) {
  padding: 0 28px 24px;
}

:deep(.el-form-item) {
  margin-bottom: 0;
}

:deep(.el-form-item__label) {
  padding-bottom: 8px;
  color: #3f4b61;
  font-weight: 700;
}

:deep(.el-input__wrapper),
:deep(.el-textarea__inner),
:deep(.el-select__wrapper) {
  min-height: 46px;
  border-radius: 14px;
  box-shadow: 0 0 0 1px rgba(15, 23, 42, 0.08) inset;
}

:deep(.el-textarea__inner) {
  min-height: 112px;
}

:deep(.el-input-number) {
  width: 100%;
}

:deep(.el-switch) {
  --el-switch-on-color: #155eef;
}

@media (max-width: 720px) {
  .dialog-hero-main h3 {
    font-size: 24px;
  }

  .form-grid-two,
  .form-grid-three,
  .toggle-row {
    grid-template-columns: 1fr;
  }

  .dialog-footer {
    flex-direction: column;
    align-items: flex-start;
  }

  .footer-actions {
    width: 100%;
    justify-content: flex-end;
  }
}

@media (max-width: 560px) {
  :deep(.el-dialog__header) {
    padding: 22px 18px 8px;
  }

  :deep(.el-dialog__body) {
    padding: 10px 18px 18px;
  }

  :deep(.el-dialog__footer) {
    padding: 0 18px 20px;
  }

  :deep(.el-dialog__title) {
    font-size: 24px;
  }
}
</style>
