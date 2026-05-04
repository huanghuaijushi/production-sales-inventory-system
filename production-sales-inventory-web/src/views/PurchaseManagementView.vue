<template>
  <div class="purchase-page">
    <div class="page-header">
      <div>
        <p class="page-eyebrow">采购管理</p>
        <h1>原材料采购</h1>
        <p>只处理原材料采购；供应商和配方已拆到独立页面，采购页专注下单、到货和建议。</p>
      </div>
      <div class="header-actions">
        <button type="button" class="secondary-button" @click="goSuppliers()">供应商配置</button>
        <button type="button" class="secondary-button" @click="goProductionConfig">生产配置</button>
        <button type="button" class="primary-button" @click="openPurchaseModal()">新增采购单</button>
      </div>
    </div>

    <p v-if="message" class="operation-message">{{ message }}</p>

    <div class="purchase-layout">
      <section class="orders-section">
        <div class="toolbar">
          <input
            v-model="filters.query"
            type="search"
            placeholder="搜索采购单号、供应商或备注"
            @keyup.enter="loadOrders(true)"
          />
          <select v-model="filters.status" @change="loadOrders(true)">
            <option value="all">全部状态</option>
            <option value="DRAFT">草稿</option>
            <option value="PENDING_INBOUND">待入库</option>
            <option value="INBOUNDED">已入库</option>
            <option value="CANCELLED">已取消</option>
          </select>
          <button type="button" class="secondary-button" @click="loadOrders(true)">查询</button>
        </div>

        <div class="order-table-wrap">
          <table class="order-table">
            <thead>
              <tr>
                <th>采购单</th>
                <th>供应商</th>
                <th>明细</th>
                <th>金额</th>
                <th>状态</th>
                <th>预计到货</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="ordersLoading">
                <td colspan="7" class="empty-cell">正在加载采购单...</td>
              </tr>
              <tr v-else-if="orders.length === 0">
                <td colspan="7" class="empty-cell">暂无采购单</td>
              </tr>
              <tr v-for="order in orders" v-else :key="order.id">
                <td>
                  <div class="strong-text">{{ order.orderNo }}</div>
                  <div class="muted-text">{{ formatDateTime(order.createdAt) }}</div>
                </td>
                <td>{{ order.supplierName }}</td>
                <td>
                  <div class="item-summary">
                    <span v-for="item in order.items.slice(0, 2)" :key="item.id">
                      {{ item.productName }} x {{ item.quantity }}{{ item.productUnit }}
                    </span>
                    <span v-if="order.items.length > 2">等 {{ order.items.length }} 项</span>
                  </div>
                </td>
                <td class="money-cell">{{ formatMoney(order.totalAmount) }}</td>
                <td>
                  <span class="status-pill" :class="`status-pill--${order.status.toLowerCase()}`">
                    {{ order.statusText }}
                  </span>
                </td>
                <td>{{ order.expectedArrivalDate || '未填写' }}</td>
                <td>
                  <div class="table-actions">
                    <button v-if="isEditable(order.status)" type="button" class="text-button" @click="openPurchaseModal(order)">
                      编辑
                    </button>
                    <button v-if="order.status === 'PENDING_INBOUND'" type="button" class="text-button primary-text" @click="confirmInbound(order)">
                      确认入库
                    </button>
                    <button v-if="isCancellable(order.status)" type="button" class="text-button danger-text" @click="cancelOrder(order)">
                      取消
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="pagination-bar">
          <span>共 {{ orderPage.totalElements }} 条采购单</span>
          <div class="pagination-actions">
            <button type="button" :disabled="orderPage.number <= 0" @click="changeOrderPage(orderPage.number - 1)">
              上一页
            </button>
            <span>{{ orderPage.number + 1 }} / {{ Math.max(orderPage.totalPages, 1) }}</span>
            <button type="button" :disabled="orderPage.number + 1 >= orderPage.totalPages" @click="changeOrderPage(orderPage.number + 1)">
              下一页
            </button>
          </div>
        </div>
      </section>

      <aside class="side-section">
        <section class="info-panel">
          <div class="panel-header">
            <div>
              <h2>智能采购建议</h2>
              <p>按成品缺口汇总原材料，再按默认供应商分组。</p>
            </div>
            <button type="button" class="secondary-button compact-button" @click="loadPlanningData">刷新</button>
          </div>
          <div v-if="planningLoading" class="empty-box">正在计算采购建议...</div>
          <div v-else-if="sortedPurchaseSuggestionGroups.length === 0" class="empty-box">当前没有需要采购的原材料</div>
          <div v-for="group in sortedPurchaseSuggestionGroups" v-else :key="getPurchaseGroupKey(group)" class="suggestion-group">
            <div class="group-heading">
              <div>
                <strong>{{ group.supplierName }}</strong>
                <span>{{ group.expectedArrivalDate ? `预计 ${group.expectedArrivalDate} 到货` : '需要先绑定供应商' }}</span>
              </div>
              <button v-if="group.supplierId" type="button" class="text-button primary-text" @click="openSmartPurchaseModal(group)">
                生成采购单
              </button>
              <button v-else type="button" class="text-button primary-text" @click="goSuppliers(group.items[0]?.productId)">
                绑定供应商
              </button>
            </div>
            <div v-for="item in getSortedPurchaseSuggestionItems(group)" :key="item.productId" class="mini-row">
              <span>{{ item.productCode }} · {{ item.productName }}</span>
              <em>缺 {{ item.shortageQuantity }}，采 {{ item.suggestedPurchaseQuantity }}{{ item.productUnit }}</em>
              <button v-if="!group.supplierId" type="button" class="text-button primary-text mini-bind-button" @click="goSuppliers(item.productId)">
                绑定
              </button>
            </div>
            <div class="group-total">预计金额 {{ formatMoney(group.totalAmount) }}</div>
          </div>
        </section>

        <section class="info-panel">
          <div class="panel-header">
            <div>
              <h2>生产建议</h2>
              <p>成品低于预警线时，优先看可生产量和原材料缺口。</p>
            </div>
          </div>
          <div v-if="planningLoading" class="empty-box">正在计算生产建议...</div>
          <div v-else-if="productionSuggestions.length === 0" class="empty-box">当前暂无成品生产建议</div>
          <div v-for="item in productionSuggestions" v-else :key="item.finishedProductId" class="production-item">
            <div class="production-main">
              <div>
                <strong>{{ item.finishedProductName }}</strong>
                <span>库存 {{ item.currentStock }}{{ item.finishedProductUnit }} · 预警 {{ item.alertQuantity }}</span>
              </div>
              <em :class="{ 'warning-text': !item.canProduceNow }">
                建议生产 {{ item.suggestedProductionQuantity }}{{ item.finishedProductUnit }}
              </em>
            </div>
            <div class="capacity-line">
              当前最多可生产 {{ item.maxProducibleQuantity }}{{ item.finishedProductUnit }}
            </div>
            <div v-if="item.materialRequirements.length > 0" class="material-mini-list">
              <div v-for="material in item.materialRequirements" :key="material.materialProductId" class="mini-row">
                <span>{{ material.materialProductName }}</span>
                <em>需 {{ material.requiredQuantity }}，缺 {{ material.shortageQuantity }}{{ material.materialProductUnit }}</em>
              </div>
            </div>
          </div>
        </section>
      </aside>
    </div>

    <div v-if="purchaseModalOpen" class="modal-backdrop">
      <div class="modal-content purchase-modal">
        <div class="modal-header">
          <h2>{{ purchaseForm.id ? '编辑采购单' : '新增采购单' }}</h2>
          <button type="button" class="icon-button" @click="closePurchaseModal">×</button>
        </div>

        <div class="modal-body">
          <div class="form-grid">
            <label>
              <span>供应商</span>
              <select v-model.number="purchaseForm.supplierId">
                <option :value="0" disabled>请选择供应商</option>
                <option v-for="supplier in suppliers" :key="supplier.id" :value="supplier.id">
                  {{ supplier.name }}
                </option>
              </select>
            </label>
            <label>
              <span>预计到货日期</span>
              <input v-model="purchaseForm.expectedArrivalDate" type="date" />
            </label>
          </div>

          <label class="full-field">
            <span>备注</span>
            <textarea v-model="purchaseForm.remark" rows="2" placeholder="可填写采购说明、交期要求等"></textarea>
          </label>

          <div class="items-header">
            <h3>采购明细</h3>
            <button type="button" class="secondary-button compact-button" @click="addPurchaseItem()">添加商品</button>
          </div>

          <div class="purchase-items">
            <div v-for="(item, index) in purchaseForm.items" :key="item.uid" class="purchase-item-row">
              <select v-model.number="item.productId" @change="syncItemPrice(item)">
                <option :value="0" disabled>请选择原材料</option>
                <option v-for="product in rawMaterialProducts" :key="product.id" :value="product.id">
                  {{ product.sku }} · {{ product.name }}
                </option>
              </select>
              <input v-model.number="item.quantity" type="number" min="1" placeholder="数量" />
              <input v-model.number="item.unitPrice" type="number" min="0" step="0.01" placeholder="单价" />
              <strong>{{ formatMoney(getItemAmount(item)) }}</strong>
              <button type="button" class="icon-button danger-icon" :disabled="purchaseForm.items.length === 1" @click="removePurchaseItem(index)">
                ×
              </button>
            </div>
          </div>

          <div class="form-total">
            <span>合计</span>
            <strong>{{ formatMoney(formTotal) }}</strong>
          </div>

          <label class="draft-check">
            <input v-model="purchaseForm.draft" type="checkbox" />
            <span>保存为草稿，暂不允许入库</span>
          </label>
        </div>

        <div class="modal-actions">
          <button type="button" class="secondary-button" @click="closePurchaseModal">取消</button>
          <button type="button" class="primary-button" :disabled="purchaseSubmitting" @click="submitPurchaseOrder">
            {{ purchaseSubmitting ? '保存中...' : '保存采购单' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ApiError } from '@/api/http'
import { productApi, type Product } from '@/api/product'
import { purchaseApi, type PurchaseOrder, type PurchaseOrderStatus } from '@/api/purchase'
import {
  productionApi,
  type ProductionSuggestion,
  type PurchaseSuggestionGroup,
  type PurchaseSuggestionItem,
  type SupplierMaterial
} from '@/api/production'
import { supplierApi, type Supplier } from '@/api/supplier'

interface PurchaseFormItem {
  uid: string
  productId: number
  quantity: number
  unitPrice: number
}

interface PurchaseForm {
  id: number | null
  supplierId: number
  expectedArrivalDate: string
  remark: string
  draft: boolean
  items: PurchaseFormItem[]
}

const router = useRouter()
const orders = ref<PurchaseOrder[]>([])
const products = ref<Product[]>([])
const suppliers = ref<Supplier[]>([])
const supplierMaterials = ref<SupplierMaterial[]>([])
const productionSuggestions = ref<ProductionSuggestion[]>([])
const purchaseSuggestionGroups = ref<PurchaseSuggestionGroup[]>([])
const ordersLoading = ref(false)
const planningLoading = ref(false)
const purchaseModalOpen = ref(false)
const purchaseSubmitting = ref(false)
const message = ref('')
const filters = reactive({
  query: '',
  status: 'all'
})
const orderPage = reactive({
  number: 0,
  size: 10,
  totalElements: 0,
  totalPages: 0
})
const purchaseForm = reactive<PurchaseForm>(createEmptyPurchaseForm())
let messageTimer: number | undefined
let orderSearchTimer: number | undefined

const rawMaterialProducts = computed(() => products.value.filter(product => product.type === 'RAW_MATERIAL'))
const formTotal = computed(() => purchaseForm.items.reduce((sum, item) => sum + getItemAmount(item), 0))
const sortedPurchaseSuggestionGroups = computed(() => {
  return [...purchaseSuggestionGroups.value]
    .map(group => ({ ...group, items: getSortedPurchaseSuggestionItems(group) }))
    .sort(comparePurchaseSuggestionGroups)
})

watch(
  () => filters.query,
  () => {
    window.clearTimeout(orderSearchTimer)
    orderSearchTimer = window.setTimeout(() => loadOrders(true), 300)
  }
)

onMounted(() => {
  loadInitialData()
})

async function loadInitialData() {
  await Promise.all([
    loadOrders(true),
    loadSuppliers(),
    loadProducts(),
    loadPlanningData()
  ])
}

async function loadOrders(resetPage = false) {
  if (resetPage) {
    orderPage.number = 0
  }
  ordersLoading.value = true
  try {
    const result = await purchaseApi.getOrders(orderPage.number, orderPage.size, filters)
    orders.value = result.content
    orderPage.number = result.number
    orderPage.size = result.size
    orderPage.totalElements = result.totalElements
    orderPage.totalPages = result.totalPages
  } catch (error) {
    showMessage(getErrorMessage(error, '加载采购单失败'))
  } finally {
    ordersLoading.value = false
  }
}

async function loadSuppliers() {
  try {
    const result = await supplierApi.getSuppliers(0, 200)
    suppliers.value = result.content
  } catch (error) {
    showMessage(getErrorMessage(error, '加载供应商失败'))
  }
}

async function loadProducts() {
  try {
    const result = await productApi.getAllProducts(0, 300)
    products.value = result.content
  } catch (error) {
    showMessage(getErrorMessage(error, '加载商品失败'))
  }
}

async function loadPlanningData() {
  planningLoading.value = true
  try {
    const [supplierMaterialResult, productionResult, purchaseResult] = await Promise.all([
      productionApi.getSupplierMaterials(),
      productionApi.getSuggestions(),
      productionApi.getPurchaseSuggestions()
    ])
    supplierMaterials.value = supplierMaterialResult
    productionSuggestions.value = productionResult
    purchaseSuggestionGroups.value = purchaseResult
  } catch (error) {
    showMessage(getErrorMessage(error, '加载生产采购建议失败'))
  } finally {
    planningLoading.value = false
  }
}

function changeOrderPage(page: number) {
  orderPage.number = page
  loadOrders()
}

function openPurchaseModal(order?: PurchaseOrder) {
  resetPurchaseForm()
  if (order) {
    purchaseForm.id = order.id
    purchaseForm.supplierId = order.supplierId
    purchaseForm.expectedArrivalDate = order.expectedArrivalDate ?? ''
    purchaseForm.remark = order.remark ?? ''
    purchaseForm.draft = order.status === 'DRAFT'
    purchaseForm.items = order.items.map(item => ({
      uid: createUid(),
      productId: item.productId,
      quantity: item.quantity,
      unitPrice: Number(item.unitPrice)
    }))
  }
  purchaseModalOpen.value = true
}

function openSmartPurchaseModal(group: PurchaseSuggestionGroup) {
  if (!group.supplierId) {
    goSuppliers(group.items[0]?.productId)
    return
  }
  resetPurchaseForm()
  purchaseForm.supplierId = group.supplierId
  purchaseForm.expectedArrivalDate = group.expectedArrivalDate ?? ''
  purchaseForm.remark = '根据生产计划缺口自动生成采购单。'
  purchaseForm.items = group.items.map(item => ({
    uid: createUid(),
    productId: item.productId,
    quantity: item.suggestedPurchaseQuantity,
    unitPrice: Number(item.defaultUnitPrice ?? 0)
  }))
  purchaseModalOpen.value = true
}

function closePurchaseModal() {
  purchaseModalOpen.value = false
}

function addPurchaseItem(item?: Partial<PurchaseFormItem>) {
  purchaseForm.items.push({
    uid: createUid(),
    productId: item?.productId ?? 0,
    quantity: item?.quantity ?? 1,
    unitPrice: item?.unitPrice ?? 0
  })
}

function removePurchaseItem(index: number) {
  if (purchaseForm.items.length <= 1) {
    return
  }
  purchaseForm.items.splice(index, 1)
}

function syncItemPrice(item: PurchaseFormItem) {
  const supplierRule = supplierMaterials.value.find(rule =>
    rule.supplierId === purchaseForm.supplierId && rule.productId === item.productId
  )
  if (supplierRule) {
    item.unitPrice = Number(supplierRule.defaultUnitPrice)
    return
  }
  const product = rawMaterialProducts.value.find(candidate => candidate.id === item.productId)
  item.unitPrice = Number(product?.costPrice ?? 0)
}

async function submitPurchaseOrder() {
  const validationMessage = validatePurchaseForm()
  if (validationMessage) {
    showMessage(validationMessage)
    return
  }

  purchaseSubmitting.value = true
  try {
    const payload = {
      supplierId: purchaseForm.supplierId,
      draft: purchaseForm.draft,
      expectedArrivalDate: purchaseForm.expectedArrivalDate || undefined,
      remark: purchaseForm.remark.trim() || undefined,
      items: purchaseForm.items.map(item => ({
        productId: item.productId,
        quantity: Number(item.quantity),
        unitPrice: Number(item.unitPrice)
      }))
    }

    if (purchaseForm.id) {
      await purchaseApi.updateOrder(purchaseForm.id, payload)
      showMessage('采购单已更新。')
    } else {
      await purchaseApi.createOrder(payload)
      showMessage('采购单已创建。')
    }
    closePurchaseModal()
    await Promise.all([loadOrders(true), loadPlanningData()])
  } catch (error) {
    showMessage(getErrorMessage(error, '保存采购单失败'))
  } finally {
    purchaseSubmitting.value = false
  }
}

async function confirmInbound(order: PurchaseOrder) {
  const confirmed = window.confirm(`确认采购单 ${order.orderNo} 已到货并入库吗？`)
  if (!confirmed) return

  try {
    await purchaseApi.inbound(order.id)
    showMessage('采购入库成功，库存已更新。')
    await Promise.all([loadOrders(), loadPlanningData()])
  } catch (error) {
    showMessage(getErrorMessage(error, '采购入库失败'))
  }
}

async function cancelOrder(order: PurchaseOrder) {
  const confirmed = window.confirm(`确认取消采购单 ${order.orderNo} 吗？`)
  if (!confirmed) return

  try {
    await purchaseApi.cancel(order.id)
    showMessage('采购单已取消。')
    await Promise.all([loadOrders(), loadPlanningData()])
  } catch (error) {
    showMessage(getErrorMessage(error, '取消采购单失败'))
  }
}

function validatePurchaseForm() {
  if (purchaseForm.supplierId <= 0) return '请选择供应商。'
  if (purchaseForm.items.length === 0) return '请添加采购商品。'

  const productIds = new Set<number>()
  for (const item of purchaseForm.items) {
    if (item.productId <= 0) return '请选择采购商品。'
    const product = rawMaterialProducts.value.find(candidate => candidate.id === item.productId)
    if (!product) return '采购单只能选择原材料，成品粽子请走生产补货。'
    if (productIds.has(item.productId)) return '同一采购单不能重复添加同一个商品。'
    productIds.add(item.productId)
    if (!Number.isFinite(item.quantity) || item.quantity <= 0) return '采购数量必须大于 0。'
    if (!Number.isFinite(item.unitPrice) || item.unitPrice < 0) return '采购单价不能小于 0。'
  }
  return ''
}

function goSuppliers(productId?: number) {
  if (productId) {
    router.push({ name: 'suppliers', query: { productId: String(productId) } })
    return
  }
  router.push({ name: 'suppliers' })
}

function goProductionConfig() {
  router.push({ name: 'production-config' })
}

function isEditable(status: PurchaseOrderStatus) {
  return status === 'DRAFT' || status === 'PENDING_INBOUND'
}

function isCancellable(status: PurchaseOrderStatus) {
  return status === 'DRAFT' || status === 'PENDING_INBOUND'
}

function getItemAmount(item: PurchaseFormItem) {
  return Number(item.quantity || 0) * Number(item.unitPrice || 0)
}

function getPurchaseGroupKey(group: PurchaseSuggestionGroup) {
  return group.supplierId ? `supplier-${group.supplierId}` : 'unbound'
}

function getSortedPurchaseSuggestionItems(group: PurchaseSuggestionGroup) {
  return [...group.items].sort(comparePurchaseSuggestionItems)
}

function comparePurchaseSuggestionGroups(first: PurchaseSuggestionGroup, second: PurchaseSuggestionGroup) {
  const firstUnboundRank = first.supplierId ? 1 : 0
  const secondUnboundRank = second.supplierId ? 1 : 0
  if (firstUnboundRank !== secondUnboundRank) return firstUnboundRank - secondUnboundRank

  const dateCompare = compareOptionalDate(first.expectedArrivalDate, second.expectedArrivalDate)
  if (dateCompare !== 0) return dateCompare

  const amountCompare = Number(second.totalAmount || 0) - Number(first.totalAmount || 0)
  if (amountCompare !== 0) return amountCompare

  return first.supplierName.localeCompare(second.supplierName, 'zh-CN')
}

function comparePurchaseSuggestionItems(first: PurchaseSuggestionItem, second: PurchaseSuggestionItem) {
  const codeCompare = first.productCode.localeCompare(second.productCode, 'zh-CN')
  if (codeCompare !== 0) return codeCompare
  return first.productName.localeCompare(second.productName, 'zh-CN')
}

function compareOptionalDate(first?: string, second?: string) {
  if (!first && !second) return 0
  if (!first) return 1
  if (!second) return -1
  return first.localeCompare(second)
}

function formatMoney(value: number) {
  return new Intl.NumberFormat('zh-CN', {
    style: 'currency',
    currency: 'CNY'
  }).format(Number(value || 0))
}

function formatDateTime(value: string) {
  return new Intl.DateTimeFormat('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }).format(new Date(value))
}

function getErrorMessage(error: unknown, fallback: string) {
  if (error instanceof ApiError) {
    return error.message || fallback
  }
  return fallback
}

function showMessage(value: string) {
  message.value = value
  window.clearTimeout(messageTimer)
  messageTimer = window.setTimeout(() => {
    message.value = ''
  }, 2800)
}

function resetPurchaseForm() {
  Object.assign(purchaseForm, createEmptyPurchaseForm())
}

function createEmptyPurchaseForm(): PurchaseForm {
  return {
    id: null,
    supplierId: 0,
    expectedArrivalDate: '',
    remark: '',
    draft: false,
    items: [
      {
        uid: createUid(),
        productId: 0,
        quantity: 1,
        unitPrice: 0
      }
    ]
  }
}

function createUid() {
  return `${Date.now()}-${Math.random().toString(16).slice(2)}`
}
</script>

<style scoped>
.purchase-page {
  width: 100%;
  overflow-wrap: anywhere;
}

.purchase-page * {
  box-sizing: border-box;
}

.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 24px;
  margin-bottom: 20px;
}

.page-eyebrow {
  margin: 0 0 6px;
  color: #2563eb;
  font-size: 13px;
  font-weight: 700;
}

.page-header h1 {
  margin: 0;
  color: #0f172a;
  font-size: 28px;
  line-height: 1.2;
}

.page-header p:last-child,
.panel-header p {
  margin: 8px 0 0;
  color: #64748b;
}

.header-actions,
.table-actions,
.pagination-actions,
.modal-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.primary-button,
.secondary-button {
  border-radius: 8px;
  padding: 10px 16px;
  font-weight: 700;
  cursor: pointer;
}

.primary-button {
  border: 0;
  background: #2563eb;
  color: #ffffff;
}

.secondary-button {
  border: 1px solid #dbe3ef;
  background: #ffffff;
  color: #334155;
}

.compact-button {
  padding: 7px 10px;
  font-size: 12px;
}

.primary-button:disabled,
.secondary-button:disabled,
.icon-button:disabled,
.text-button:disabled {
  cursor: not-allowed;
  opacity: 0.55;
}

.operation-message {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 80;
  margin: 0;
  border: 1px solid #bfdbfe;
  border-radius: 10px;
  padding: 12px 16px;
  background: #eff6ff;
  color: #1d4ed8;
  box-shadow: 0 16px 36px rgba(37, 99, 235, 0.14);
}

.purchase-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 390px;
  gap: 20px;
}

