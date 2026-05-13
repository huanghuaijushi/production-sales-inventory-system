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
  skus?: SalesSku[] | undefined
}

export interface SalesSkuComponent {
  id?: number | undefined
  productId: number
  productCode?: string | undefined
  productName?: string | undefined
  productSpecification?: string | undefined
  productUnit?: string | undefined
  quantity: number
  remark?: string | undefined
}

export interface SalesSku {
  id: number
  code: string
  name: string
  specName?: string | undefined
  unit: string
  perSkuPrice: number
  enabled: boolean
  salesGoodsId?: number | undefined
  salesGoodsName?: string | undefined
  remark?: string | undefined
  createdAt: string
  updatedAt: string
  components: SalesSkuComponent[]
}

export interface SalesSkuComponentRequest {
  productId: number
  quantity: number
  remark?: string | undefined
}

export interface SalesSkuRequest {
  code: string
  name: string
  specName?: string | undefined
  unit: string
  perSkuPrice?: number | undefined
  enabled?: boolean | undefined
  salesGoodsId?: number | undefined
  remark?: string | undefined
  components: SalesSkuComponentRequest[]
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
    request<SalesGoods>(`/sales-goods/${id}`, { method: 'PUT', body: payload }),

  getSkus: (goodsId: number) =>
    request<SalesSku[]>(`/sales-goods/${goodsId}/skus`),

  getSkuDetail: (goodsId: number, skuId: number) =>
    request<SalesSku>(`/sales-goods/${goodsId}/skus/${skuId}`),

  createSku: (goodsId: number, payload: SalesSkuRequest) =>
    request<SalesSku>(`/sales-goods/${goodsId}/skus`, { method: 'POST', body: payload }),

  updateSku: (goodsId: number, skuId: number, payload: SalesSkuRequest) =>
    request<SalesSku>(`/sales-goods/${goodsId}/skus/${skuId}`, { method: 'PUT', body: payload }),

  deleteSku: (goodsId: number, skuId: number) =>
    request<void>(`/sales-goods/${goodsId}/skus/${skuId}`, { method: 'DELETE' })
}
