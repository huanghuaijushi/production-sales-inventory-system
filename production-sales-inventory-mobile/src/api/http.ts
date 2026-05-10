import { getStorage, TOKEN_STORAGE_KEY, TOKEN_TYPE_STORAGE_KEY } from '@/utils/storage'

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
  timestamp?: string
  traceId?: string
}

export interface RequestOptions {
  method?: UniApp.RequestOptions['method']
  data?: UniApp.RequestOptions['data']
  header?: Record<string, string>
  showLoading?: boolean
}

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
const API_PREFIX = '/api/v1'

function buildUrl(url: string) {
  if (/^https?:\/\//i.test(url)) {
    return url
  }

  const normalizedPath = url.startsWith('/') ? url : `/${url}`
  const apiPath = normalizedPath.startsWith('/api/') ? normalizedPath : `${API_PREFIX}${normalizedPath}`

  return `${API_BASE_URL}${apiPath}`
}

export function getAuthorizationHeader() {
  const token = getStorage<string>(TOKEN_STORAGE_KEY, '')
  const tokenType = getStorage<string>(TOKEN_TYPE_STORAGE_KEY, 'Bearer')

  if (!token) {
    return {}
  }

  return {
    Authorization: `${tokenType || 'Bearer'} ${token}`
  }
}

export function request<T>(url: string, options: RequestOptions = {}): Promise<T> {
  const { method = 'GET', data, header = {}, showLoading = false } = options

  if (showLoading) {
    uni.showLoading({ title: '请稍候...', mask: true })
  }

  return new Promise<T>((resolve, reject) => {
    uni.request({
      url: buildUrl(url),
      method,
      data,
      header: {
        'Content-Type': 'application/json',
        ...getAuthorizationHeader(),
        ...header
      },
      success: (response) => {
        const body = response.data as ApiResponse<T> | undefined

        if (response.statusCode === 401) {
          reject(new Error('登录已过期，请重新登录'))
          return
        }

        if (!body) {
          reject(new Error('服务器响应为空'))
          return
        }

        if (body.code !== 200) {
          reject(new Error(body.message || '请求失败'))
          return
        }

        resolve(body.data)
      },
      fail: (error) => {
        reject(new Error(error.errMsg || '网络请求失败'))
      },
      complete: () => {
        if (showLoading) {
          uni.hideLoading()
        }
      }
    })
  })
}
