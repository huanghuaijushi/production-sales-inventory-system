import { request } from './http'

export interface Product {
  id: number
  sku: string
  name: string
  type: 'FINISHED_PRODUCT' | 'RAW_MATERIAL'
  category?: string | undefined
  unit: string
  specification?: string | undefined
  costPrice?: number | undefined
  salePrice?: number | undefined
  alertQuantity: number
  description?: string | undefined
}

export interface ProductRequest {
  code: string
  name: string
  type: string
  category?: string | undefined
  specification?: string | undefined
  unit: string
  costPrice?: number | undefined
  salePrice?: number | undefined
  alertQuantity?: number | undefined
  description?: string | undefined
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

export const productApi = {
  getAllProducts: (page: number = 0, size: number = 20) =>
    request<PageResponse<Product>>(`/products?page=${page}&size=${size}`),

  createProduct: (product: ProductRequest) =>
    request<Product>('/products', {
      method: 'POST',
      body: product
    }),

  updateProduct: (productId: number, product: ProductRequest) =>
    request<Product>(`/products/${productId}`, {
      method: 'PUT',
      body: product
    }),

  deleteProduct: (productId: number) =>
    request<void>(`/products/${productId}`, {
      method: 'DELETE'
    }),

  searchProducts: (query: string, page: number = 0, size: number = 20) =>
    request<PageResponse<Product>>(`/products/search?query=${encodeURIComponent(query)}&page=${page}&size=${size}`)
}
