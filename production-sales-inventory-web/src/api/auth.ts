import { request } from '@/api/http'
import type {
  AdminProfile,
  AuthStatusResponse,
  AuthTokenResponse,
  LoginRequest,
  RegisterRequest
} from '@/types/auth'

export function loginApi(payload: LoginRequest) {
  return request<AuthTokenResponse>('/auth/login', {
    method: 'POST',
    auth: false,
    body: { ...payload }
  })
}

export function registerApi(payload: RegisterRequest) {
  return request<AdminProfile>('/auth/register', {
    method: 'POST',
    auth: false,
    body: { ...payload }
  })
}

export function getCurrentAdminApi() {
  return request<AdminProfile>('/auth/me')
}

export function getAuthStatusApi() {
  return request<AuthStatusResponse>('/auth/status')
}

export function logoutApi() {
  return request<void>('/auth/logout', {
    method: 'POST'
  })
}
