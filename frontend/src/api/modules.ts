import http from './http'
import type {
  AccessRequestItem,
  AuthenticatedUser,
  Department,
  LoginResponse,
  PageResponse,
  ResourceDetail,
  ResourcePageResponse,
  ResourceSummary,
  ResourceUpsertRequest
} from '@/types'

export const loginApi = (payload: { username: string; password: string }) =>
  http.post<any, LoginResponse>('/api/auth/login', payload)

export const getCurrentUserApi = () => http.get<any, AuthenticatedUser>('/api/auth/me')

export const getDepartmentsApi = () => http.get<any, Department[]>('/api/meta/departments')

export const getAdminResourcesApi = (params: { pageNum: number; pageSize: number; keyword?: string; resourceType?: 'SKILL' | 'MCP' }) =>
  http.get<any, ResourcePageResponse>('/api/admin/resources', { params })

export const getAdminResourceDetailApi = (id: number) =>
  http.get<any, ResourceDetail>(`/api/admin/resources/${id}`)

export const createResourceApi = (payload: ResourceUpsertRequest) =>
  http.post<any, ResourceDetail>('/api/admin/resources', payload)

export const updateResourceApi = (id: number, payload: ResourceUpsertRequest) =>
  http.put<any, ResourceDetail>(`/api/admin/resources/${id}`, payload)

export const toggleResourceEnabledApi = (id: number, enabled: boolean) =>
  http.patch<any, void>(`/api/admin/resources/${id}/enabled`, { enabled })

export const deleteResourceApi = (id: number) =>
  http.delete<any, void>(`/api/admin/resources/${id}`)

export const getAvailableResourcesApi = (params: { pageNum: number; pageSize: number; keyword?: string; resourceType?: 'SKILL' | 'MCP' }) =>
  http.get<any, ResourcePageResponse>('/api/user/resources/available', { params })

export const getApplyCatalogApi = (params: { pageNum: number; pageSize: number }) =>
  http.get<any, PageResponse<ResourceSummary>>('/api/user/resources/apply-catalog', { params })

export const createApplicationApi = (payload: { resourceId: number; reason?: string }) =>
  http.post<any, AccessRequestItem>('/api/user/applications', payload)

export const getMyApplicationsApi = () =>
  http.get<any, AccessRequestItem[]>('/api/user/applications')

export const getReviewApplicationsApi = () =>
  http.get<any, AccessRequestItem[]>('/api/admin/applications')

export const reviewApplicationApi = (id: number, payload: { approved: boolean; reviewComment?: string }) =>
  http.patch<any, AccessRequestItem>(`/api/admin/applications/${id}/review`, payload)

export const runtimeInvokeApi = (resourceCode: string) =>
  http.post<any, { allowed: boolean; message: string }>('/api/runtime/invoke', { resourceCode })
