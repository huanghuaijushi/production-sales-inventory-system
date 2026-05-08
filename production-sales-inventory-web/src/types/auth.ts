export type SysUserStatus = 'ACTIVE' | 'DISABLED'

export interface SysUserProfile {
  id: number
  username: string
  nickname: string
  roles: string[]
  permissions: string[]
  status: SysUserStatus
  lastLoginAt?: string
  createdAt: string
}

export interface RoleOption {
  id: number
  code: string
  name: string
  description?: string
}

export interface PermissionOption {
  id: number
  code: string
  name: string
  module: string
  description?: string
}

export interface PermissionGroupResponse {
  module: string
  permissions: PermissionOption[]
}

export interface RoleDetailResponse {
  id: number
  code: string
  name: string
  description?: string
  enabled: boolean
  sortOrder: number
  permissionCodes: string[]
}

export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest {
  username: string
  password: string
  nickname: string
}

export interface AuthTokenResponse {
  tokenType: 'Bearer'
  accessToken: string
  expiresIn: number
  sysUser: SysUserProfile
}

export interface AuthStatusResponse {
  authenticated: boolean
  sysUser?: SysUserProfile
}
