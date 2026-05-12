<template>
  <div class="page">
    <header class="page-header">
      <div>
        <p class="page-eyebrow">生产计划</p>
        <h1>生产工单与批次进度</h1>
        <p>从计划、领料、工序损耗到成品入库串成一条线，库存流水由工单自动生成。</p>
      </div>
    </header>

    <p v-if="message" class="operation-message">{{ message }}</p>

    <div class="production-layout">
      <aside class="left-column">
        <section class="panel">
          <div class="panel-header">
            <div>
              <h2>新建工单</h2>
              <p>选择成品后，会按生产配置里的配方生成用料计划。</p>
            </div>
          </div>
          <div class="form-grid form-grid--single">
            <label>
              <span>成品</span>
              <select v-model.number="createForm.productId">
                <option :value="0" disabled>选择成品</option>
                <option v-for="product in finishedProducts" :key="product.id" :value="product.id">
                  {{ product.sku }} · {{ product.name }}
                </option>
              </select>
              <small v-if="createForm.productId > 0 && !selectedProductHasBom" class="field-warning">
                该成品还没有配置成品配方，请先到“生产配置”维护配方。
              </small>
            </label>
            <label>
              <span>计划数量</span>
              <input v-model.number="createForm.plannedQuantity" type="number" min="1" step="1" />
            </label>
            <label>
              <span>生产批次</span>
              <input v-model="createForm.batchNo" type="text" placeholder="不填则自动生成" />
            </label>
            <label>
              <span>计划日期</span>
              <input v-model="createForm.plannedDate" type="date" />
            </label>
            <label>
              <span>备注</span>
              <textarea v-model="createForm.remark" rows="3" placeholder="可填写生产说明"></textarea>
            </label>
          </div>
          <button type="button" class="primary-button full-button" :disabled="creating" @click="createOrder">
            {{ creating ? '创建中...' : '创建生产工单' }}
          </button>
        </section>

        <section class="panel order-list-panel">
          <div class="panel-header">
            <div>
              <h2>工单列表</h2>
              <p>共 {{ orders.length }} 张工单。</p>
            </div>
            <button type="button" class="secondary-button" :disabled="loading" @click="loadOrders">刷新</button>
          </div>
          <div v-if="loading" class="empty-box">正在加载生产工单...</div>
          <div v-else-if="orders.length === 0" class="empty-box">暂无生产工单。</div>
          <button
            v-for="order in orders"
            v-else
            :key="order.id"
            type="button"
            class="order-card"
            :class="{ active: detail?.order.id === order.id }"
            @click="selectOrder(order.id)"
          >
            <div>
              <strong>{{ order.productName }}</strong>
              <span>{{ order.orderNo }}</span>
            </div>
            <span class="status-badge" :class="`status-badge--${order.status.toLowerCase()}`">
              {{ statusLabel(order.status) }}
            </span>
            <div class="order-meta">
              <span>批次 {{ order.batchNo }}</span>
              <span>{{ order.inboundQuantity }}/{{ order.plannedQuantity }}{{ order.productUnit }}</span>
            </div>
          </button>
        </section>
      </aside>

      <main class="right-column">
        <section v-if="!detail" class="panel empty-detail">
          <h2>请选择或创建一张生产工单</h2>
          <p>工单详情会显示用料批次、生产进度、过程损耗和最终入库。</p>
        </section>

        <template v-else>
          <section class="panel summary-panel">
            <div class="summary-head">
              <div>
                <p class="page-eyebrow">当前工单</p>
                <h2>{{ detail.order.productName }}</h2>
                <p>{{ detail.order.orderNo }} · 批次 {{ detail.order.batchNo }}</p>
              </div>
              <div class="summary-actions">
                <button
                  type="button"
                  class="secondary-button"
                  :disabled="!canOperate"
                  @click="startOrder"
                >
                  开始生产
                </button>
                <button
                  type="button"
                  class="secondary-button danger-outline"
                  :disabled="!canCancel"
                  @click="cancelOrder"
                >
                  取消工单
                </button>
              </div>
            </div>
            <div class="metric-grid">
              <div class="metric-card">
                <span>计划数量</span>
                <strong>{{ detail.order.plannedQuantity }}{{ detail.order.productUnit }}</strong>
              </div>
              <div class="metric-card">
                <span>已完成</span>
                <strong>{{ detail.order.completedQuantity }}{{ detail.order.productUnit }}</strong>
              </div>
              <div class="metric-card">
                <span>已入库</span>
                <strong>{{ detail.order.inboundQuantity }}{{ detail.order.productUnit }}</strong>
              </div>
              <div class="metric-card">
                <span>过程损耗</span>
                <strong>{{ detail.order.lossQuantity }}{{ detail.order.productUnit }}</strong>
              </div>
              <div class="metric-card">
                <span>当前工序</span>
                <strong>{{ detail.order.currentStep ? stepLabel(detail.order.currentStep) : '-' }}</strong>
              </div>
              <div class="metric-card">
                <span>状态</span>
                <strong>{{ statusLabel(detail.order.status) }}</strong>
              </div>
            </div>
          </section>

          <section class="panel">
            <div class="panel-header">
              <div>
                <h2>原料批次领用</h2>
                <p>领料会自动扣减原料批次库存，并生成库存流水“生产领用”。</p>
              </div>
              <button type="button" class="secondary-button" :disabled="batchesLoading" @click="refreshMaterialBatches">
                {{ batchesLoading ? '刷新中...' : '刷新批次' }}
              </button>
            </div>
            <div class="table-wrapper">
              <table class="data-table">
                <thead>
                  <tr>
                    <th>原材料</th>
                    <th>计划 / 已领</th>
                    <th>批次</th>
                    <th>本次领料</th>
                    <th>备注</th>
                    <th>操作</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="plan in detail.materialPlans" :key="plan.id">
                    <td>
                      <strong>{{ plan.materialProductName }}</strong>
                      <span>{{ plan.materialProductCode }}</span>
                    </td>
                    <td>{{ plan.requiredQuantity }} / {{ plan.issuedQuantity }}{{ plan.materialProductUnit }}</td>
                    <td>
                      <select v-model.number="issueForms[plan.id]!.batchId">
                        <option :value="0" disabled>{{ batchOptions(plan).length ? '选择批次' : '暂无可用批次' }}</option>
                        <option
                          v-for="batch in batchOptions(plan)"
                          :key="batch.id"
                          :value="batch.id"
                        >
                          {{ batch.batchNo }} · 可用 {{ batch.availableQuantity }}{{ batch.productUnit }}
                        </option>
                      </select>
                      <small v-if="batchOptions(plan).length === 0" class="field-warning">
                        该原料没有可用批次，请先采购入库或手动入库。
                      </small>
                    </td>
                    <td>
                      <input
                        v-model.number="issueForms[plan.id]!.quantity"
                        type="number"
                        min="1"
                        :max="remainingQuantity(plan)"
                      />
                    </td>
                    <td>
                      <input v-model="issueForms[plan.id]!.remark" type="text" placeholder="领料说明" />
                    </td>
                    <td>
                      <button
                        type="button"
                        class="text-button"
                        :disabled="!canOperate || remainingQuantity(plan) <= 0 || batchOptions(plan).length === 0"
                        @click="issueMaterial(plan)"
                      >
                        领料
                      </button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </section>

          <section class="panel">
              <div class="panel-header">
                <div>
                  <h2>{{ isCompleted ? '生产已完成' : canInbound ? '成品入库' : '记录工序与损耗' }}</h2>
                  <p>{{ isCompleted ? '该工单已完成入库，工序记录和领料记录可在下方查看。' : canInbound ? '所有工序已完成，填写最终合格入库数量并生成成品批次。' : '按工序路线一步一步推进，当前节点会自动高亮。' }}</p>
                </div>
                <RouterLink class="secondary-button config-link" to="/production-config#route-config">
                  编辑工序路线
                </RouterLink>
              </div>
              <div class="route-timeline">
                <div
                  v-for="step in routeSteps"
                  :key="step.stepCode"
                  class="route-step"
                  :class="`route-step--${routeStepState(step.stepCode)}`"
                >
                  <span class="route-node">{{ routeStepIndex(step.stepCode) + 1 }}</span>
                  <div>
                    <strong>{{ step.stepName }}</strong>
                    <div class="route-step-metrics">
                      <span>合格 {{ step.completedQuantity || 0 }}{{ detail.order.productUnit }}</span>
                      <span v-if="step.allowLoss">损耗 {{ step.lossQuantity || 0 }}</span>
                    </div>
                  </div>
                </div>
              </div>
              <div class="step-progress-panel">
                <div>
                  <span>当前工序</span>
                  <strong>{{ isCompleted ? '已完成' : currentStepDisplay ? stepLabel(currentStepDisplay) : '工序已完成' }}</strong>
                </div>
                <div>
                  <span>本步可处理</span>
                  <strong>{{ currentStepInputQuantity }}{{ detail.order.productUnit }}</strong>
                </div>
                <div>
                  <span>本步合格</span>
                  <strong>{{ currentStepQualifiedQuantity }}{{ detail.order.productUnit }}</strong>
                </div>
              </div>
              <div v-if="isCompleted" class="completion-summary">
                <strong>已入库 {{ detail.order.inboundQuantity }}{{ detail.order.productUnit }}</strong>
                <span>计划 {{ detail.order.plannedQuantity }}{{ detail.order.productUnit }}，合格 {{ detail.order.completedQuantity }}{{ detail.order.productUnit }}，损耗 {{ detail.order.lossQuantity }}{{ detail.order.productUnit }}</span>
              </div>
              <template v-else-if="!canInbound">
                <div class="form-grid">
                  <label>
                    <span>本次损耗</span>
                    <input v-model.number="stepForm.lossQuantity" type="number" min="0" step="1" :disabled="!currentRouteStep?.allowLoss" />
                  </label>
                  <label>
                    <span>损耗原因</span>
                    <input v-model="stepForm.lossReason" type="text" placeholder="如破损、封口失败、胀包" />
                  </label>
                </div>
                <button type="button" class="primary-button full-button" :disabled="!canRecordStep" @click="recordStep">
                  保存并进入下一步
                </button>
              </template>
              <template v-else>
                <div class="form-grid">
                  <label>
                    <span>入库数量</span>
                    <input v-model.number="inboundForm.quantity" type="number" min="1" step="1" />
                  </label>
                  <label>
                    <span>生产日期</span>
                    <input v-model="inboundForm.productionDate" type="date" />
                  </label>
                  <label>
                    <span>到期日期</span>
                    <input v-model="inboundForm.expiryDate" type="date" />
                  </label>
                  <label>
                    <span>备注</span>
                    <input v-model="inboundForm.remark" type="text" placeholder="入库说明" />
                  </label>
                </div>
                <button type="button" class="primary-button full-button" :disabled="!canInbound" @click="inboundProduction">
                  完成入库
                </button>
              </template>
          </section>

          <div class="detail-grid">
            <section class="panel">
              <div class="panel-header">
                <div>
                  <h2>工序记录</h2>
                  <p>共 {{ detail.stepRecords.length }} 条记录。</p>
                </div>
              </div>
              <div v-if="detail.stepRecords.length === 0" class="empty-box">暂无工序记录。</div>
              <div v-for="record in detail.stepRecords" v-else :key="record.id" class="history-row">
                <div>
                  <strong>{{ record.stepName || stepLabel(record.stepType) }}</strong>
                  <span>{{ formatDateTime(record.createdAt) }} · {{ record.operatorName }}</span>
                </div>
                <div>
                  <b>完成 {{ record.completedQuantity }}</b>
                  <b class="danger-text">损耗 {{ record.lossQuantity }}</b>
                </div>
                <p v-if="record.lossReason">{{ record.lossReason }}</p>
              </div>
            </section>

            <section class="panel">
              <div class="panel-header">
                <div>
                  <h2>领料记录</h2>
                  <p>共 {{ detail.materialIssues.length }} 条记录。</p>
                </div>
              </div>
              <div v-if="detail.materialIssues.length === 0" class="empty-box">暂无领料记录。</div>
              <div v-for="issue in detail.materialIssues" v-else :key="issue.id" class="history-row">
                <div>
                  <strong>{{ issue.materialProductName }}</strong>
                  <span>{{ issue.batchNo }} · {{ formatDateTime(issue.createdAt) }}</span>
                </div>
                <div>
                  <b>{{ issue.issuedQuantity }}{{ issue.materialProductUnit }}</b>
                </div>
                <p v-if="issue.remark">{{ issue.remark }}</p>
              </div>
            </section>
          </div>

        </template>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ApiError } from '@/api/http'
