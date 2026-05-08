import { request } from './http'
import type {
  PermissionGroupResponse,
  PermissionOption,
  RoleDetailResponse,
  RoleOption,
  SysUserProfile
} from '@/types/auth'

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

export interface SysUserCreateRequest {
  username: string
  nickname: string
  password: string
  roleCodes: string[]
}

export interface UserRoleUpdateRequest {
  roleCodes: string[]
}

export interface RoleUpsertRequest {
  code: string
  name: string
  description?: string
  enabled: boolean
  sortOrder?: number
  permissionCodes: string[]
}

export const sysUserApi = {
  getUsers: (page: number = 0, size: number = 20, query: string = '') => {
    const params = new URLSearchParams({
      page: String(page),
      size: String(size)
    })
    if (query.trim()) {
      params.set('query', query.trim())
    }
    return request<PageResponse<SysUserProfile>>(`/sys-users?${params.toString()}`)
  },

  createUser: (payload: SysUserCreateRequest) =>
    request<SysUserProfile>('/sys-users', {
      method: 'POST',
      body: payload
    }),

  getRoleOptions: () => request<RoleOption[]>('/sys-users/role-options'),
  getPermissionOptions: () => request<PermissionOption[]>('/sys-users/permission-options'),
  getPermissionGroups: () => request<PermissionGroupResponse[]>('/roles/permission-groups'),

  updateUserRoles: (userId: number, payload: UserRoleUpdateRequest) =>
    request<SysUserProfile>(`/sys-users/${userId}/roles`, {
      method: 'PATCH',
      body: payload
    }),

  activateUser: (userId: number) =>
    request<SysUserProfile>(`/sys-users/${userId}/activate`, {
      method: 'PATCH'
    }),

  disableUser: (userId: number) =>
    request<SysUserProfile>(`/sys-users/${userId}/disable`, {
      method: 'PATCH'
    }),

  resetPassword: (userId: number, password: string) =>
    request<SysUserProfile>(`/sys-users/${userId}/password`, {
      method: 'PATCH',
      body: { password }
    }),

  getRoles: () => request<RoleOption[]>('/roles'),
  getRole: (roleId: number) => request<RoleDetailResponse>(`/roles/${roleId}`),
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
