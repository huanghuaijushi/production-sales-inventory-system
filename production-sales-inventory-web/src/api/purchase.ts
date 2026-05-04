import { request } from './http'

export type PurchaseOrderStatus = 'DRAFT' | 'PENDING_INBOUND' | 'INBOUNDED' | 'CANCELLED'

export interface PurchaseOrderItem {
  id: number
  productId: number
  productCode: string
  productName: string
  productSpecification?: string | undefined
  productUnit: string
  quantity: number
  unitPrice: number
  amount: number
}

export interface PurchaseOrder {
  id: number
  orderNo: string
  supplierId: number
  supplierName: string
  status: PurchaseOrderStatus
  statusText: string
  expectedArrivalDate?: string | undefined
  totalAmount: number
  operatorName: string
  remark?: string | undefined
  inboundAt?: string | undefined
  createdAt: string
  updatedAt: string
  items: PurchaseOrderItem[]
}

export interface PurchaseOrderItemRequest {
  productId: number
  quantity: number
  unitPrice: number
}

export interface PurchaseOrderRequest {
  supplierId: number
  draft?: boolean | undefined
  expectedArrivalDate?: string | undefined
  remark?: string | undefined
  items: PurchaseOrderItemRequest[]
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

export interface PurchaseOrderSearchParams {
  query?: string
  status?: string
}

export const purchaseApi = {
  getOrders: (page: number = 0, size: number = 20, filters: PurchaseOrderSearchParams = {}) => {
    const params = new URLSearchParams({
      page: String(page),
      size: String(size)
    })
    if (filters.query?.trim()) {
      params.set('query', filters.query.trim())
    }
    if (filters.status && filters.status !== 'all') {
      params.set('status', filters.status)
    }
    return request<PageResponse<PurchaseOrder>>(`/purchase-orders?${params.toString()}`)
  },

  createOrder: (order: PurchaseOrderRequest) =>
    request<PurchaseOrder>('/purchase-orders', {
      method: 'POST',
      body: order
    }),

  updateOrder: (orderId: number, order: PurchaseOrderRequest) =>
    request<PurchaseOrder>(`/purchase-orders/${orderId}`, {
      method: 'PUT',
      body: order
    }),

  inbound: (orderId: number) =>
    request<PurchaseOrder>(`/purchase-orders/${orderId}/inbound`, {
      method: 'POST'
    }),

  cancel: (orderId: number) =>
    request<PurchaseOrder>(`/purchase-orders/${orderId}/cancel`, {
      method: 'POST'
    })
}
