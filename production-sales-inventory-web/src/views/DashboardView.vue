<template>
  <div class="dashboard-page">
    <div class="grid-container">
      <!-- 第一行：数据概览卡片 -->
      <div class="grid-row">
        <DataCard
          title="总商品数"
          :value="formatNumber(dashboardStats.totalProducts)"
          :subtitle="dashboardLoading ? '正在加载' : '已启用商品数量'"
          icon="cube"
          color="#1890ff"
        />
        <DataCard
          title="库存预警"
          :value="formatNumber(dashboardStats.lowStockCount)"
          :subtitle="hasPositiveValue(dashboardStats.lowStockCount) ? '需要及时补货' : '库存状态良好'"
          icon="warning"
          color="#52C41A"
        />
        <DataCard
          title="缺货商品"
          :value="formatNumber(dashboardStats.outOfStockCount)"
          :subtitle="hasPositiveValue(dashboardStats.outOfStockCount) ? '紧急补货' : '暂无缺货'"
          icon="danger"
          color="#FAAD14"
        />
        <DataCard
          title="可用库存"
          :value="formatNumber(dashboardStats.availableStockQuantity)"
          :subtitle="hasPositiveValue(dashboardStats.availableStockQuantity) ? '全部商品可用库存合计' : '暂无可用库存'"
          icon="success"
          color="#722ED1"
        />
      </div>

      <!-- 第二行：快捷操作 + 订单流向 -->
      <div class="grid-row">
        <QuickActionsCard
          @inbound="openInboundModal"
          @outbound="openOutboundModal"
          @stocktake="goInventory"
          @report="goInventory"
        />
        <FlowCard
          :overview="todayBusinessOverview"
          :loading="dashboardLoading"
        />
      </div>

      <!-- 第三行：图表区域 -->
      <div class="grid-row">
        <ChartCard
          type="line"
          :trend="inventoryTrend"
          :trend-days="trendDays"
          :loading="trendLoading"
          @trend-days-change="handleTrendDaysChange"
        />
        <ChartCard type="pie" :distribution="inventoryDistribution" />
      </div>

      <!-- 第四行：表格 + 时间线 -->
      <div class="grid-row">
        <TableCard
          :items="lowStockItems"
          :loading="dashboardLoading"
        />
        <TimelineCard
          :records="recentStockRecords"
          :loading="recordsLoading"
        />
      </div>
    </div>

    <p v-if="operationMessage" class="operation-message">{{ operationMessage }}</p>

    <StockOperationModal
      :is-open="modalOpen"
      :is-inbound="isInbound"
      @close="closeModal"
      @success="handleOperationSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import DataCard from '@/components/dashboard/DataCard.vue'
import QuickActionsCard from '@/components/dashboard/QuickActionsCard.vue'
import FlowCard from '@/components/dashboard/FlowCard.vue'
import ChartCard from '@/components/dashboard/ChartCard.vue'
import TableCard from '@/components/dashboard/TableCard.vue'
import TimelineCard from '@/components/dashboard/TimelineCard.vue'
import StockOperationModal from '@/components/inventory/StockOperationModal.vue'
import {
  inventoryApi,
  type InventoryDistributionItem,
  type StockItem,
  type StockRecord,
  type StockTrendItem,
  type TodayBusinessOverview
} from '@/api/inventory'

const router = useRouter()
const modalOpen = ref(false)
const isInbound = ref(true)
const operationMessage = ref('')
const dashboardLoading = ref(false)
const recordsLoading = ref(false)
const trendLoading = ref(false)
const dashboardStats = reactive({
  totalProducts: null as number | null,
  lowStockCount: null as number | null,
  outOfStockCount: null as number | null,
  availableStockQuantity: null as number | null
})
const todayBusinessOverview = ref<TodayBusinessOverview | null>(null)
const inventoryDistribution = ref<InventoryDistributionItem[]>([])
const inventoryTrend = ref<StockTrendItem[]>([])
const lowStockItems = ref<StockItem[]>([])
const recentStockRecords = ref<StockRecord[]>([])
const trendDays = ref(7)
let messageTimer: number | undefined
let dashboardRefreshTimer: number | undefined
let dashboardRefreshing = false
let trendRequestId = 0
const DASHBOARD_REFRESH_INTERVAL = 15_000