import { inventoryApi, type StockBatch } from '@/api/inventory'
import { productApi, type Product } from '@/api/product'
import {
  productionApi,
  type BomItem,
  type ProductionMaterialPlan,
  type ProductionOrderDetail,
  type ProductionOrderStatus,
  type ProductionOrderSummary,
  type ProductionStepType
} from '@/api/production'

const orders = ref<ProductionOrderSummary[]>([])
const detail = ref<ProductionOrderDetail | null>(null)
const products = ref<Product[]>([])
const bomItems = ref<BomItem[]>([])
const batchesByProductId = ref<Record<number, StockBatch[]>>({})
const loading = ref(false)
const batchesLoading = ref(false)
const creating = ref(false)
const message = ref('')
let messageTimer: number | undefined

const createForm = reactive({
  productId: 0,
  plannedQuantity: 100,
  batchNo: '',
  plannedDate: '',
  remark: ''
})

const issueForms = reactive<Record<number, { batchId: number; quantity: number; remark: string }>>({})

const stepForm = reactive({
  lossQuantity: 0,
  lossReason: ''
})

const inboundForm = reactive({
  quantity: 0,
  productionDate: '',
  expiryDate: '',
  remark: ''
})

const finishedProducts = computed(() => products.value.filter(product => product.type === 'FINISHED_PRODUCT'))
const bomFinishedProductIds = computed(() => new Set(bomItems.value.map(item => item.finishedProductId)))
const selectedProductHasBom = computed(() => {
  return createForm.productId > 0 && bomFinishedProductIds.value.has(createForm.productId)
})
const canOperate = computed(() => {
  const status = detail.value?.order.status
  return status === 'PLANNED' || status === 'IN_PROGRESS' || status === 'WAIT_INBOUND'
})
const canCancel = computed(() => {
  const status = detail.value?.order.status
  return status === 'PLANNED' || status === 'IN_PROGRESS' || status === 'WAIT_INBOUND'
})
const currentStepDisplay = computed<ProductionStepType | null>(() => {
  const order = detail.value?.order
  if (!order) return null
  if (order.status === 'COMPLETED' || order.status === 'CANCELLED') return null
  if (order.currentStep) return order.currentStep
  return order.status === 'PLANNED' ? routeSteps.value[0]?.stepCode ?? 'PREPARATION' : null
})
const routeSteps = computed(() => detail.value?.routeSteps?.length ? detail.value.routeSteps : defaultRouteSteps())
const currentRouteStep = computed(() => {
  const step = currentStepDisplay.value
  return step ? routeSteps.value.find(item => item.stepCode === step) : null
})
const activeRouteStepIndex = computed(() => {
  const steps = routeSteps.value
  if (steps.length === 0) return -1
  const status = detail.value?.order.status
  if (status === 'WAIT_INBOUND' || status === 'COMPLETED') return steps.length
  const currentStep = currentStepDisplay.value
  if (!currentStep) return 0
  const currentIndex = routeStepIndex(currentStep)
  return currentIndex >= 0 ? currentIndex : 0
})
const currentStepInputQuantity = computed(() => {
  const order = detail.value?.order
  const step = currentStepDisplay.value
  if (!order || !step) return 0
  return routeStepIndex(step) === 0 ? order.plannedQuantity : order.completedQuantity
})
const currentStepQualifiedQuantity = computed(() => {
  const lossQuantity = Number.isFinite(stepForm.lossQuantity) ? Number(stepForm.lossQuantity) : 0
  return Math.max(currentStepInputQuantity.value - lossQuantity, 0)
})
const canRecordStep = computed(() => {
  return canOperate.value && currentStepDisplay.value !== null && detail.value?.order.status !== 'WAIT_INBOUND'
})
const canInbound = computed(() => {
  return detail.value?.order.status === 'WAIT_INBOUND'
})
const isCompleted = computed(() => {
  return detail.value?.order.status === 'COMPLETED'
})