.orders-section,
.info-panel {
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  background: #ffffff;
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.06);
}

.orders-section {
  overflow: hidden;
}

.toolbar {
  display: grid;
  grid-template-columns: minmax(260px, 1fr) 160px auto;
  gap: 12px;
  padding: 16px;
  border-bottom: 1px solid #e2e8f0;
}

input,
select,
textarea {
  width: 100%;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  padding: 10px 12px;
  color: #0f172a;
  font: inherit;
  outline: none;
}

textarea {
  resize: vertical;
}

input:focus,
select:focus,
textarea:focus {
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.12);
}

.order-table-wrap {
  overflow-x: auto;
}

.order-table {
  width: 100%;
  min-width: 880px;
  border-collapse: collapse;
}

.order-table th,
.order-table td {
  padding: 14px 16px;
  border-bottom: 1px solid #edf2f7;
  text-align: left;
  vertical-align: top;
}

.order-table th {
  color: #475569;
  background: #f8fafc;
  font-size: 12px;
  font-weight: 700;
}

.strong-text,
.money-cell {
  color: #0f172a;
  font-weight: 700;
}

.muted-text {
  margin-top: 4px;
  color: #94a3b8;
  font-size: 12px;
}

.item-summary {
  display: flex;
  flex-direction: column;
  gap: 4px;
  color: #475569;
  font-size: 13px;
}

