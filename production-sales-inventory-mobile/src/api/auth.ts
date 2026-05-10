import { request } from '@/api/http'

export interface LoginRequest {
  username: string
  password: string
}

export interface SysUserProfile {
  id: number
  username: string
  nickname: string
  roles: string[]
  permissions: string[]
  status: string
  lastLoginAt: string | null
  createdAt: string
}

export interface AuthTokenResponse {
  tokenType: string
  accessToken: string
  expiresIn: number
  sysUser: SysUserProfile
}

export interface AuthStatusResponse {
  authenticated: boolean
  sysUser: SysUserProfile | null
}

export function login(data: LoginRequest) {
  return request<AuthTokenResponse>('/api/v1/auth/login', {
    method: 'POST',
    data,
    showLoading: true
  })
}

export function getAuthStatus() {
  return request<AuthStatusResponse>('/api/v1/auth/status')
}

export function logout() {
  return request<void>('/api/v1/auth/logout', {
    method: 'POST'
  })
}