onMounted(() => {
  loadInitialData()
})

async function loadInitialData() {
  await Promise.all([loadProducts(), loadBomItems(), loadOrders()])
}

async function loadProducts() {
  try {
    const result = await productApi.getAllProducts(0, 300)
    products.value = result.content
  } catch (error) {
    showMessage(getErrorMessage(error, '加载成品失败'))
  }
}

async function loadOrders() {
  loading.value = true
  try {
    orders.value = await productionApi.getOrders()
    const firstOrder = orders.value[0]
    if (!detail.value && firstOrder) {
      await selectOrder(firstOrder.id)
    }
  } catch (error) {
    showMessage(getErrorMessage(error, '加载生产工单失败'))
  } finally {
    loading.value = false
  }
}

async function loadBomItems() {
  try {
    bomItems.value = await productionApi.getBomItems()
  } catch (error) {
    showMessage(getErrorMessage(error, '加载成品配方失败'))
  }
}

async function selectOrder(orderId: number) {
  try {
    detail.value = await productionApi.getOrder(orderId)
    initializeForms()
    await loadMaterialBatches()
  } catch (error) {
    showMessage(getErrorMessage(error, '加载工单详情失败'))
  }
}

async function createOrder() {
  if (createForm.productId <= 0 || createForm.plannedQuantity <= 0) {
    showMessage('请选择成品并填写计划数量。')
    return
  }
  if (!selectedProductHasBom.value) {
    showMessage('该成品还没有配置成品配方，请先到“生产配置”维护配方。')
    return
  }

  creating.value = true
  try {
    const payload = {
      productId: createForm.productId,
      plannedQuantity: Number(createForm.plannedQuantity),
      batchNo: createForm.batchNo.trim() || undefined,
      plannedDate: createForm.plannedDate || undefined,
      remark: createForm.remark.trim() || undefined
    }
    detail.value = await productionApi.createOrder(payload)
    resetCreateForm()
    initializeForms()
    await Promise.all([loadOrders(), loadMaterialBatches()])
    showMessage('生产工单已创建。')
  } catch (error) {
    showMessage(getErrorMessage(error, '创建生产工单失败'))
  } finally {
    creating.value = false
  }
}