.status-pill {
  display: inline-flex;
  align-items: center;
  border-radius: 999px;
  padding: 4px 10px;
  background: #f1f5f9;
  color: #475569;
  font-size: 12px;
  font-weight: 700;
}

.status-pill--pending_inbound {
  background: #eff6ff;
  color: #1d4ed8;
}

.status-pill--inbounded {
  background: #ecfdf5;
  color: #047857;
}

.status-pill--cancelled {
  background: #fef2f2;
  color: #b91c1c;
}

.text-button {
  border: 0;
  padding: 0;
  background: transparent;
  color: #475569;
  font-weight: 700;
  cursor: pointer;
}

.primary-text {
  color: #2563eb;
}

.danger-text,
.warning-text {
  color: #dc2626;
}

.empty-cell,
.empty-box {
  color: #94a3b8;
  text-align: center;
}

.empty-box {
  padding: 18px 12px;
}

.pagination-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  color: #64748b;
  font-size: 13px;
}

.pagination-actions button {
  border: 1px solid #dbe3ef;
  border-radius: 6px;
  padding: 6px 10px;
  background: #ffffff;
  cursor: pointer;
}

.side-section {
  display: grid;
  gap: 20px;
}

.info-panel {
  padding: 16px;
}

.panel-header,
.group-heading,
.production-main,
.mini-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  min-width: 0;
}

