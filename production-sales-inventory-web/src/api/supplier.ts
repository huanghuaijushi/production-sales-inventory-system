import { request } from './http'

export interface Supplier {
  id: number
  name: string
  contactName?: string | undefined
  phone?: string | undefined
  address?: string | undefined
  remark?: string | undefined
  enabled: boolean
  createdAt: string
  updatedAt: string
}

export interface SupplierRequest {
  name: string
  contactName?: string | undefined
  phone?: string | undefined
  address?: string | undefined
  remark?: string | undefined
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

export const supplierApi = {
  getSuppliers: (page: number = 0, size: number = 20, query: string = '') => {
    const params = new URLSearchParams({
      page: String(page),
      size: String(size)
    })
    if (query.trim()) {
      params.set('query', query.trim())
    }
    return request<PageResponse<Supplier>>(`/suppliers?${params.toString()}`)
  },

  createSupplier: (supplier: SupplierRequest) =>
    request<Supplier>('/suppliers', {
      method: 'POST',
      body: supplier
    }),

  updateSupplier: (supplierId: number, supplier: SupplierRequest) =>
    request<Supplier>(`/suppliers/${supplierId}`, {
      method: 'PUT',
      body: supplier
    }),

  deleteSupplier: (supplierId: number) =>
    request<void>(`/suppliers/${supplierId}`, {
      method: 'DELETE'
    })
}