async function startOrder() {
  if (!detail.value) return
  try {
    detail.value = await productionApi.startOrder(detail.value.order.id)
    initializeForms()
    await loadOrders()
    showMessage('生产已开始。')
  } catch (error) {
    showMessage(getErrorMessage(error, '开始生产失败'))
  }
}

async function cancelOrder() {
  if (!detail.value) return
  const confirmed = window.confirm(`确定取消工单「${detail.value.order.orderNo}」吗？`)
  if (!confirmed) return
  try {
    detail.value = await productionApi.cancelOrder(detail.value.order.id)
    await loadOrders()
    showMessage('工单已取消。')
  } catch (error) {
    showMessage(getErrorMessage(error, '取消工单失败'))
  }
}

async function issueMaterial(plan: ProductionMaterialPlan) {
  if (!detail.value) return
  const form = issueForms[plan.id]
  if (!form || form.batchId <= 0 || form.quantity <= 0) {
    showMessage('请选择批次并填写领料数量。')
    return
  }

  try {
    detail.value = await productionApi.issueMaterial(detail.value.order.id, {
      materialPlanId: plan.id,
      batchId: form.batchId,
      quantity: Number(form.quantity),
      remark: form.remark.trim() || undefined
    })
    initializeForms()
    await Promise.all([loadOrders(), loadMaterialBatches()])
    showMessage('领料已完成，库存批次已扣减。')
  } catch (error) {
    showMessage(getErrorMessage(error, '领料失败'))
  }
}