.panel-header {
  margin-bottom: 14px;
}

.panel-header h2 {
  margin: 0;
  color: #0f172a;
  font-size: 16px;
}

.suggestion-group,
.production-item {
  border-top: 1px solid #edf2f7;
  padding: 12px 0;
}

.suggestion-group:first-of-type,
.production-item:first-of-type {
  border-top: 0;
}

.group-heading strong,
.production-main strong {
  display: block;
  color: #0f172a;
  font-size: 13px;
}

.group-heading span,
.production-main span,
.capacity-line,
.mini-row {
  color: #64748b;
  font-size: 12px;
}

.production-main em,
.mini-row em,
.group-total {
  color: #2563eb;
  font-size: 12px;
  font-style: normal;
  font-weight: 700;
}

.capacity-line,
.group-total {
  margin-top: 8px;
}

.group-total {
  text-align: right;
}

.material-mini-list {
  margin-top: 8px;
}

.mini-row {
  align-items: center;
  padding-top: 6px;
}

.mini-row span {
  min-width: 0;
  flex: 1;
}

.mini-row em {
  min-width: 0;
  text-align: right;
}

.mini-bind-button {
  flex-shrink: 0;
  font-size: 12px;
}

.modal-backdrop {
  position: fixed;
  inset: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: rgba(15, 23, 42, 0.48);
}

