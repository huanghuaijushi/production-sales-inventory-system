export type AdminStatus = 'ACTIVE' | 'DISABLED'

export interface AdminProfile {
  id: number
  username: string
  nickname: string
  roles: string[]
  permissions: string[]
  status: AdminStatus
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
  admin: AdminProfile
}

export interface AuthStatusResponse {
  authenticated: boolean
  admin?: AdminProfile
}
