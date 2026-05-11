import { request } from './http'

export interface SalesGoodsComponent {
  id?: number | undefined
  productId: number
  productCode?: string | undefined
  productName?: string | undefined
  productSpecification?: string | undefined
  productUnit?: string | undefined
  quantityPerUnit: number
  lossRate?: number | undefined
  remark?: string | undefined
}

export interface SalesGoodsChannelPrice {
  id?: number | undefined
  channelId: number
  channelName?: string | undefined
  price: number
  enabled: boolean
  remark?: string | undefined
}

export interface SalesGoods {
  id: number
  code: string
  name: string
  category?: string | undefined
  specification?: string | undefined
  unit: string
  defaultPrice: number
  enabled: boolean
  remark?: string | undefined
  createdAt: string
  updatedAt: string
  components: SalesGoodsComponent[]
  channelPrices: SalesGoodsChannelPrice[]
}

export interface SalesGoodsRequest {
  code: string
  name: string
  category?: string | undefined
  specification?: string | undefined
  unit: string
  defaultPrice?: number | undefined
  enabled: boolean
  remark?: string | undefined
  components: Array<{
    productId: number
    quantityPerUnit: number
    lossRate?: number | undefined
    remark?: string | undefined
  }>
  channelPrices: Array<{
    channelId: number
    price: number
    enabled: boolean
    remark?: string | undefined
  }>
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

export const salesGoodsApi = {
  getGoods: (page = 0, size = 20, query = '') =>
    request<PageResponse<SalesGoods>>(`/sales-goods?page=${page}&size=${size}&query=${encodeURIComponent(query)}`),

  getEnabledGoods: () =>
    request<SalesGoods[]>('/sales-goods/enabled'),

  getGoodsDetail: (id: number) =>
    request<SalesGoods>(`/sales-goods/${id}`),

  createGoods: (payload: SalesGoodsRequest) =>
    request<SalesGoods>('/sales-goods', { method: 'POST', body: payload }),

  updateGoods: (id: number, payload: SalesGoodsRequest) =>
    request<SalesGoods>(`/sales-goods/${id}`, { method: 'PUT', body: payload })
}