.modal-content {
  display: flex;
  flex-direction: column;
  width: min(920px, 100%);
  max-height: 90vh;
  border-radius: 16px;
  background: #ffffff;
  box-shadow: 0 24px 60px rgba(15, 23, 42, 0.22);
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 22px 24px;
  border-bottom: 1px solid #e2e8f0;
}

.modal-header h2 {
  margin: 0;
  color: #0f172a;
  font-size: 22px;
}

.icon-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border: 0;
  border-radius: 8px;
  background: #f1f5f9;
  color: #334155;
  font-size: 22px;
  cursor: pointer;
}

.danger-icon {
  color: #dc2626;
}

.modal-body {
  overflow-y: auto;
  padding: 22px 24px;
}

.modal-actions {
  justify-content: flex-end;
  padding: 16px 24px;
  border-top: 1px solid #e2e8f0;
}

.form-grid,
.purchase-item-row {
  display: grid;
  gap: 12px;
}

.form-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

label span,
.items-header h3 {
  display: block;
  margin-bottom: 8px;
  color: #334155;
  font-size: 13px;
  font-weight: 700;
}

.full-field {
  display: block;
  margin-top: 16px;
}

.items-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 20px;
}

.items-header h3 {
  margin: 0;
  font-size: 15px;
}

.purchase-items {
  display: grid;
  gap: 10px;
}

.purchase-item-row {
  grid-template-columns: minmax(220px, 1fr) 110px 120px 110px 40px;
  align-items: center;
}

.purchase-item-row strong {
  color: #0f172a;
  text-align: right;
}

.form-total {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
  margin-top: 16px;
  color: #334155;
}

.form-total strong {
  color: #0f172a;
  font-size: 18px;
}

.draft-check {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  margin-top: 16px;
  color: #475569;
  font-size: 13px;
}

.draft-check input {
  width: 16px;
  height: 16px;
}

@media (max-width: 1280px) {
  .purchase-layout {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 860px) {
  .page-header,
  .pagination-bar,
  .header-actions,
  .panel-header,
  .group-heading,
  .production-main {
    flex-direction: column;
    align-items: stretch;
  }

  .toolbar,
  .form-grid,
  .purchase-item-row {
    grid-template-columns: 1fr;
  }

  .purchase-item-row strong,
  .mini-row em,
  .group-total {
    text-align: left;
  }
}
</style>