async function recordStep() {
  if (!detail.value) return
  if (!currentStepDisplay.value) {
    showMessage('所有工序已完成，请进行成品入库。')
    return
  }
  if (stepForm.lossQuantity < 0) {
    showMessage('损耗数量不能小于 0。')
    return
  }
  if (stepForm.lossQuantity > currentStepInputQuantity.value) {
    showMessage(`损耗数量不能超过本步可处理数量：${currentStepInputQuantity.value}。`)
    return
  }

  try {
    detail.value = await productionApi.recordStep(detail.value.order.id, {
      lossQuantity: Number(stepForm.lossQuantity),
      lossReason: stepForm.lossReason.trim() || undefined
    })
    initializeForms()
    await loadOrders()
    showMessage('工序记录已保存。')
  } catch (error) {
    showMessage(getErrorMessage(error, '保存工序记录失败'))
  }
}

async function inboundProduction() {
  if (!detail.value) return
  if (inboundForm.quantity <= 0) {
    showMessage('请填写正确的入库数量。')
    return
  }

  try {
    detail.value = await productionApi.inboundProduction(detail.value.order.id, {
      quantity: Number(inboundForm.quantity),
      productionDate: inboundForm.productionDate || undefined,
      expiryDate: inboundForm.expiryDate || undefined,
      remark: inboundForm.remark.trim() || undefined
    })
    initializeForms()
    await loadOrders()
    showMessage('成品已入库，成品批次已生成。')
  } catch (error) {
    showMessage(getErrorMessage(error, '成品入库失败'))
  }
}

