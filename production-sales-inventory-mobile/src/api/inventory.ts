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
  shelfLifeDays: number | null
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

export type StockInSubType = 'PURCHASE' | 'PRODUCTION'

export type StockOutSubType = 'SALES' | 'PRODUCTION_USAGE'

export type StockRecordSubType = StockInSubType | StockOutSubType

export type StockLossSubType =
  | 'PRODUCTION_LOSS'
  | 'PACKAGING_LOSS'
  | 'SHIPPING_LOSS'
  | 'EXPIRED_LOSS'
  | 'DAMAGE_LOSS'
  | 'OTHER_LOSS'

export interface StockOperationRequest {
  productId: number
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

export interface StockLossRequest {
  productId: number
  batchId: number
  lossType: StockLossSubType
  quantity: number
  remark?: string
}

export interface StockRecordResponse {
  id: number
  recordNo: string
  productId: number
  productCode?: string
  productName?: string
  productUnit?: string
  type: 'IN' | 'OUT' | 'ADJUST'
  subType: string
  quantity: number
  costUnitPrice?: number | null
  costAmount?: number | null
  beforeQuantity: number
  afterQuantity: number
  batchId?: number | null
  batchNo?: string | null
  productionDate?: string | null
  expiryDate?: string | null
  operatorName: string
  remark?: string | null
  createdAt: string
}

export interface StockCheckRequest {
  productId: number
  batchId: number
  actualQuantity: number
  remark?: string
}

export type StockCheckResultType = 'GAIN' | 'LOSS' | 'MATCH'

export interface StockCheckOrderItemResponse {
  id: number
  productId: number
  productCode: string
  productName: string
  batchId: number
  batchNo: string
  systemQuantity: number
  actualQuantity: number
  differenceQuantity: number
  resultType: StockCheckResultType
  stockRecordId?: number
  remark?: string
}

export interface StockCheckOrderResponse {
  id: number
  checkNo: string
  status: 'DRAFT' | 'CONFIRMED' | 'CANCELLED'
  operatorName: string
  remark?: string
  confirmedAt?: string
  createdAt: string
  items: StockCheckOrderItemResponse[]
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

export interface DashboardWarningItem {
  name: string
  current: string
  safety: string
  days: string
  status: string
  statusClass: string
}

export interface InventoryDashboard {
  totalProducts: number
  lowStockCount: number
  outOfStockCount: number
  availableStockQuantity: number
  todayBusinessOverview: TodayBusinessOverview
  lowStockItems: StockItem[]
  rawMaterialWarnings: DashboardWarningItem[]
}

export interface ExpiringBatchItem {
  batchId: number
  productId: number
  productName: string
  batchNo: string
  expiryDate: string | null
  daysToExpire: number | null
  availableQuantity: number
  productUnit: string
}

export interface MobileHome {
  today: TodayBusinessOverview
  totalProducts: number
  lowStockCount: number
  outOfStockCount: number
  lowStockItems: StockItem[]
  rawMaterialWarnings: DashboardWarningItem[]
  expiringBatches: ExpiringBatchItem[]
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
    }),
  reportLoss: (data: StockLossRequest) =>
    request<StockRecordResponse>('/api/v1/inventory/loss', {
      method: 'POST',
      data,
      showLoading: true
    }),
  quickCheck: (data: StockCheckRequest) =>
    request<StockCheckOrderResponse>('/api/v1/inventory/check', {
      method: 'POST',
      data,
      showLoading: true
    }),
  getDashboard: () =>
    request<InventoryDashboard>('/api/v1/inventory/dashboard', {
      method: 'GET'
    }),
  getMobileHome: () =>
    request<MobileHome>('/api/v1/inventory/mobile/home', {
      method: 'GET'
    }),
  getAvailableBatches: (params: { page?: number; size?: number; expiringWithinDays?: number; availableOnly?: boolean } = {}) =>
    request<PageResponse<StockBatch>>('/api/v1/inventory/batches', {
      method: 'GET',
      data: {
        page: params.page ?? 0,
        size: params.size ?? 50,
        ...(params.expiringWithinDays !== undefined ? { expiringWithinDays: params.expiringWithinDays } : {}),
        ...(params.availableOnly !== undefined ? { availableOnly: params.availableOnly } : {})
      }
    }),
  getStockRecords: (params: { page?: number; size?: number; query?: string } = {}) =>
    request<PageResponse<StockRecordResponse>>('/api/v1/inventory/records', {
      method: 'GET',
      data: {
        page: params.page ?? 0,
        size: params.size ?? 20,
        ...(params.query?.trim() ? { query: params.query.trim() } : {})
      }
    }),
  getStockRecordsByProduct: (productId: number) =>
    request<StockRecordResponse[]>(`/api/v1/inventory/records/product/${productId}`, {
      method: 'GET'
    })
}
