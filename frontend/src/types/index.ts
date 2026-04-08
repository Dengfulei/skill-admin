export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export interface PageResponse<T> {
  records: T[]
  total: number
  pageNum: number
  pageSize: number
}

export interface AuthenticatedUser {
  id: number
  username: string
  displayName: string
  systemAdmin: boolean
  departmentIds: number[]
  departmentAdminIds: number[]
}

export interface LoginResponse {
  token: string
  user: AuthenticatedUser
}

export type ResourceType = 'SKILL' | 'MCP'
export type ScopeLevel = 'PUBLIC' | 'DEPARTMENT' | 'PERSONAL'
export type ResourceStatus = 'DRAFT' | 'ACTIVE' | 'DISABLED'
export type AccessRequestStatus = 'PENDING' | 'APPROVED' | 'REJECTED'

export interface ResourceSummary {
  id: number
  name: string
  code: string
  resourceType: ResourceType
  scopeLevel: ScopeLevel
  status: ResourceStatus
  enabled: boolean
  approvalRequired: boolean
  ownerDepartmentId?: number
  ownerUserId?: number
  description?: string
}

export interface ResourceListStats {
  total: number
  skillCount: number
  mcpCount: number
  publicCount: number
  departmentCount: number
  personalCount: number
}

export interface ResourcePageResponse extends PageResponse<ResourceSummary> {
  stats: ResourceListStats
}

export interface PermissionAssignment {
  targetScope: ScopeLevel
  departmentId?: number
  userId?: number
}

export interface ResourceDetail {
  resource: ResourceSummary
  skillConfig?: {
    resourceId: number
    version?: string
    manifestJson: string
    entrypoint?: string
    icon?: string
  }
  mcpConfig?: {
    resourceId: number
    serverName: string
    transportType: string
    commandLine?: string
    argsJson?: string
    envJson?: string
    endpointUrl?: string
    headersJson?: string
  }
  permissions: Array<{
    id: number
    targetScope: ScopeLevel
    departmentId?: number
    userId?: number
    permissionType: 'USE' | 'MANAGE'
    enabled: boolean
  }>
}

export interface Department {
  id: number
  name: string
  code: string
}

export interface ResourceUpsertRequest {
  resourceType: ResourceType
  name: string
  code: string
  description?: string
  scopeLevel: ScopeLevel
  ownerDepartmentId?: number
  ownerUserId?: number
  status: ResourceStatus
  enabled: boolean
  approvalRequired: boolean
  version?: string
  manifestJson?: string
  entrypoint?: string
  icon?: string
  serverName?: string
  transportType?: string
  commandLine?: string
  argsJson?: string
  envJson?: string
  endpointUrl?: string
  headersJson?: string
  permissions?: PermissionAssignment[]
}

export interface AccessRequestItem {
  id: number
  resourceId: number
  resourceName: string
  applicantUserId: number
  departmentId: number
  reason?: string
  status: AccessRequestStatus
  reviewComment?: string
}