function initializeForms() {
  if (!detail.value) return
  for (const plan of detail.value.materialPlans) {
    const currentForm = issueForms[plan.id]
    issueForms[plan.id] = {
      batchId: currentForm?.batchId ?? 0,
      quantity: Math.max(remainingQuantity(plan), 1),
      remark: currentForm?.remark ?? ''
    }
  }
  stepForm.lossQuantity = 0
  stepForm.lossReason = ''
  inboundForm.quantity = detail.value.order.status === 'WAIT_INBOUND'
    ? Math.max(detail.value.order.completedQuantity - detail.value.order.inboundQuantity, 0)
    : 0
  inboundForm.productionDate = new Date().toISOString().slice(0, 10)
  inboundForm.expiryDate = ''
  inboundForm.remark = ''
}

async function loadMaterialBatches() {
  if (!detail.value) return
  batchesLoading.value = true
  try {
    const productIds = [...new Set(detail.value.materialPlans.map(plan => plan.materialProductId))]
    const entries = await Promise.all(productIds.map(async productId => {
      const batches = await inventoryApi.getBatchesByProductId(productId)
      return [productId, batches] as const
    }))
    batchesByProductId.value = {
      ...batchesByProductId.value,
      ...Object.fromEntries(entries)
    }
  } catch (error) {
    showMessage(getErrorMessage(error, '加载原料批次失败'))
  } finally {
    batchesLoading.value = false
  }

  for (const plan of detail.value.materialPlans) {
    const options = batchOptions(plan)
    const form = issueForms[plan.id]
    if (form && options.length > 0 && !form.batchId) {
      form.batchId = options[0]!.id
    }
  }
}

async function refreshMaterialBatches() {
  await loadMaterialBatches()
  showMessage('原料批次已刷新。')
}

function remainingQuantity(plan: ProductionMaterialPlan) {
  return Math.max(plan.requiredQuantity - plan.issuedQuantity, 0)
}

function batchOptions(plan: ProductionMaterialPlan) {
  return batchesByProductId.value[plan.materialProductId] || []
}

function resetCreateForm() {
  createForm.productId = 0
  createForm.plannedQuantity = 100
  createForm.batchNo = ''
  createForm.plannedDate = ''
  createForm.remark = ''
}

function statusLabel(status: ProductionOrderStatus) {
  const labels: Record<ProductionOrderStatus, string> = {
    PLANNED: '待开始',
    IN_PROGRESS: '生产中',
    WAIT_INBOUND: '待入库',
    COMPLETED: '已完成',
    CANCELLED: '已取消'
  }
  return labels[status]
}

function stepLabel(step: ProductionStepType) {
  const routeStep = routeSteps.value.find(item => item.stepCode === step)
  if (routeStep) return routeStep.stepName
  const labels: Record<string, string> = {
    PREPARATION: '备料',
    WRAPPING: '包制',
    COOKING: '蒸煮',
    PACKAGING: '包装',
    STERILIZATION: '杀菌',
    BOXING: '装箱'
  }
  return labels[step] || step
}

function defaultRouteSteps() {
  return [
    { id: null, stepCode: 'PREPARATION', stepName: '备料', sortOrder: 10, allowLoss: true, completedQuantity: 0, lossQuantity: 0 },
    { id: null, stepCode: 'WRAPPING', stepName: '包制', sortOrder: 20, allowLoss: true, completedQuantity: 0, lossQuantity: 0 },
    { id: null, stepCode: 'COOKING', stepName: '蒸煮', sortOrder: 30, allowLoss: true, completedQuantity: 0, lossQuantity: 0 },
    { id: null, stepCode: 'PACKAGING', stepName: '包装', sortOrder: 40, allowLoss: true, completedQuantity: 0, lossQuantity: 0 },
    { id: null, stepCode: 'STERILIZATION', stepName: '杀菌', sortOrder: 50, allowLoss: true, completedQuantity: 0, lossQuantity: 0 },
    { id: null, stepCode: 'BOXING', stepName: '装箱', sortOrder: 60, allowLoss: true, completedQuantity: 0, lossQuantity: 0 }
  ]
}

function routeStepIndex(stepCode: ProductionStepType) {
  return routeSteps.value.findIndex(step => step.stepCode === stepCode)
}

function routeStepState(stepCode: ProductionStepType) {
  const index = routeStepIndex(stepCode)
  const status = detail.value?.order.status
  if (status === 'CANCELLED') return 'muted'
  if (index < activeRouteStepIndex.value) return 'done'
  if (index === activeRouteStepIndex.value) return 'current'
  return 'pending'
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
  if (error instanceof ApiError) return error.message || fallback
  return fallback
}

function showMessage(value: string) {
  message.value = value
  window.clearTimeout(messageTimer)
  messageTimer = window.setTimeout(() => {
    message.value = ''
  }, 3000)
}
</script>

