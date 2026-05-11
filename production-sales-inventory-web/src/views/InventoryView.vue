<template>
  <main class="inventory-page">
    <section class="page-header">
      <div>
        <p class="page-eyebrow">库存管理</p>
        <h1>库存总览</h1>
        <p>按商品名称、SKU、分类和状态筛选库存数据，快速查看当前库存健康状况。</p>
      </div>
    </section>

    <InventoryFilterBar
      :filters="filters"
      @update:filters="updateFilters"
      @inbound="openInboundModal"
      @outbound="openOutboundModal"
      @export="exportInventoryReport"
    />

    <InventoryTableCard
      :items="pagedItems"
      :loading="loading"
      :pageSize="pageSize"
      @edit="openStockEditModal"
    />

    <InventoryPagination
      :total="totalElements"
      :page="page"
      :pageSize="pageSize"
      @update:page="setPage"
    />

    <section class="stock-record-card">
      <header class="stock-record-header">
        <div>
          <p class="page-eyebrow">库存流水</p>
          <h2>最近出入库记录</h2>
        </div>
        <button type="button" class="record-refresh" :disabled="recordsLoading" @click="loadRecords">
          {{ recordsLoading ? '刷新中...' : '刷新流水' }}
        </button>
      </header>

      <div class="record-table-wrapper">
        <table class="record-table">
          <thead>
            <tr>
              <th>单据号</th>
              <th>类型</th>
              <th>商品</th>
              <th>批次</th>
              <th>数量</th>
              <th>变更前</th>
              <th>变更后</th>
              <th>操作人</th>
              <th>时间</th>
              <th>备注</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="recordsLoading">
              <td colspan="10">正在加载库存流水...</td>
            </tr>
            <tr v-else-if="stockRecords.length === 0">
              <td colspan="10">暂无出入库记录。</td>
            </tr>
            <tr v-for="record in stockRecords" v-else :key="record.id">
              <td>{{ record.recordNo }}</td>
              <td>
                <span :class="['record-type', record.type === 'IN' ? 'record-type--in' : 'record-type--out']">
                  {{ recordTypeLabel(record.type, record.subType) }}
                </span>
              </td>
              <td>{{ record.productName }}</td>
              <td>{{ record.batchNo || '-' }}</td>
              <td :class="record.quantity >= 0 ? 'quantity-in' : 'quantity-out'">
                {{ record.quantity > 0 ? '+' : '' }}{{ record.quantity }}{{ record.productUnit }}
              </td>
              <td>{{ record.beforeQuantity }}</td>
              <td>{{ record.afterQuantity }}</td>
              <td>{{ record.operatorName }}</td>
              <td>{{ formatDateTime(record.createdAt) }}</td>
              <td>{{ record.remark || '-' }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>

    <StockOperationModal
      :is-open="modalOpen"
      :is-inbound="isInbound"
      v-bind="initialOperationSubType ? { initialSubType: initialOperationSubType } : {}"
      @close="closeModal"
      @success="onOperationSuccess"
    />

    <StockEditModal
      :is-open="stockEditModalOpen"
      :item="selectedStockItem"
      @close="closeStockEditModal"
      @success="onStockEditSuccess"
    />
  </main>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import * as XLSX from 'xlsx'
import InventoryFilterBar from '@/components/inventory/InventoryFilterBar.vue'
import InventoryTableCard from '@/components/inventory/InventoryTableCard.vue'
import InventoryPagination from '@/components/inventory/InventoryPagination.vue'
import StockOperationModal from '@/components/inventory/StockOperationModal.vue'
import StockEditModal from '@/components/inventory/StockEditModal.vue'
import { inventoryApi, type StockItem, type StockRecord, type PageResponse, type StockRecordSubType } from '@/api/inventory'

const route = useRoute()
const page = ref(0)
const pageSize = ref(8)
const loading = ref(false)
const recordsLoading = ref(false)
const exporting = ref(false)
const modalOpen = ref(false)
const isInbound = ref(true)
const initialOperationSubType = ref<StockRecordSubType | undefined>()
const stockEditModalOpen = ref(false)
const selectedStockItem = ref<StockItem | null>(null)
const totalElements = ref(0)
const totalPages = ref(0)
const stockRecords = ref<StockRecord[]>([])
const filters = reactive({
  query: '',
  category: 'all',
  status: 'all'
})

const inventoryItems = ref<StockItem[]>([])

const pagedItems = inventoryItems

async function loadData() {
  loading.value = true
  try {
    const result: PageResponse<StockItem> = await inventoryApi.getAllStocks(page.value, pageSize.value, filters)
    inventoryItems.value = result.content
    totalElements.value = result.totalElements
    totalPages.value = result.totalPages
  } catch (error) {
    console.error('加载库存数据失败:', error)
  } finally {
    loading.value = false
  }
}

async function loadRecords() {
  recordsLoading.value = true
  try {
    const result: PageResponse<StockRecord> = await inventoryApi.getStockRecords(0, 10)
    stockRecords.value = result.content
  } catch (error) {
    console.error('加载库存流水失败:', error)
  } finally {
    recordsLoading.value = false
  }
}

function updateFilters(nextFilters: typeof filters) {
  Object.assign(filters, nextFilters)
  page.value = 0
  loadData()
}

function setPage(nextPage: number) {
  page.value = nextPage
  loadData()
}

function openInboundModal(subType?: StockRecordSubType) {
  isInbound.value = true
  initialOperationSubType.value = subType
  modalOpen.value = true
}

function openOutboundModal(subType?: StockRecordSubType) {
  isInbound.value = false
  initialOperationSubType.value = subType
  modalOpen.value = true
}

function closeModal() {
  modalOpen.value = false
  initialOperationSubType.value = undefined
}

function onOperationSuccess() {
  closeModal()
  loadData()
  loadRecords()
}

function openStockEditModal(item: StockItem) {
  selectedStockItem.value = item
  stockEditModalOpen.value = true
}

function closeStockEditModal() {
  stockEditModalOpen.value = false
  selectedStockItem.value = null
}

function onStockEditSuccess() {
  closeStockEditModal()
  loadData()
  loadRecords()
}

async function exportInventoryReport() {
  if (exporting.value) return

  exporting.value = true
  try {
    const result = await inventoryApi.getAllStocks(0, Math.max(totalElements.value, 1), filters)
    exportInventoryWorkbook(result.content, `库存报表-${formatDateForFile(new Date())}.xlsx`)
  } catch (error) {
    console.error('导出库存报表失败:', error)
  } finally {
    exporting.value = false
  }
}

function routeSearchKeyword() {
  return typeof route.query.q === 'string' ? route.query.q : ''
}

function applyRouteSearch() {
  filters.query = routeSearchKeyword()
  page.value = 0
  loadData()
}

watch(
  () => route.query.q,
  () => {
    applyRouteSearch()
  }
)

watch(
  () => route.query.action,
  () => {
    applyRouteAction()
  }
)

onMounted(() => {
  applyRouteSearch()
  applyRouteAction()
  loadRecords()
})

function routeSubType(): StockRecordSubType | undefined {
  const value = route.query.subType
  const allowed: StockRecordSubType[] = [
    'PRODUCTION',
    'PURCHASE',
    'SALES',
    'PRODUCTION_USAGE',
    'PRODUCTION_LOSS',
    'PACKAGING_LOSS',
    'SHIPPING_LOSS',
    'INVENTORY'
  ]
  return typeof value === 'string' && allowed.includes(value as StockRecordSubType)
    ? value as StockRecordSubType
    : undefined
}

function applyRouteAction() {
  const action = route.query.action
  if (action === 'inbound') {
    openInboundModal(routeSubType())
  } else if (action === 'outbound') {
    openOutboundModal(routeSubType())
  }
}

function recordTypeLabel(type: StockRecord['type'], subType: StockRecord['subType']) {
  const subTypeMap: Record<StockRecord['subType'], string> = {
    PURCHASE: '采购',
    PRODUCTION: '生产',
    SALES: '销售',
    PRODUCTION_USAGE: '生产领用',
    PRODUCTION_LOSS: '生产损耗',
    PACKAGING_LOSS: '包装损耗',
    SHIPPING_LOSS: '运输损耗',
    INVENTORY: '盘点'
  }
  return `${type === 'IN' ? '入库' : type === 'OUT' ? '出库' : '调整'} · ${subTypeMap[subType]}`
}

function exportInventoryWorkbook(items: StockItem[], fileName: string) {
  const rows = items.map(item => ({
    商品编码: item.productCode,
    商品名称: item.productName,
    类型: productTypeLabel(item.productType),
    分类: item.category || '-',
    规格: item.specification || '-',
    单位: item.unit,
    库存数量: item.quantity,
    可用库存: item.availableQuantity,
    锁定库存: item.lockedQuantity,
    预警库存: item.alertQuantity,
    库存状态: stockStatusLabel(item),
    成本价: Number(item.costPrice || 0)
  }))
  const worksheet = XLSX.utils.json_to_sheet(rows)
  worksheet['!cols'] = [
    { wch: 14 },
    { wch: 20 },
    { wch: 10 },
    { wch: 12 },
    { wch: 14 },
    { wch: 8 },
    { wch: 10 },
    { wch: 10 },
    { wch: 10 },
    { wch: 10 },
    { wch: 10 },
    { wch: 10 }
  ]

  const workbook = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(workbook, worksheet, '库存报表')
  XLSX.writeFile(workbook, fileName)
}

function productTypeLabel(type: StockItem['productType']) {
  return type === 'RAW_MATERIAL' ? '原材料' : '成品'
}

function stockStatusLabel(item: StockItem) {
  if (item.quantity === 0) return '缺货'
  if (item.isLowStock) return '预警'
  return '正常'
}

function formatDateForFile(date: Date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function formatDateTime(value: string) {
  return new Intl.DateTimeFormat('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }).format(new Date(value))
}
</script>

<style scoped>
.inventory-page {
  min-height: 100vh;
  padding: 0;
  background: #F1F5F9;
}

.page-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 18px;
  margin-bottom: 16px;
}

.page-eyebrow {
  margin: 0 0 6px;
  color: #475569;
  font-size: 13px;
  font-weight: 700;
}

.page-header h1 {
  margin: 0 0 6px;
  color: #0F172A;
  font-size: 20px;
}

.page-header p {
  margin: 0;
  color: #64748B;
  font-size: 13px;
  line-height: 1.6;
}

.page-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.page-action {
  min-height: 38px;
  padding: 0 14px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
}

.page-action--primary {
  border: 1px solid #2563eb;
  background: #2563eb;
  color: #ffffff;
}

.page-action--primary:hover {
  background: #1d4ed8;
}

.page-action--outline {
  border: 1px solid #cbd5e1;
  background: #ffffff;
  color: #0f172a;
}

.page-action--outline:hover {
  background: #f8fafc;
}

.stock-record-card {
  margin-top: 16px;
  padding: 16px;
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.06);
}

.stock-record-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 14px;
}

