import { request } from '@/api/http'
import type {
  AuthStatusResponse,
  AuthTokenResponse,
  LoginRequest,
  RegisterRequest,
  SysUserProfile
} from '@/types/auth'

export function loginApi(payload: LoginRequest) {
  return request<AuthTokenResponse>('/auth/login', {
    method: 'POST',
    auth: false,
    body: { ...payload }
  })
}

export function registerApi(payload: RegisterRequest) {
  return request<SysUserProfile>('/auth/register', {
    method: 'POST',
    auth: false,
    body: { ...payload }
  })
}

export function getCurrentSysUserApi() {
  return request<SysUserProfile>('/auth/me')
}

export function getAuthStatusApi() {
  return request<AuthStatusResponse>('/auth/status')
}

export function getCurrentSysUserApi() {
  return request<SysUserProfile>('/auth/me')
}

export function logoutApi() {
  return request<void>('/auth/logout', {
    method: 'POST'
  })
}
