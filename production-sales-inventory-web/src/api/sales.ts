import { request } from './http'

export interface SalesOrderItem {
  id: number
  salesGoodsId: number
  salesGoodsCode: string
  salesGoodsName: string
  salesGoodsSpecification?: string | undefined
  salesGoodsUnit?: string | undefined
  salesGoodsCategory?: string | undefined
  externalProductName?: string | undefined
  externalSpecName?: string | undefined
  externalQuantity?: number | undefined
  mappingId?: number | undefined
  quantity: number
  unitPrice: number
  subtotal: number
}

export interface SalesOrder {
  id: number
  orderNo: string
  channel: 'DOUYIN' | 'PINDUODUO' | 'OFFLINE'
  channelId?: number | undefined
  channelText: string
  externalOrderNo?: string | undefined
  importBatchId?: number | undefined
  sourceType?: 'EXCEL' | 'TEXT' | 'MANUAL' | 'CONTRACT' | 'API' | undefined
  sourceRemark?: string | undefined
  customerName?: string | undefined
  customerPhone?: string | undefined
  customerAddress?: string | undefined
  totalAmount: number
  status: 'PENDING' | 'SHIPPED' | 'COMPLETED' | 'CANCELLED'
  statusText: string
  orderDate: string
  shipDate?: string | undefined
  completeDate?: string | undefined
  operatorName: string
  remark?: string | undefined
  createdAt: string
  updatedAt: string
  items: SalesOrderItem[]
}

export interface SalesOrderItemRequest {
  salesGoodsId: number
  quantity: number
  unitPrice: number
}

export interface SalesOrderRequest {
  channel: 'DOUYIN' | 'PINDUODUO' | 'OFFLINE'
  channelId?: number | undefined
  customerName?: string | undefined
  customerPhone?: string | undefined
  customerAddress?: string | undefined
  remark?: string | undefined
  items: SalesOrderItemRequest[]
}

export interface SalesChannelConfig {
  id: number
  code: string
  name: string
  sourceType: 'EXCEL' | 'TEXT' | 'MANUAL' | 'CONTRACT' | 'API'
  enabled: boolean
  sortOrder: number
  configJson?: string | undefined
  remark?: string | undefined
  createdAt: string
  updatedAt: string
}

export interface SalesChannelConfigRequest {
  code: string
  name: string
  sourceType: 'EXCEL' | 'TEXT' | 'MANUAL' | 'CONTRACT' | 'API'
  enabled: boolean
  sortOrder?: number | undefined
  configJson?: string | undefined
  remark?: string | undefined
}

export interface ChannelProductMapping {
  id: number
  channelId: number
  channelName: string
  externalProductName: string
  externalSpecName?: string | undefined
  externalSkuCode?: string | undefined
  salesGoodsId: number
  salesGoodsCode: string
  salesGoodsName: string
  salesGoodsSpecification?: string | undefined
  salesGoodsUnit?: string | undefined
  quantityMultiplier: number
  defaultUnitPrice?: number | undefined
  matchType: 'EXACT' | 'CONTAINS'
  enabled: boolean
  priority: number
  remark?: string | undefined
  createdAt: string
  updatedAt: string
}

export interface ChannelProductMappingRequest {
  channelId: number
  externalProductName: string
  externalSpecName?: string | undefined
  externalSkuCode?: string | undefined
  salesGoodsId: number
  quantityMultiplier: number
  defaultUnitPrice?: number | undefined
  matchType: 'EXACT' | 'CONTAINS'
  enabled: boolean
  priority?: number | undefined
  remark?: string | undefined
}

export interface ExternalOrderItemRaw {
  id: number
  externalProductName: string
  externalSpecName?: string | undefined
  externalSkuCode?: string | undefined
  externalQuantity: number
  externalUnitPrice: number
  resolvedUnitPrice?: number | undefined
  resolvedSubtotal?: number | undefined
  priceSource?: 'IMPORTED' | 'MAPPING_DEFAULT' | 'SALES_GOODS_DEFAULT_PRICE' | 'NONE' | undefined
  matchedSalesGoodsId?: number | undefined
  matchedGoodsCode?: string | undefined
  matchedGoodsName?: string | undefined
  mappingId?: number | undefined
  convertedQuantity?: number | undefined
  matchStatus: 'MATCHED' | 'UNMATCHED' | 'AMBIGUOUS' | 'ERROR'
  matchMessage?: string | undefined
}

export interface ExternalOrderRaw {
  id: number
  batchId: number
  channelId: number
  channelName: string
  externalOrderNo: string
  customerName?: string | undefined
  customerPhone?: string | undefined
  customerAddress?: string | undefined
  buyerMessage?: string | undefined
  sellerRemark?: string | undefined
  status: 'WAIT_MATCH' | 'READY' | 'ERROR' | 'CONVERTED' | 'SKIPPED'
  errorMessage?: string | undefined
  salesOrderId?: number | undefined
  createdAt: string
  items: ExternalOrderItemRaw[]
}