async function loadDashboardStats(showLoading = false) {
  if (dashboardRefreshing) {
    return
  }

  dashboardRefreshing = true
  if (showLoading) {
    dashboardLoading.value = true
    recordsLoading.value = true
  }

  try {
    const [dashboardResult, recordsResult] = await Promise.allSettled([
      inventoryApi.getDashboard(),
      inventoryApi.getStockRecords(0, 6)
    ])

    if (dashboardResult.status === 'fulfilled') {
      dashboardStats.totalProducts = dashboardResult.value.totalProducts
      dashboardStats.lowStockCount = dashboardResult.value.lowStockCount
      dashboardStats.outOfStockCount = dashboardResult.value.outOfStockCount
      dashboardStats.availableStockQuantity = dashboardResult.value.availableStockQuantity
      todayBusinessOverview.value = dashboardResult.value.todayBusinessOverview
      inventoryDistribution.value = dashboardResult.value.inventoryDistribution
      lowStockItems.value = dashboardResult.value.lowStockItems
    } else {
      console.error('加载首页统计失败:', dashboardResult.reason)
    }

    if (recordsResult.status === 'fulfilled') {
      recentStockRecords.value = recordsResult.value.content
    } else {
      console.error('加载操作日志失败:', recordsResult.reason)
    }
  } catch (error) {
    console.error('加载首页统计失败:', error)
  } finally {
    dashboardRefreshing = false
    if (showLoading) {
      dashboardLoading.value = false
    }
    recordsLoading.value = false
  }
}

async function loadStockTrend(showLoading = false) {
  const requestId = ++trendRequestId
  if (showLoading) {
    trendLoading.value = true
  }

  try {
    const trend = await inventoryApi.getStockTrend(trendDays.value)
    if (requestId === trendRequestId) {
      inventoryTrend.value = trend
    }
  } catch (error) {
    console.error('加载库存趋势失败:', error)
  } finally {
    if (requestId === trendRequestId) {
      trendLoading.value = false
    }
  }
}

function startDashboardAutoRefresh() {
  window.clearInterval(dashboardRefreshTimer)
  dashboardRefreshTimer = window.setInterval(() => {
    if (document.visibilityState === 'visible') {
      loadDashboardStats()
      loadStockTrend()
    }
  }, DASHBOARD_REFRESH_INTERVAL)
}

function stopDashboardAutoRefresh() {
  window.clearInterval(dashboardRefreshTimer)
  dashboardRefreshTimer = undefined
}

function handleVisibilityChange() {
  if (document.visibilityState === 'visible') {
    loadDashboardStats()
    loadStockTrend()
  }
}

function formatNumber(value: number | null) {
  return value === null ? '--' : new Intl.NumberFormat('zh-CN').format(value)
}

function hasPositiveValue(value: number | null) {
  return value !== null && value > 0
}

function openInboundModal() {
  isInbound.value = true
  modalOpen.value = true
}

function openOutboundModal() {
  isInbound.value = false
  modalOpen.value = true
}

function closeModal() {
  modalOpen.value = false
}

function handleOperationSuccess() {
  closeModal()
  loadDashboardStats()
  loadStockTrend()
  operationMessage.value = isInbound.value ? '入库成功，库存已更新。' : '出库成功，库存已更新。'
  window.clearTimeout(messageTimer)
  messageTimer = window.setTimeout(() => {
    operationMessage.value = ''
  }, 2600)
}

function goInventory() {
  router.push({ name: 'inventory' })
}

function handleTrendDaysChange(days: number) {
  if (trendDays.value === days) {
    return
  }

  trendDays.value = days
  loadStockTrend(true)
}

onMounted(() => {
  loadDashboardStats(true)
  loadStockTrend(true)
  startDashboardAutoRefresh()
  document.addEventListener('visibilitychange', handleVisibilityChange)
})

onUnmounted(() => {
  stopDashboardAutoRefresh()
  window.clearTimeout(messageTimer)
  document.removeEventListener('visibilitychange', handleVisibilityChange)
})
</script>

<style scoped>
.dashboard-page {
  width: 100%;
  position: relative;
}

.grid-container {
  display: grid;
  gap: 16px;
}

.grid-row {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.grid-row:nth-child(2),
.grid-row:nth-child(3),
.grid-row:nth-child(4) {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

@media (max-width: 1200px) {
  .grid-row {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .grid-row:nth-child(2),
  .grid-row:nth-child(3),
  .grid-row:nth-child(4) {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .grid-row {
    grid-template-columns: 1fr;
  }
}

.operation-message {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 50;
  margin: 0;
  padding: 12px 16px;
  border: 1px solid #bbf7d0;
  border-radius: 10px;
  background: #f0fdf4;
  color: #166534;
  box-shadow: 0 16px 30px rgba(15, 23, 42, 0.12);
  font-size: 14px;
  font-weight: 700;
}
</style>