.stock-record-header h2 {
  margin: 0;
  color: #0f172a;
  font-size: 16px;
}

.record-refresh {
  min-height: 36px;
  padding: 0 12px;
  border: 1px solid #cbd5e1;
  border-radius: 10px;
  background: #ffffff;
  color: #0f172a;
  font-weight: 600;
  cursor: pointer;
}

.record-refresh:disabled {
  color: #94a3b8;
  cursor: not-allowed;
}

.record-table-wrapper {
  overflow-x: auto;
}

.record-table {
  width: 100%;
  min-width: 980px;
  border-collapse: collapse;
}

.record-table th,
.record-table td {
  padding: 12px;
  border-bottom: 1px solid #e2e8f0;
  color: #0f172a;
  font-size: 13px;
  text-align: left;
}

.record-table th {
  background: #f8fafc;
  color: #334155;
  font-weight: 700;
}

.record-type {
  display: inline-flex;
  align-items: center;
  min-width: 92px;
  justify-content: center;
  padding: 5px 8px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
}

.record-type--in {
  background: #dcfce7;
  color: #166534;
}

.record-type--out {
  background: #fee2e2;
  color: #991b1b;
}

.quantity-in {
  color: #16a34a;
  font-weight: 700;
}

.quantity-out {
  color: #dc2626;
  font-weight: 700;
}
</style>
