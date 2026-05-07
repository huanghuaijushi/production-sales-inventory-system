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

export interface DashboardMetricItem {
  key: string
  title: string
  value: string
  subtitle: string
  icon: string
  accentColor: string
  change: string
  trendClass: string
  progress: string
  progressColor: string
}

export interface DashboardChartBar {
  label: string
  inbound: string | null
  outbound: string | null
  stock: string | null
  production: string | null
  sales: string | null
  inboundValue: string | null
  outboundValue: string | null
  stockValue: string | null
  productionValue: string | null
  salesValue: string | null
  inboundQuantityValue: string | null
  outboundQuantityValue: string | null
  stockQuantityValue: string | null
  productionAmountValue: string | null
  salesAmountValue: string | null
  stockAmountValue: string | null
}

export interface DashboardWarningItem {
  name: string
  current: string
  safety: string
  days: string
  status: string
  statusClass: string
}

export interface DashboardRankingItem {
  rank: number
  name: string
  percent: string
  value: string
}

export interface DashboardCategoryShare {
  name: string
  percent: string
  value: string
  ratio: string
  color: string
  productCount: number
  quantity: number
}

export interface DashboardSummaryItem {
  label: string
  value: string
  meta: string
}

export interface ProfitOverview {
  totalRevenue: string
  totalCost: string
  grossProfit: string
  grossMargin: string
  averageOrderRevenue: string
  averageOrderGrossProfit: string
  metrics: DashboardMetricItem[]
  trendBars: DashboardChartBar[]
  channelRanking: DashboardRankingItem[]
  summaries: DashboardSummaryItem[]
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
  topMetrics: DashboardMetricItem[]
  rawMaterialBars: DashboardChartBar[]
  finishedProductBars: DashboardChartBar[]
  rawMaterialWarnings: DashboardWarningItem[]
  hotProducts: DashboardRankingItem[]
  rawCategoryShares: DashboardCategoryShare[]
  finishedCategoryShares: DashboardCategoryShare[]
  rawSummaryCards: DashboardSummaryItem[]
  finishedSummaryCards: DashboardSummaryItem[]
  profitOverview: ProfitOverview
}

export interface StockTrendItem {
  date: string
  label: string
  inboundQuantity: number
  outboundQuantity: number
  netChangeQuantity: number
  rawMaterialInboundQuantity: number
  rawMaterialOutboundQuantity: number
  finishedProductInboundQuantity: number
  finishedProductOutboundQuantity: number
  productionLossQuantity: number
}

export interface TrendChartItem {
  date: string
  label: string
  [key: string]: string | number
}

export interface BusinessFlowTrendItem extends TrendChartItem {
  totalCount: number
  purchaseInboundCount: number
  productionUsageCount: number
  finishedProductInboundCount: number
  salesOutboundCount: number
  productionLossCount: number
}

export interface InventoryValueTrendItem extends TrendChartItem {
  totalAmount: number
  rawMaterialInboundAmount: number
  rawMaterialUsageAmount: number
  finishedProductInboundAmount: number
  finishedProductSalesAmount: number
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
  subType: StockRecordSubType
  quantity: number
  businessUnitPrice?: number | null
  businessAmount?: number | null
  costUnitPrice?: number | null
  costAmount?: number | null
  beforeQuantity: number
  afterQuantity: number
  batchId: number | null
  batchNo: string
  productionDate: string
  expiryDate: string
  operatorName: string
  remark: string
  createdAt: string
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

export const inventoryApi = {
  getDashboard: () => request<InventoryDashboard>('/inventory/dashboard'),

  getStockTrend: (days: number = 7) =>
    request<StockTrendItem[]>(`/inventory/trends?days=${days}`),

  getBusinessFlowTrend: (days: number = 7) =>
    request<BusinessFlowTrendItem[]>(`/inventory/business-flow-trends?days=${days}`),

  getInventoryValueTrend: (days: number = 7) =>
    request<InventoryValueTrendItem[]>(`/inventory/value-trends?days=${days}`),

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

  getBatchesByProductId: (productId: number) =>
    request<StockBatch[]>(`/inventory/batches/product/${productId}`),

  updateStock: (stockId: number, req: StockUpdateRequest) =>
    request<StockItem>(`/inventory/stocks/${stockId}`, { method: 'PUT', body: req }),

  inbound: (req: StockOperationRequest) =>
    request<StockRecord>('/inventory/inbound', { method: 'POST', body: req }),

  outbound: (req: StockOperationRequest) =>
    request<StockRecord>('/inventory/outbound', { method: 'POST', body: req }),

  getStockRecords: (page: number = 0, size: number = 20) =>
    request<PageResponse<StockRecord>>(`/inventory/records?page=${page}&size=${size}`)
}
