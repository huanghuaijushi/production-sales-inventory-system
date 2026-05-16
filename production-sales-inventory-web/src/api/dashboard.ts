import { request } from './http'

export interface SalesMonthBlock {
  currentMonthAmount: number
  lastMonthAmount: number
  growthPercent: number | null
  todayAmount: number
  currency: string
}

export interface RedAlertItem {
  productId: number
  productName: string
  productType: 'FINISHED_PRODUCT' | 'RAW_MATERIAL' | 'PACKAGING_MATERIAL' | 'SEMI_FINISHED_PRODUCT'
  current: number
  safety: number
}

export interface YellowAlertItem {
  productId: number
  productName: string
  batchNo: string
  daysToExpiry: number
}

export interface InventoryAlertsBlock {
  redCount: number
  yellowCount: number
  greenCount: number
  redAlerts: RedAlertItem[]
  yellowAlerts: YellowAlertItem[]
}

export interface TodayTodosBlock {
  pendingShipments: number
  pendingInbounds: number
  pendingApprovals: number
}

export interface ReceivablesAndCashBlock {
  status: string
}

export interface HomeDashboard {
  salesMonth: SalesMonthBlock
  receivablesAndCash: ReceivablesAndCashBlock | null
  inventoryAlerts: InventoryAlertsBlock
  todayTodos: TodayTodosBlock
}

export const dashboardApi = {
  getHome(): Promise<HomeDashboard> {
    return request<HomeDashboard>('/dashboard/home')
  }
}
