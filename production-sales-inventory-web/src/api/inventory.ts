import { request } from './http'

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

export interface InventoryDashboard {
  totalProducts: number
  lowStockCount: number
  outOfStockCount: number
  availableStockQuantity: number
  todayBusinessOverview: TodayBusinessOverview
  inventoryDistribution: InventoryDistributionItem[]
  lowStockItems: StockItem[]
  recentUpdates: StockItem[]
}

export interface StockTrendItem {
  date: string
  label: string
  inboundQuantity: number
  outboundQuantity: number
  netChangeQuantity: number
}

export interface InventoryDistributionItem {
  category: string
  quantity: number
  productCount: number
}

export interface TodayBusinessOverview {
  inboundRecordCount: number
  outboundRecordCount: number
  lossRecordCount: number
  pendingSalesOrderCount: number
  inboundQuantity: number
  outboundQuantity: number
  lossQuantity: number
}

export interface StockOperationRequest {
  productId: number
  type: 'IN' | 'OUT' | 'ADJUST'
  subType: 'PRODUCTION' | 'PURCHASE' | 'SALES' | 'LOSS' | 'INVENTORY'
  quantity: number
  batchNo?: string
  productionDate?: string
  remark?: string
}

export interface StockUpdateRequest {
  quantity: number
  alertQuantity: number
  remark?: string | undefined
}

export interface StockRecord {
  id: number
  recordNo: string
  productId: number
  productCode: string
  productName: string
  productUnit: string
  type: 'IN' | 'OUT' | 'ADJUST'
  subType: 'PRODUCTION' | 'PURCHASE' | 'SALES' | 'LOSS' | 'INVENTORY'
  quantity: number
  beforeQuantity: number
  afterQuantity: number
  batchNo: string
  productionDate: string
  expiryDate: string
  operatorName: string
  remark: string
  createdAt: string
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

export const inventoryApi = {
  getDashboard: () => request<InventoryDashboard>('/inventory/dashboard'),

  getStockTrend: (days: number = 7) =>
    request<StockTrendItem[]>(`/inventory/trends?days=${days}`),

  getAllStocks: (page: number = 0, size: number = 20, filters: StockSearchParams = {}) => {
    const params = new URLSearchParams({
      page: String(page),
      size: String(size)
    })

    if (filters.query?.trim()) {
      params.set('query', filters.query.trim())
    }
    if (filters.category && filters.category !== 'all') {
      params.set('category', filters.category)
    }
    if (filters.status && filters.status !== 'all') {
      params.set('status', filters.status)
    }

    return request<PageResponse<StockItem>>(`/inventory/stocks?${params.toString()}`)
  },

  getStockByProductId: (productId: number) =>
    request<StockItem>(`/inventory/stocks/product/${productId}`),

  updateStock: (stockId: number, req: StockUpdateRequest) =>
    request<StockItem>(`/inventory/stocks/${stockId}`, { method: 'PUT', body: req }),

  inbound: (req: StockOperationRequest) =>
    request<StockRecord>('/inventory/inbound', { method: 'POST', body: req }),

  outbound: (req: StockOperationRequest) =>
    request<StockRecord>('/inventory/outbound', { method: 'POST', body: req }),

  performStockOperation: (req: StockOperationRequest) =>
    request<StockRecord>('/inventory/operation', { method: 'POST', body: req }),

  getStockRecords: (page: number = 0, size: number = 20) =>
    request<PageResponse<StockRecord>>(`/inventory/records?page=${page}&size=${size}`),

  getStockRecordsByProduct: (productId: number) =>
    request<StockRecord[]>(`/inventory/records/product/${productId}`)
}