<style scoped>
.page {
  width: 100%;
}

.page * {
  box-sizing: border-box;
}

.page-header {
  margin-bottom: 16px;
}

.page-eyebrow {
  margin: 0 0 6px;
  color: #2563eb;
  font-size: 13px;
  font-weight: 700;
}

.page-header h1,
.summary-head h2 {
  margin: 0;
  color: #0f172a;
  font-size: 20px;
}

.page-header p,
.panel-header p,
.summary-head p {
  margin: 6px 0 0;
  color: #64748b;
  font-size: 13px;
  line-height: 1.6;
}

.production-layout {
  display: grid;
  grid-template-columns: 360px minmax(0, 1fr);
  gap: 16px;
  align-items: start;
}

.left-column,
.right-column {
  display: grid;
  gap: 16px;
}

.panel {
  min-width: 0;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  background: #fff;
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.06);
  padding: 14px;
}

.panel-header,
.summary-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.panel-header h2 {
  margin: 0;
  color: #0f172a;
  font-size: 16px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.form-grid--single {
  grid-template-columns: 1fr;
}

label span {
  display: block;
  margin-bottom: 6px;
  color: #334155;
  font-size: 13px;
  font-weight: 700;
}

.field-warning {
  display: block;
  margin-top: 6px;
  color: #dc2626;
  font-size: 12px;
  line-height: 1.5;
}

input,
select,
textarea {
  width: 100%;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  padding: 9px 11px;
  color: #0f172a;
  font-size: 14px;
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

.primary-button,
.secondary-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 36px;
  border-radius: 8px;
  padding: 0 14px;
  font-weight: 700;
  cursor: pointer;
  text-decoration: none;
}

.primary-button {
  border: 0;
  background: #2563eb;
  color: #fff;
}

.secondary-button {
  border: 1px solid #dbe3ef;
  background: #fff;
  color: #334155;
}

.danger-outline {
  color: #dc2626;
}

.primary-button:disabled,
.secondary-button:disabled,
.text-button:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.full-button {
  width: 100%;
  margin-top: 12px;
}

.order-list-panel {
  max-height: 640px;
  overflow-y: auto;
}

.order-card {
  width: 100%;
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 8px;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 12px;
  background: #fff;
  text-align: left;
  cursor: pointer;
}

.order-card + .order-card {
  margin-top: 10px;
}

.order-card.active {
  border-color: #93c5fd;
  background: #eff6ff;
}

.order-card strong,
.data-table strong,
.history-row strong {
  display: block;
  color: #0f172a;
  font-size: 14px;
}

.order-card span,
.data-table span,
.history-row span {
  color: #64748b;
  font-size: 12px;
}

.order-meta {
  grid-column: 1 / -1;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.status-badge {
  border-radius: 999px;
  padding: 4px 8px;
  background: #f1f5f9;
  color: #334155 !important;
  font-size: 12px;
  font-weight: 700;
}

.status-badge--in_progress,
.status-badge--wait_inbound {
  background: #dbeafe;
  color: #1d4ed8 !important;
}

.status-badge--completed {
  background: #dcfce7;
  color: #15803d !important;
}

.status-badge--cancelled {
  background: #fee2e2;
  color: #dc2626 !important;
}

.summary-actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 10px;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 10px;
}

.metric-card {
  border: 1px solid #edf2f7;
  border-radius: 10px;
  padding: 12px;
  background: #f8fafc;
}

.metric-card span {
  display: block;
  color: #64748b;
  font-size: 12px;
}

.metric-card strong {
  display: block;
  margin-top: 6px;
  color: #0f172a;
  font-size: 16px;
}

.route-timeline {
  position: relative;
  display: flex;
  gap: 12px;
  margin: 0 0 14px;
  overflow-x: auto;
  padding: 8px 2px 14px;
}

.route-step {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: 42px minmax(0, 1fr);
  gap: 8px;
  flex: 0 0 176px;
  min-width: 176px;
}

.route-step:not(:last-child)::after {
  content: '';
  position: absolute;
  top: 20px;
  left: 42px;
  width: calc(100% + 12px);
  height: 2px;
  background: #dbe3ef;
  z-index: -1;
}

.route-node {
  display: grid;
  width: 42px;
  height: 42px;
  place-items: center;
  border: 2px solid #cbd5e1;
  border-radius: 999px;
  background: #f8fafc;
  color: #64748b;
  font-size: 13px;
  font-weight: 800;
}

.route-step strong {
  display: block;
  margin-top: 2px;
  color: #334155;
  font-size: 13px;
}

.route-step-metrics {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
  margin-top: 6px;
}

.route-step-metrics span {
  display: inline-flex;
  max-width: 100%;
  border-radius: 999px;
  padding: 3px 7px;
  background: #f1f5f9;
  color: #64748b;
  font-size: 11px;
  font-weight: 700;
  line-height: 1.2;
  white-space: normal;
}

.config-link {
  white-space: nowrap;
}

.route-step--done .route-node {
  border-color: #16a34a;
  background: #dcfce7;
  color: #15803d;
}

.route-step--done:not(:last-child)::after {
  background: #86efac;
}

.route-step--done strong {
  color: #15803d;
}

.route-step--done .route-step-metrics span {
  background: #dcfce7;
  color: #15803d;
}

.route-step--current .route-node {
  border-color: #2563eb;
  background: #2563eb;
  color: #ffffff;
  box-shadow: 0 0 0 6px rgba(37, 99, 235, 0.14);
}

.route-step--current:not(:last-child)::after {
  background: #bfdbfe;
}

.route-step--current strong {
  color: #1d4ed8;
}

.route-step--current .route-step-metrics span {
  background: #dbeafe;
  color: #1d4ed8;
}

.route-step--pending .route-node {
  border-color: #cbd5e1;
  background: #f8fafc;
  color: #94a3b8;
}

.route-step--pending strong {
  color: #64748b;
}

.route-step--pending .route-step-metrics span {
  background: #f8fafc;
  color: #94a3b8;
}

.route-step--muted {
  opacity: 0.55;
}

.step-progress-panel {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
  margin-bottom: 12px;
}

.step-progress-panel > div {
  border: 1px solid #dbeafe;
  border-radius: 10px;
  padding: 12px;
  background: #eff6ff;
}

.step-progress-panel span {
  display: block;
  color: #475569;
  font-size: 12px;
  font-weight: 700;
}

.step-progress-panel strong {
  display: block;
  margin-top: 6px;
  color: #0f172a;
  font-size: 16px;
}

.completion-summary {
  border: 1px solid #bbf7d0;
  border-radius: 10px;
  padding: 12px;
  background: #f0fdf4;
}

.completion-summary strong,
.completion-summary span {
  display: block;
}

.completion-summary strong {
  color: #15803d;
  font-size: 16px;
}

.completion-summary span {
  margin-top: 6px;
  color: #64748b;
  font-size: 13px;
}

.table-wrapper {
  overflow-x: auto;
}

.data-table {
  width: 100%;
  min-width: 960px;
  border-collapse: collapse;
}

.data-table th,
.data-table td {
  border-bottom: 1px solid #e2e8f0;
  padding: 10px;
  color: #0f172a;
  font-size: 13px;
  text-align: left;
  vertical-align: middle;
}

.data-table th {
  background: #f8fafc;
  color: #334155;
  font-weight: 700;
}

.text-button {
  border: 0;
  background: transparent;
  color: #2563eb;
  font-weight: 700;
  cursor: pointer;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.history-row {
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 10px;
  background: #fbfdff;
}

.history-row + .history-row {
  margin-top: 10px;
}

.history-row > div {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
}

.history-row b {
  display: inline-block;
  margin-left: 8px;
  color: #0f172a;
  font-size: 13px;
}

.history-row p {
  margin: 8px 0 0;
  color: #64748b;
  font-size: 13px;
}

.danger-text {
  color: #dc2626 !important;
}

.empty-box,
.empty-detail {
  color: #94a3b8;
  text-align: center;
}

.empty-box {
  padding: 18px 12px;
}

.empty-detail {
  padding: 80px 20px;
}

.operation-message {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 80;
  border: 1px solid #bfdbfe;
  border-radius: 10px;
  padding: 12px 16px;
  background: #eff6ff;
  color: #1d4ed8;
  box-shadow: 0 16px 36px rgba(37, 99, 235, 0.14);
}

@media (max-width: 1280px) {
  .production-layout,
  .detail-grid {
    grid-template-columns: 1fr;
  }

  .order-list-panel {
    max-height: none;
  }

  .metric-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 760px) {
  .form-grid,
  .metric-grid,
  .step-progress-panel {
    grid-template-columns: 1fr;
  }

  .panel-header,
  .summary-head,
  .history-row > div {
    display: grid;
  }
}
</style>
