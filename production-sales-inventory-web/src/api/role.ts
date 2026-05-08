import { request } from '@/api/http'
import type { PermissionGroupResponse, RoleDetailResponse, RoleOption } from '@/types/auth'

export interface RoleUpsertRequest {
  code: string
  name: string
  description?: string
  enabled: boolean
  sortOrder: number
  permissionCodes: string[]
}

export const roleApi = {
  getRoles: () => request<RoleOption[]>('/roles'),

  getRole: (roleId: number) => request<RoleDetailResponse>(`/roles/${roleId}`),

  getPermissionGroups: () => request<PermissionGroupResponse[]>('/roles/permission-groups'),

  createRole: (payload: RoleUpsertRequest) =>
    request<RoleDetailResponse>('/roles', {
      method: 'POST',
      body: payload
    }),

  updateRole: (roleId: number, payload: RoleUpsertRequest) =>
    request<RoleDetailResponse>(`/roles/${roleId}`, {
      method: 'PATCH',
      body: payload
    }),

  updateRolePermissions: (roleId: number, permissionCodes: string[]) =>
    request<RoleDetailResponse>(`/roles/${roleId}/permissions`, {
      method: 'PATCH',
      body: permissionCodes
    })
}
