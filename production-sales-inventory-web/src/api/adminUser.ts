import { request } from './http'
import type { AdminProfile } from '@/types/auth'

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

export interface AdminCreateRequest {
  username: string
  nickname: string
  password: string
}

export const adminUserApi = {
  getAdmins: (page: number = 0, size: number = 20, query: string = '') => {
    const params = new URLSearchParams({
      page: String(page),
      size: String(size)
    })
    if (query.trim()) {
      params.set('query', query.trim())
    }
    return request<PageResponse<AdminProfile>>(`/admin-users?${params.toString()}`)
  },

  createAdmin: (payload: AdminCreateRequest) =>
    request<AdminProfile>('/admin-users', {
      method: 'POST',
      body: payload
    }),

  activateAdmin: (adminId: number) =>
    request<AdminProfile>(`/admin-users/${adminId}/activate`, {
      method: 'PATCH'
    }),

  disableAdmin: (adminId: number) =>
    request<AdminProfile>(`/admin-users/${adminId}/disable`, {
      method: 'PATCH'
    }),

  resetPassword: (adminId: number, password: string) =>
    request<AdminProfile>(`/admin-users/${adminId}/password`, {
      method: 'PATCH',
      body: { password }
    })
}
