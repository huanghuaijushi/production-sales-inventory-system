import { request } from './http'

export interface BomItem {
  id: number
  finishedProductId: number
  finishedProductCode: string
  finishedProductName: string
  materialProductId: number
  materialProductCode: string
  materialProductName: string
  materialProductUnit: string
  quantityPerUnit: number
  lossRate: number
}

export interface BomItemRequest {
  finishedProductId: number
  materialProductId: number
  quantityPerUnit: number
  lossRate?: number | undefined
}

export interface SupplierMaterial {
  id: number
  supplierId: number
  supplierName: string
  productId: number
  productCode: string
  productName: string
  productUnit: string
  defaultUnitPrice: number
  minOrderQuantity: number
  orderMultiple: number
  leadTimeDays: number
  preferred: boolean
  remark?: string | undefined
}

export interface SupplierMaterialRequest {
  supplierId: number
  productId: number
  defaultUnitPrice?: number | undefined
  minOrderQuantity?: number | undefined
  orderMultiple?: number | undefined
  leadTimeDays?: number | undefined
  preferred?: boolean | undefined
  remark?: string | undefined
}

export interface ProductionMaterialCapacity {
  materialProductId: number
  materialProductCode: string
  materialProductName: string
  materialProductUnit: string
  quantityPerUnit: number
  lossRate: number
  availableQuantity: number
  maxSupportQuantity: number
}

export interface ProductionCapacity {
  finishedProductId: number
  finishedProductCode: string
  finishedProductName: string
  finishedProductUnit: string
  currentStock: number
  availableStock: number
  alertQuantity: number
  hasBom: boolean
  maxProducibleQuantity: number
  bottleneckMaterialName?: string | undefined
  materials: ProductionMaterialCapacity[]
}

export interface ProductionMaterialRequirement {
  materialProductId: number
  materialProductCode: string
  materialProductName: string
  materialProductUnit: string
  requiredQuantity: number
  availableQuantity: number
  onOrderQuantity: number
  shortageQuantity: number
  defaultSupplierId?: number | undefined
  defaultSupplierName?: string | undefined
  defaultUnitPrice: number
  suggestedPurchaseQuantity: number
  leadTimeDays: number
  suggestionReason: string
}

export interface ProductionSuggestion {
  finishedProductId: number
  finishedProductCode: string
  finishedProductName: string
  finishedProductUnit: string
  currentStock: number
  availableStock: number
  alertQuantity: number
  suggestedProductionQuantity: number
  maxProducibleQuantity: number
  canProduceNow: boolean
  suggestionReason: string
  materialRequirements: ProductionMaterialRequirement[]
}

export interface PurchaseSuggestionItem {
  productId: number
  productCode: string
  productName: string
  productUnit: string
  shortageQuantity: number
  suggestedPurchaseQuantity: number
  availableQuantity: number
  onOrderQuantity: number
  defaultUnitPrice: number
  amount: number
  leadTimeDays: number
  reason: string
}

export interface PurchaseSuggestionGroup {
  supplierId?: number | undefined
  supplierName: string
  expectedArrivalDate?: string | undefined
  totalAmount: number
  items: PurchaseSuggestionItem[]
}

export const productionApi = {
  getBomItems: () => request<BomItem[]>('/production/bom'),

  createBomItem: (item: BomItemRequest) =>
    request<BomItem>('/production/bom', {
      method: 'POST',
      body: item
    }),

  updateBomItem: (bomItemId: number, item: BomItemRequest) =>
    request<BomItem>(`/production/bom/${bomItemId}`, {
      method: 'PUT',
      body: item
    }),

  deleteBomItem: (bomItemId: number) =>
    request<void>(`/production/bom/${bomItemId}`, {
      method: 'DELETE'
    }),

  getSupplierMaterials: () => request<SupplierMaterial[]>('/production/supplier-materials'),

  createSupplierMaterial: (item: SupplierMaterialRequest) =>
    request<SupplierMaterial>('/production/supplier-materials', {
      method: 'POST',
      body: item
    }),

  updateSupplierMaterial: (supplierMaterialId: number, item: SupplierMaterialRequest) =>
    request<SupplierMaterial>(`/production/supplier-materials/${supplierMaterialId}`, {
      method: 'PUT',
      body: item
    }),

  deleteSupplierMaterial: (supplierMaterialId: number) =>
    request<void>(`/production/supplier-materials/${supplierMaterialId}`, {
      method: 'DELETE'
    }),

  getCapacity: () => request<ProductionCapacity[]>('/production/capacity'),

  getSuggestions: () => request<ProductionSuggestion[]>('/production/suggestions'),

  getPurchaseSuggestions: () => request<PurchaseSuggestionGroup[]>('/production/purchase-suggestions')
}
