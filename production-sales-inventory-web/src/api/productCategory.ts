import { request } from './http'

export interface ProductCategory {
  id: number
  name: string
  type: 'FINISHED_PRODUCT' | 'RAW_MATERIAL' | 'PACKAGING_MATERIAL' | 'SEMI_FINISHED_PRODUCT' | null
  typeText: string
  sortOrder: number
  enabled: boolean
  remark?: string | undefined
  createdAt: string
  updatedAt: string
}

export interface ProductCategoryRequest {
  name: string
  type?: 'FINISHED_PRODUCT' | 'RAW_MATERIAL' | undefined
  sortOrder?: number | undefined
  enabled?: boolean | undefined
  remark?: string | undefined
}

export const productCategoryApi = {
  getCategories: (type?: string, enabledOnly: boolean = true) => {
    const params = new URLSearchParams()
    if (type) params.set('type', type)
    params.set('enabledOnly', String(enabledOnly))
    return request<ProductCategory[]>(`/product-categories?${params.toString()}`)
  },

  createCategory: (payload: ProductCategoryRequest) =>
    request<ProductCategory>('/product-categories', {
      method: 'POST',
      body: payload
    }),

  updateCategory: (categoryId: number, payload: ProductCategoryRequest) =>
    request<ProductCategory>(`/product-categories/${categoryId}`, {
      method: 'PUT',
      body: payload
    })
}
