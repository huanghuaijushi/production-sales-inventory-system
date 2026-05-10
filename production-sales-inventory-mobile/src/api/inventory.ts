import { request } from '@/api/http'

export interface StockItem {
  id: number
  productId: number
  productCode: string
  productName: string
  productType: 'FINISHED_PRODUCT' | 'RAW_MATERIAL'
  category: string
  specification: string
  unit: string
  quantity: number
  lockedQuantity: number
  availableQuantity: number
  alertQuantity: number
  costPrice: number
  salePrice: number
  isLowStock: boolean
}

export interface StockBatch {
  id: number
  productId: number
  productCode: string
  productName: string
  productUnit: string
  batchNo: string
  productionDate: string | null
  expiryDate: string | null
  quantity: number
  availableQuantity: number
  unitCost?: number | null
  remark: string | null
  createdAt: string
  updatedAt: string
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

export interface StockSearchParams {
  query?: string
  category?: string
  status?: string
}

export type StockRecordSubType =
  | 'PRODUCTION'
  | 'PURCHASE'
  | 'SALES'
  | 'PRODUCTION_USAGE'
  | 'PRODUCTION_LOSS'
  | 'PACKAGING_LOSS'
  | 'SHIPPING_LOSS'
  | 'INVENTORY'

export interface StockOperationRequest {
  productId: number
  type: 'IN' | 'OUT' | 'ADJUST'
  subType: StockRecordSubType
  quantity: number
  relatedOrderId?: number
  businessUnitPrice?: number
  batchId?: number
  batchNo?: string
  productionDate?: string
  expiryDate?: string
  remark?: string
}

export const inventoryApi = {
  getAllStocks: (page = 0, size = 20, filters: StockSearchParams = {}) =>
    request<PageResponse<StockItem>>('/api/v1/inventory/stocks', {
      method: 'GET',
      data: {
        page,
        size,
        ...(filters.query?.trim() ? { query: filters.query.trim() } : {}),
        ...(filters.category && filters.category !== 'all' ? { category: filters.category } : {}),
        ...(filters.status && filters.status !== 'all' ? { status: filters.status } : {})
      }
    }),
  getBatchesByProductId: (productId: number) =>
    request<StockBatch[]>(`/api/v1/inventory/batches/product/${productId}`, {
      method: 'GET'
    }),
  createInbound: (data: StockOperationRequest) =>
    request<void>('/api/v1/inventory/inbound', {
      method: 'POST',
      data,
      showLoading: true
    }),
  createOutbound: (data: StockOperationRequest) =>
    request<void>('/api/v1/inventory/outbound', {
      method: 'POST',
      data,
      showLoading: true
    })
}
