import { request } from './http'
import type { SysUserProfile } from '@/types/auth'

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
    return request<PageResponse<SysUserProfile>>(`/admin-users?${params.toString()}`)
  },

  createUser: (payload: SysUserCreateRequest) =>
    request<SysUserProfile>('/admin-users', {
      method: 'POST',
      body: payload
    }),

  activateUser: (userId: number) =>
    request<SysUserProfile>(`/admin-users/${userId}/activate`, {
      method: 'PATCH'
    }),

  disableUser: (userId: number) =>
    request<SysUserProfile>(`/admin-users/${userId}/disable`, {
      method: 'PATCH'
    }),

  resetPassword: (userId: number, password: string) =>
    request<SysUserProfile>(`/admin-users/${userId}/password`, {
      method: 'PATCH',
      body: { password }
    })
}
