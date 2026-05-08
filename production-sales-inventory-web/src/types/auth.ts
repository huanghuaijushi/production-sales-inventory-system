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
