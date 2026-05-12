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

export type ProductionOrderStatus = 'PLANNED' | 'IN_PROGRESS' | 'WAIT_INBOUND' | 'COMPLETED' | 'CANCELLED'
export type ProductionStepType = string

export interface ProductionOrderSummary {
  id: number
  orderNo: string
  batchNo: string
  productId: number
  productCode: string
  productName: string
  productUnit: string
  plannedQuantity: number
  completedQuantity: number
  inboundQuantity: number
  lossQuantity: number
  currentStep: ProductionStepType | null
  status: ProductionOrderStatus
  plannedDate: string | null
  startedAt: string | null
  completedAt: string | null
  operatorName: string
  remark: string | null
  createdAt: string
}

export interface ProductionMaterialPlan {
  id: number
  materialProductId: number
  materialProductCode: string
  materialProductName: string
  materialProductUnit: string
  requiredQuantity: number
  issuedQuantity: number
}

export interface ProductionMaterialIssue {
  id: number
  materialPlanId: number
  materialProductId: number
  materialProductCode: string
  materialProductName: string
  materialProductUnit: string
  stockBatchId: number
  batchNo: string
  issuedQuantity: number
  stockRecordId: number | null
  operatorName: string
  remark: string | null
  createdAt: string
}

export interface ProductionStepRecord {
  id: number
  stepType: ProductionStepType
  stepName: string
  completedQuantity: number
  lossQuantity: number
  lossReason: string | null
  operatorName: string
  createdAt: string
}

export interface ProductionOrderStep {
  id: number | null
  stepCode: ProductionStepType
  stepName: string
  sortOrder: number
  allowLoss: boolean
  completedQuantity: number
  lossQuantity: number
}

export interface ProductionOrderDetail {
  order: ProductionOrderSummary
  materialPlans: ProductionMaterialPlan[]
  materialIssues: ProductionMaterialIssue[]
  routeSteps: ProductionOrderStep[]
  stepRecords: ProductionStepRecord[]
}

export interface ProductionOrderCreateRequest {
  productId: number
  plannedQuantity: number
  batchNo?: string | undefined
  plannedDate?: string | undefined
  remark?: string | undefined
}

export interface ProductionMaterialIssueRequest {
  materialPlanId: number
  batchId: number
  quantity: number
  remark?: string | undefined
}

export interface ProductionStepRecordRequest {
  lossQuantity: number
  lossReason?: string | undefined
}

export interface ProductionInboundRequest {
  quantity: number
  productionDate?: string | undefined
  expiryDate?: string | undefined
  remark?: string | undefined
}

export interface ProductionRouteStep {
  id: number
  productId: number
  productCode: string
  productName: string
  stepCode: string
  stepName: string
  sortOrder: number
  allowLoss: boolean
  enabled: boolean
}

export interface ProductionRouteStepRequest {
  productId: number
  stepCode: string
  stepName: string
  sortOrder: number
  allowLoss?: boolean | undefined
  enabled?: boolean | undefined
}

export const productionApi = {
  getOrders: () => request<ProductionOrderSummary[]>('/production/orders'),

  getOrder: (productionOrderId: number) =>
    request<ProductionOrderDetail>(`/production/orders/${productionOrderId}`),

  getRouteSteps: () => request<ProductionRouteStep[]>('/production/route-steps'),

  createRouteStep: (item: ProductionRouteStepRequest) =>
    request<ProductionRouteStep>('/production/route-steps', {
      method: 'POST',
      body: item
    }),

  updateRouteStep: (routeStepId: number, item: ProductionRouteStepRequest) =>
    request<ProductionRouteStep>(`/production/route-steps/${routeStepId}`, {
      method: 'PUT',
      body: item
    }),

  deleteRouteStep: (routeStepId: number) =>
    request<void>(`/production/route-steps/${routeStepId}`, {
      method: 'DELETE'
    }),

  createOrder: (item: ProductionOrderCreateRequest) =>
    request<ProductionOrderDetail>('/production/orders', {
      method: 'POST',
      body: item
    }),

  startOrder: (productionOrderId: number) =>
    request<ProductionOrderDetail>(`/production/orders/${productionOrderId}/start`, {
      method: 'POST'
    }),

  issueMaterial: (productionOrderId: number, item: ProductionMaterialIssueRequest) =>
    request<ProductionOrderDetail>(`/production/orders/${productionOrderId}/materials/issue`, {
      method: 'POST',
      body: item
    }),

  recordStep: (productionOrderId: number, item: ProductionStepRecordRequest) =>
    request<ProductionOrderDetail>(`/production/orders/${productionOrderId}/steps`, {
      method: 'POST',
      body: item
    }),

  inboundProduction: (productionOrderId: number, item: ProductionInboundRequest) =>
    request<ProductionOrderDetail>(`/production/orders/${productionOrderId}/inbound`, {
      method: 'POST',
      body: item
    }),

  cancelOrder: (productionOrderId: number) =>
    request<ProductionOrderDetail>(`/production/orders/${productionOrderId}/cancel`, {
      method: 'POST'
    }),

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