export interface OrderImportBatch {
  id: number
  batchNo: string
  channelId: number
  channelName: string
  sourceType: 'EXCEL' | 'TEXT' | 'MANUAL' | 'CONTRACT' | 'API'
  fileName?: string | undefined
  totalCount: number
  parsedCount: number
  readyCount: number
  convertedCount: number
  errorCount: number
  status: 'DRAFT' | 'PARSED' | 'CONFIRMED' | 'CANCELLED'
  operatorName: string
  createdAt: string
  updatedAt: string
  orders?: ExternalOrderRaw[] | undefined
}

export interface ExternalOrderEditItemRequest {
  id?: number | undefined
  externalProductName: string
  externalSpecName?: string | undefined
  externalSkuCode?: string | undefined
  externalQuantity: number
  externalUnitPrice: number
}

export interface ExternalOrderEditRequest {
  customerName?: string | undefined
  customerPhone?: string | undefined
  customerAddress?: string | undefined
  buyerMessage?: string | undefined
  sellerRemark?: string | undefined
  items: ExternalOrderEditItemRequest[]
}

export interface TextImportRequest {
  channelId: number
  rawText: string
}

export interface PddExcelImportRequest {
  channelId: number
  fileName: string
  rawText: string
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

export const salesApi = {
  getOrders: (page: number = 0, size: number = 10, query: string = '', status: string = 'all') =>
    request<PageResponse<SalesOrder>>(`/sales/orders?page=${page}&size=${size}&query=${encodeURIComponent(query)}&status=${encodeURIComponent(status)}`),

  createOrder: (payload: SalesOrderRequest) =>
    request<SalesOrder>('/sales/orders', { method: 'POST', body: payload }),

  updateOrder: (orderId: number, payload: SalesOrderRequest) =>
    request<SalesOrder>(`/sales/orders/${orderId}`, { method: 'PUT', body: payload }),

  shipOrder: (orderId: number) =>
    request<SalesOrder>(`/sales/orders/${orderId}/ship`, { method: 'POST' }),

  completeOrder: (orderId: number) =>
    request<SalesOrder>(`/sales/orders/${orderId}/complete`, { method: 'POST' }),

  cancelOrder: (orderId: number) =>
    request<SalesOrder>(`/sales/orders/${orderId}/cancel`, { method: 'POST' }),

  getChannels: (enabledOnly: boolean = false) =>
    request<SalesChannelConfig[]>(`/sales/channels?enabledOnly=${enabledOnly}`),

  createChannel: (payload: SalesChannelConfigRequest) =>
    request<SalesChannelConfig>('/sales/channels', { method: 'POST', body: payload }),

  updateChannel: (id: number, payload: SalesChannelConfigRequest) =>
    request<SalesChannelConfig>(`/sales/channels/${id}`, { method: 'PUT', body: payload }),

  getProductMappings: (channelId?: number) =>
    request<ChannelProductMapping[]>(channelId ? `/sales/product-mappings?channelId=${channelId}` : '/sales/product-mappings'),

  createProductMapping: (payload: ChannelProductMappingRequest) =>
    request<ChannelProductMapping>('/sales/product-mappings', { method: 'POST', body: payload }),

  updateProductMapping: (id: number, payload: ChannelProductMappingRequest) =>
    request<ChannelProductMapping>(`/sales/product-mappings/${id}`, { method: 'PUT', body: payload }),

  getImportBatches: (page: number = 0, size: number = 10) =>
    request<PageResponse<OrderImportBatch>>(`/sales/imports?page=${page}&size=${size}`),

  getImportBatch: (batchId: number) =>
    request<OrderImportBatch>(`/sales/imports/${batchId}`),

  importText: (payload: TextImportRequest) =>
    request<OrderImportBatch>('/sales/imports/text', { method: 'POST', body: payload }),

  importWechatText: (payload: TextImportRequest) =>
    request<OrderImportBatch>('/sales/imports/wechat-text', { method: 'POST', body: payload }),

  importPddExcel: (payload: PddExcelImportRequest) =>
    request<OrderImportBatch>('/sales/imports/pdd-excel', { method: 'POST', body: payload }),

  parseImportBatch: (batchId: number) =>
    request<OrderImportBatch>(`/sales/imports/${batchId}/parse`, { method: 'POST' }),

  updateExternalOrder: (batchId: number, externalOrderId: number, payload: ExternalOrderEditRequest) =>
    request<OrderImportBatch>(`/sales/imports/${batchId}/orders/${externalOrderId}`, { method: 'PUT', body: payload }),

  confirmImportBatch: (batchId: number) =>
    request<OrderImportBatch>(`/sales/imports/${batchId}/confirm`, { method: 'POST' })
}
