<template>
  <div class="page">
    <div class="page-header">
      <div>
        <p class="page-eyebrow">生产配置</p>
        <h1>成品配方</h1>
        <p>维护“生产 1 个成品”需要消耗哪些原材料，用来计算可生产量和原材料缺口。</p>
      </div>
    </div>

    <p v-if="message" class="operation-message">{{ message }}</p>

    <div class="config-tabs">
      <button
        type="button"
        :class="{ active: activeConfigTab === 'bom' }"
        @click="switchConfigTab('bom')"
      >
        成品配方
      </button>
      <button
        type="button"
        :class="{ active: activeConfigTab === 'route' }"
        @click="switchConfigTab('route')"
      >
        工序路线
      </button>
    </div>

    <template v-if="activeConfigTab === 'bom'">
      <section class="filter-panel">
        <input v-model="recipeSearch" type="search" placeholder="搜索成品或原材料" />
        <select v-model.number="selectedFinishedProductId">
          <option :value="0">全部成品配方</option>
          <option v-for="product in finishedProducts" :key="product.id" :value="product.id">
            {{ product.sku }} · {{ product.name }}
          </option>
        </select>
        <select v-model.number="recipePageSize">
          <option :value="3">每页 3 个成品</option>
          <option :value="5">每页 5 个成品</option>
          <option :value="10">每页 10 个成品</option>
        </select>
        <button type="button" class="primary-button" @click="openBomEditor">
          添加配方
        </button>
      </section>

      <section class="panel detail-panel">
        <div class="panel-header">
          <div>
            <h2>配方详情</h2>
            <p>共 {{ recipeTotalGroups }} 个成品，{{ filteredBomItemCount }} 条明细。</p>
          </div>
        </div>
        <div v-if="paginatedBomGroups.length === 0" class="empty-box">暂无配方明细</div>
        <div v-for="group in paginatedBomGroups" v-else :key="group.productId" class="recipe-card">
          <div class="recipe-card-head">
            <div>
              <strong>{{ group.productCode }} · {{ group.productName }}</strong>
              <span>{{ group.items.length }} / {{ group.totalItems }} 种原材料</span>
            </div>
            <span class="soft-badge">成品</span>
          </div>
          <div v-if="group.items.length === 0" class="empty-inline">暂无配方明细</div>
          <div v-for="item in group.items" v-else :key="item.id" class="recipe-material-row">
            <div>
              <strong>{{ item.materialProductName }}</strong>
              <span>{{ item.materialProductCode }} · {{ item.materialProductUnit }}</span>
            </div>
            <div class="recipe-metrics">
              <span>{{ formatDecimal(item.quantityPerUnit) }}{{ item.materialProductUnit }}/个</span>
              <span>损耗 {{ formatPercent(item.lossRate) }}</span>
            </div>
            <div class="row-actions">
              <button type="button" class="text-button" @click="editBomItem(item)">编辑</button>
              <button type="button" class="text-button danger-text" @click="deleteBomItem(item)">删除</button>
            </div>
          </div>
        </div>
        <div v-if="recipeTotalGroups > 0" class="pagination-bar">
          <span>第 {{ recipePage }} / {{ recipeTotalPages }} 页</span>
          <div class="pagination-actions">
            <button type="button" :disabled="recipePage <= 1" @click="changeRecipePage(recipePage - 1)">上一页</button>
            <button type="button" :disabled="recipePage >= recipeTotalPages" @click="changeRecipePage(recipePage + 1)">下一页</button>
          </div>
        </div>
      </section>

      <div v-if="bomEditorOpen" class="modal-backdrop" @click.self="resetBomForm">
        <section class="modal-panel">
          <div class="panel-header">
            <div>
              <h2>{{ bomForm.id ? '编辑配方' : '添加配方' }}</h2>
              <p>单位用量表示生产 1 个成品需要多少当前库存单位的原材料。</p>
            </div>
            <button type="button" class="icon-button" @click="resetBomForm">×</button>
          </div>
          <div class="form-grid">
            <label>
              <span>成品</span>
              <input v-model="bomFinishedQuery" class="field-search" type="search" placeholder="先搜成品编号或名称" />
              <select v-model.number="bomForm.finishedProductId">
                <option :value="0" disabled>选择成品</option>
                <option v-for="product in filteredFinishedProductsForBom" :key="product.id" :value="product.id">
                  {{ product.sku }} · {{ product.name }}
                </option>
              </select>
            </label>
            <label>
              <span>原材料</span>
              <input v-model="bomMaterialQuery" class="field-search" type="search" placeholder="先搜原材料编号或名称" />
              <select v-model.number="bomForm.materialProductId">
                <option :value="0" disabled>选择原材料</option>
                <option v-for="product in filteredRawMaterialProductsForBom" :key="product.id" :value="product.id">
                  {{ product.sku }} · {{ product.name }}
                </option>
              </select>
            </label>
            <label>
              <span>单位用量</span>
              <input v-model.number="bomForm.quantityPerUnit" type="number" min="0.0001" step="0.0001" placeholder="0.0200" />
            </label>
            <label>
              <span>损耗率</span>
              <input v-model.number="bomForm.lossRate" type="number" min="0" max="1" step="0.0001" placeholder="0.0300" />
            </label>
          </div>
          <div class="form-actions">
            <button type="button" class="secondary-button" @click="resetBomForm">取消</button>
            <button type="button" class="primary-button" :disabled="bomSubmitting" @click="submitBomItem">
              {{ bomSubmitting ? '保存中...' : (bomForm.id ? '更新配方' : '添加配方') }}
            </button>
          </div>
        </section>
      </div>
    </template>

    <section v-else id="route-config" class="route-page">
      <section class="panel route-editor-panel">
        <div class="panel-header">
          <div>
            <h2>{{ routeForm.id ? '编辑工序' : '添加工序' }}</h2>
            <p>不同成品可以有不同生产路线，新建工单时会复制当前路线。</p>
          </div>
        </div>
        <div class="route-editor">
          <label>
            <span>选择成品</span>
            <input v-model="routeProductQuery" class="field-search" type="search" placeholder="先搜成品编号或名称" />
            <div class="route-product-list">
              <button
                v-for="product in filteredFinishedProductsForRoute"
                :key="product.id"
                type="button"
                :class="{ active: routeForm.productId === product.id }"
                @click="selectRouteProduct(product.id)"
              >
                <strong>{{ product.name }}</strong>
                <span>{{ product.sku }}</span>
              </button>
            </div>
          </label>
          <label>
            <span>工序编码</span>
            <input v-model="routeForm.stepCode" type="text" placeholder="如 SOAK_RICE" />
          </label>
          <label>
            <span>工序名称</span>
            <input v-model="routeForm.stepName" type="text" placeholder="如 浸米" />
          </label>
          <label>
            <span>排序</span>
            <input v-model.number="routeForm.sortOrder" type="number" min="0" step="10" />
          </label>
          <label class="checkbox-row">
            <input v-model="routeForm.allowLoss" type="checkbox" />
            <span>允许记录损耗</span>
          </label>
          <div class="form-actions">
            <button v-if="routeForm.id" type="button" class="secondary-button" @click="resetRouteForm">取消编辑</button>
            <button v-if="routeForm.id" type="button" class="secondary-button danger-action" @click="deleteRouteStepByForm">
              删除工序
            </button>
            <button type="button" class="primary-button" :disabled="routeSubmitting" @click="submitRouteStep">
              {{ routeSubmitting ? '保存中...' : (routeForm.id ? '更新工序' : '添加工序') }}
            </button>
          </div>
        </div>
      </section>

      <section class="panel route-list-panel">
        <div class="panel-header">
          <div>
            <h2>工序路线</h2>
            <p>点击任意工序即可在左侧编辑；没有配置的成品会使用系统默认路线。</p>
          </div>
        </div>
        <div v-if="routeGroups.length === 0" class="empty-box">暂无工序路线。</div>
        <div v-for="group in routeGroups" v-else :key="group.productId" class="route-card">
          <div class="route-card-head">
            <div>
              <strong>{{ group.productName }}</strong>
              <span>{{ group.productCode }}</span>
            </div>
            <span>{{ group.items.length }} 道工序</span>
          </div>
          <div class="route-line" :style="{ '--route-count': String(group.items.length || 1) }">
            <button
              v-for="(item, index) in group.items"
              :key="item.id"
              type="button"
              class="route-pill"
              :class="{ active: routeForm.id === item.id }"
              @click="editRouteStep(item)"
            >
              <b>{{ index + 1 }}</b>
              <strong>{{ item.stepName }}</strong>
              <span>{{ item.stepCode }} · 排序 {{ item.sortOrder }}{{ item.allowLoss ? ' · 可损耗' : '' }}</span>
            </button>
          </div>
        </div>
      </section>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ApiError } from '@/api/http'
import { productApi, type Product } from '@/api/product'
import { productionApi, type BomItem, type ProductionRouteStep } from '@/api/production'

const route = useRoute()
const router = useRouter()
const products = ref<Product[]>([])
const bomItems = ref<BomItem[]>([])
const routeSteps = ref<ProductionRouteStep[]>([])
const bomSubmitting = ref(false)
const routeSubmitting = ref(false)
const message = ref('')
const selectedFinishedProductId = ref(0)
const recipeSearch = ref('')
const recipePage = ref(1)
const recipePageSize = ref(3)
const bomFinishedQuery = ref('')
const bomMaterialQuery = ref('')
const bomEditorOpen = ref(false)
const routeProductQuery = ref('')
const activeConfigTab = ref<'bom' | 'route'>(route.hash === '#route-config' ? 'route' : 'bom')
const bomForm = reactive({
  id: null as number | null,
  finishedProductId: 0,
  materialProductId: 0,
  quantityPerUnit: 1,
  lossRate: 0
})
const routeForm = reactive({
  id: null as number | null,
  productId: 0,
  stepCode: '',
  stepName: '',
  sortOrder: 10,
  allowLoss: true
})
let messageTimer: number | undefined

const rawMaterialProducts = computed(() => products.value.filter(product => product.type === 'RAW_MATERIAL'))
const finishedProducts = computed(() => products.value.filter(product => product.type === 'FINISHED_PRODUCT'))
const filteredFinishedProductsForBom = computed(() => filterProducts(finishedProducts.value, bomFinishedQuery.value))
const filteredRawMaterialProductsForBom = computed(() => filterProducts(rawMaterialProducts.value, bomMaterialQuery.value))
const filteredFinishedProductsForRoute = computed(() => filterProducts(finishedProducts.value, routeProductQuery.value).slice(0, 8))
const filteredBomGroups = computed(() => {
  const query = recipeSearch.value.trim().toLowerCase()
  return finishedProducts.value
    .filter(product => selectedFinishedProductId.value === 0 || product.id === selectedFinishedProductId.value)
    .map(product => {
      const allItems = bomItems.value
        .filter(item => item.finishedProductId === product.id)
        .sort((first, second) => first.materialProductCode.localeCompare(second.materialProductCode, 'zh-CN'))
      const productMatched = productMatchesQuery(product, query)
      const items = query ? allItems.filter(item => productMatched || bomItemMatchesQuery(item, query)) : allItems
      return {
        productId: product.id,
        productCode: product.sku,
        productName: product.name,
        totalItems: allItems.length,
        items
      }
    })
    .filter(group => selectedFinishedProductId.value > 0 || group.items.length > 0)
})
const recipeTotalGroups = computed(() => filteredBomGroups.value.length)
const filteredBomItemCount = computed(() => filteredBomGroups.value.reduce((sum, group) => sum + group.items.length, 0))
const recipeTotalPages = computed(() => Math.max(1, Math.ceil(recipeTotalGroups.value / recipePageSize.value)))
const paginatedBomGroups = computed(() => {
  const startIndex = (recipePage.value - 1) * recipePageSize.value
  return filteredBomGroups.value.slice(startIndex, startIndex + recipePageSize.value)
})
const routeGroups = computed(() => {
  const groups = new Map<number, { productId: number; productCode: string; productName: string; items: ProductionRouteStep[] }>()
  for (const item of routeSteps.value.filter(step => step.enabled)) {
    const group = groups.get(item.productId) ?? {
      productId: item.productId,
      productCode: item.productCode,
      productName: item.productName,
      items: []
    }
    group.items.push(item)
    groups.set(item.productId, group)
  }
  return [...groups.values()].map(group => ({
    ...group,
    items: group.items.sort((first, second) => first.sortOrder - second.sortOrder || first.id - second.id)
  }))
})

watch([selectedFinishedProductId, recipeSearch, recipePageSize], () => {
  recipePage.value = 1
})

watch(recipeTotalPages, (totalPages) => {
  if (recipePage.value > totalPages) recipePage.value = totalPages
})

watch(() => route.hash, (hash) => {
  if (hash === '#route-config') activeConfigTab.value = 'route'
})

onMounted(() => {
  loadInitialData()
})

function switchConfigTab(tab: 'bom' | 'route') {
  activeConfigTab.value = tab
  if (tab === 'route') {
    router.replace({ hash: '#route-config' })
  } else {
    router.replace({ hash: '' })
  }
}

async function loadInitialData() {
  await Promise.all([loadProducts(), loadBomItems(), loadRouteSteps()])
}

async function loadProducts() {
  try {
    const result = await productApi.getAllProducts(0, 300)
    products.value = result.content
  } catch (error) {
    showMessage(getErrorMessage(error, '加载商品失败'))
  }
}

async function loadBomItems() {
  try {
    bomItems.value = await productionApi.getBomItems()
  } catch (error) {
    showMessage(getErrorMessage(error, '加载配方失败'))
  }
}

async function loadRouteSteps() {
  try {
    routeSteps.value = await productionApi.getRouteSteps()
  } catch (error) {
    showMessage(getErrorMessage(error, '加载工序路线失败'))
  }
}

function changeRecipePage(page: number) {
  recipePage.value = Math.min(Math.max(page, 1), recipeTotalPages.value)
}

async function submitBomItem() {
  if (bomForm.finishedProductId <= 0 || bomForm.materialProductId <= 0) {
    showMessage('请选择成品和原材料。')
    return
  }
  if (!Number.isFinite(bomForm.quantityPerUnit) || bomForm.quantityPerUnit <= 0) {
    showMessage('单位用量必须大于 0。')
    return
  }
  if (!Number.isFinite(bomForm.lossRate) || bomForm.lossRate < 0 || bomForm.lossRate > 1) {
    showMessage('损耗率必须在 0 到 1 之间。')
    return
  }

  bomSubmitting.value = true
  try {
    selectedFinishedProductId.value = bomForm.finishedProductId
    const payload = {
      finishedProductId: bomForm.finishedProductId,
      materialProductId: bomForm.materialProductId,
      quantityPerUnit: Number(bomForm.quantityPerUnit),
      lossRate: Number(bomForm.lossRate)
    }
    if (bomForm.id) {
      await productionApi.updateBomItem(bomForm.id, payload)
      showMessage('配方已更新。')
    } else {
      await productionApi.createBomItem(payload)
      showMessage('配方已添加。')
    }
    await loadBomItems()
    resetBomForm()
  } catch (error) {
    showMessage(getErrorMessage(error, '保存配方失败'))
  } finally {
    bomSubmitting.value = false
  }
}

async function submitRouteStep() {
  if (routeForm.productId <= 0 || !routeForm.stepCode.trim() || !routeForm.stepName.trim()) {
    showMessage('请选择成品，并填写工序编码和名称。')
    return
  }
  routeSubmitting.value = true
  try {
    const payload = {
      productId: routeForm.productId,
      stepCode: routeForm.stepCode.trim(),
      stepName: routeForm.stepName.trim(),
      sortOrder: Number(routeForm.sortOrder || 0),
      allowLoss: routeForm.allowLoss,
      enabled: true
    }
    if (routeForm.id) {
      await productionApi.updateRouteStep(routeForm.id, payload)
      showMessage('工序已更新。')
    } else {
      await productionApi.createRouteStep(payload)
      showMessage('工序已添加。')
    }
    resetRouteForm()
    await loadRouteSteps()
  } catch (error) {
    showMessage(getErrorMessage(error, '保存工序失败'))
  } finally {
    routeSubmitting.value = false
  }
}

function openBomEditor() {
  bomForm.id = null
  bomForm.finishedProductId = selectedFinishedProductId.value || 0
  bomForm.materialProductId = 0
  bomForm.quantityPerUnit = 1
  bomForm.lossRate = 0
  bomFinishedQuery.value = ''
  bomMaterialQuery.value = ''
  bomEditorOpen.value = true
}

function editRouteStep(item: ProductionRouteStep) {
  activeConfigTab.value = 'route'
  routeForm.id = item.id
  routeForm.productId = item.productId
  routeForm.stepCode = item.stepCode
  routeForm.stepName = item.stepName
  routeForm.sortOrder = item.sortOrder
  routeForm.allowLoss = item.allowLoss
}

function selectRouteProduct(productId: number) {
  routeForm.productId = productId
  const selectedProduct = finishedProducts.value.find(product => product.id === productId)
  routeProductQuery.value = selectedProduct ? `${selectedProduct.sku} ${selectedProduct.name}` : routeProductQuery.value
}

function editBomItem(item: BomItem) {
  bomEditorOpen.value = true
  bomForm.id = item.id
  bomForm.finishedProductId = item.finishedProductId
  bomForm.materialProductId = item.materialProductId
  bomForm.quantityPerUnit = Number(item.quantityPerUnit)
  bomForm.lossRate = Number(item.lossRate)
  bomFinishedQuery.value = `${item.finishedProductCode} ${item.finishedProductName}`
  bomMaterialQuery.value = `${item.materialProductCode} ${item.materialProductName}`
  selectedFinishedProductId.value = item.finishedProductId
}

async function deleteBomItem(item: BomItem) {
  const confirmed = window.confirm(`确定删除「${item.finishedProductName} - ${item.materialProductName}」这条配方吗？`)
  if (!confirmed) return

  try {
    await productionApi.deleteBomItem(item.id)
    showMessage('配方已删除。')
    await loadBomItems()
  } catch (error) {
    showMessage(getErrorMessage(error, '删除配方失败'))
  }
}

async function deleteRouteStepByForm() {
  if (!routeForm.id) return
  const confirmed = window.confirm(`确定删除「${routeForm.stepName}」这道工序吗？`)
  if (!confirmed) return

  try {
    await productionApi.deleteRouteStep(routeForm.id)
    showMessage('工序已删除。')
    resetRouteForm()
    await loadRouteSteps()
  } catch (error) {
    showMessage(getErrorMessage(error, '删除工序失败'))
  }
}

function resetBomForm() {
  bomForm.id = null
  bomForm.finishedProductId = 0
  bomForm.materialProductId = 0
  bomForm.quantityPerUnit = 1
  bomForm.lossRate = 0
  bomFinishedQuery.value = ''
  bomMaterialQuery.value = ''
  bomEditorOpen.value = false
}

function resetRouteForm() {
  routeForm.id = null
  routeForm.productId = selectedFinishedProductId.value || 0
  routeForm.stepCode = ''
  routeForm.stepName = ''
  routeForm.sortOrder = 10
  routeForm.allowLoss = true
}

function filterProducts(productList: Product[], query: string) {
  const normalizedQuery = query.trim().toLowerCase()
  if (!normalizedQuery) return productList
  return productList.filter(product => productMatchesQuery(product, normalizedQuery))
}

function productMatchesQuery(product: Product, normalizedQuery: string) {
  if (!normalizedQuery) return true
  return [
    product.sku,
    product.name,
    product.category ?? '',
    product.specification ?? '',
    product.unit
  ].some(value => value.toLowerCase().includes(normalizedQuery))
}

function bomItemMatchesQuery(item: BomItem, normalizedQuery: string) {
  if (!normalizedQuery) return true
  return [
    item.finishedProductCode,
    item.finishedProductName,
    item.materialProductCode,
    item.materialProductName,
    item.materialProductUnit
  ].some(value => value.toLowerCase().includes(normalizedQuery))
}

function formatDecimal(value: number) {
  return new Intl.NumberFormat('zh-CN', { maximumFractionDigits: 4 }).format(Number(value || 0))
}

function formatPercent(value: number) {
  return `${new Intl.NumberFormat('zh-CN', { maximumFractionDigits: 2 }).format(Number(value || 0) * 100)}%`
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
  }, 2800)
}
</script>

<style scoped>
.page {
  width: 100%;
  overflow-wrap: anywhere;
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

.page-header h1 {
  margin: 0;
  color: #0f172a;
  font-size: 20px;
}

.page-header p,
.panel-header p {
  margin: 6px 0 0;
  color: #64748b;
  font-size: 13px;
}

.filter-panel,
.panel {
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  background: #fff;
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.06);
}

.config-tabs {
  display: inline-flex;
  gap: 4px;
  margin-bottom: 14px;
  border: 1px solid #dbe3ef;
  border-radius: 10px;
  padding: 4px;
  background: #ffffff;
}

.config-tabs button {
  border: 0;
  border-radius: 8px;
  padding: 9px 16px;
  background: transparent;
  color: #475569;
  font-weight: 800;
  cursor: pointer;
}

.config-tabs button.active {
  background: #2563eb;
  color: #ffffff;
}

.filter-panel {
  display: grid;
  grid-template-columns: minmax(260px, 1fr) 240px 160px auto;
  gap: 12px;
  align-items: center;
  margin-bottom: 14px;
  padding: 14px;
}

.panel {
  min-width: 0;
  padding: 14px;
}

.panel-header {
  margin-bottom: 12px;
}

.panel-header h2 {
  margin: 0;
  color: #0f172a;
  font-size: 16px;
}

input,
select {
  width: 100%;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  padding: 9px 11px;
  color: #0f172a;
  font-size: 14px;
  font: inherit;
  outline: none;
}

input[type="checkbox"] {
  width: auto;
  padding: 0;
}

input:focus,
select:focus {
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.12);
}

.field-search {
  margin-bottom: 8px;
  background: #f8fafc;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

label span {
  display: block;
  margin-bottom: 6px;
  color: #334155;
  font-size: 13px;
  font-weight: 700;
}

.form-actions,
.row-actions,
.pagination-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 10px;
  flex-wrap: wrap;
}

.form-actions {
  margin-top: 12px;
}

.primary-button,
.secondary-button {
  border-radius: 8px;
  padding: 9px 14px;
  font-weight: 700;
  cursor: pointer;
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

.danger-action {
  border-color: #fecaca;
  color: #dc2626;
}

.text-button {
  border: 0;
  padding: 0;
  background: transparent;
  color: #475569;
  font-weight: 700;
  cursor: pointer;
}

.icon-button {
  display: inline-grid;
  width: 32px;
  height: 32px;
  place-items: center;
  border: 1px solid #dbe3ef;
  border-radius: 8px;
  background: #ffffff;
  color: #475569;
  font-size: 20px;
  line-height: 1;
  cursor: pointer;
}

.modal-backdrop {
  position: fixed;
  inset: 0;
  z-index: 100;
  display: grid;
  place-items: center;
  padding: 20px;
  background: rgba(15, 23, 42, 0.35);
}

.modal-panel {
  width: min(720px, 100%);
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 16px;
  background: #ffffff;
  box-shadow: 0 24px 64px rgba(15, 23, 42, 0.22);
}

.modal-panel .panel-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.route-panel {
  margin-top: 16px;
}

.route-page {
  display: grid;
  grid-template-columns: minmax(320px, 0.42fr) minmax(0, 1fr);
  gap: 16px;
  align-items: start;
}

.route-editor-panel {
  position: sticky;
  top: 14px;
}

.route-editor {
  border: 1px solid #edf2f7;
  border-radius: 10px;
  padding: 12px;
  background: #f8fafc;
}

.route-editor label + label {
  display: block;
  margin-top: 12px;
}

.route-product-list {
  display: grid;
  gap: 8px;
  max-height: 286px;
  overflow-y: auto;
}

.route-product-list button {
  border: 1px solid #dbe3ef;
  border-radius: 8px;
  padding: 9px 10px;
  background: #ffffff;
  text-align: left;
  cursor: pointer;
}

.route-product-list button.active {
  border-color: #2563eb;
  background: #eff6ff;
}

.route-product-list strong,
.route-product-list span {
  display: block;
}

.route-product-list strong {
  color: #0f172a;
  font-size: 13px;
}

.route-product-list span {
  margin-top: 3px;
  color: #64748b;
  font-size: 12px;
}

.checkbox-row {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  margin-top: 12px;
}

.checkbox-row span {
  margin: 0;
}

.route-card {
  margin-top: 12px;
  border: 1px solid #dbe3ef;
  border-radius: 10px;
  padding: 12px;
  background: #ffffff;
}

.route-card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.route-card-head strong {
  color: #0f172a;
  font-size: 14px;
}

.route-card-head span {
  display: block;
  margin-top: 3px;
  color: #64748b;
  font-size: 12px;
}

.route-line {
  position: relative;
  display: grid;
  grid-template-columns: repeat(var(--route-count), minmax(120px, 1fr));
  gap: 12px;
  overflow-x: auto;
  padding: 4px 0 2px;
}

.route-line::before {
  content: '';
  position: absolute;
  top: 22px;
  left: 20px;
  right: 20px;
  height: 2px;
  background: #dbeafe;
}

.route-pill {
  position: relative;
  z-index: 1;
  min-width: 120px;
  border: 1px solid transparent;
  border-radius: 8px;
  padding: 0 8px 8px;
  background: transparent;
  text-align: center;
  cursor: pointer;
}

.route-pill.active {
  border-color: #bfdbfe;
  background: #eff6ff;
}

.route-pill b {
  display: inline-grid;
  width: 38px;
  height: 24px;
  place-items: center;
  border-radius: 999px;
  background: #2563eb;
  color: #ffffff;
  font-size: 12px;
}

.route-pill strong,
.route-pill span {
  display: block;
}

.route-pill strong {
  margin-top: 8px;
  color: #0f172a;
  font-size: 13px;
}

.route-pill span {
  margin-top: 4px;
  color: #64748b;
  font-size: 12px;
}

.danger-text {
  color: #dc2626;
}

.recipe-card {
  border: 1px solid #dbe3ef;
  border-radius: 10px;
  padding: 12px;
  background: #fbfdff;
}

.recipe-card + .recipe-card {
  margin-top: 10px;
}

.recipe-card-head,
.recipe-material-row,
.pagination-bar {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 14px;
  min-width: 0;
}

.recipe-card-head strong,
.recipe-material-row strong {
  display: block;
  color: #0f172a;
  font-size: 13px;
}

.recipe-card-head span,
.recipe-material-row span {
  color: #64748b;
  font-size: 12px;
}

.soft-badge {
  border-radius: 999px;
  padding: 4px 9px;
  background: #eff6ff;
  color: #1d4ed8 !important;
  font-weight: 700;
}

.recipe-material-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto auto;
  align-items: center;
  margin-top: 8px;
  border-top: 1px solid #e8eef6;
  padding-top: 8px;
}

.recipe-metrics {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 7px;
}

.recipe-metrics span {
  border-radius: 999px;
  padding: 5px 8px;
  background: #f1f5f9;
  color: #334155;
  font-size: 12px;
  font-weight: 700;
}

.pagination-bar {
  margin-top: 14px;
  border-top: 1px solid #edf2f7;
  padding-top: 14px;
  color: #64748b;
  font-size: 13px;
}

.pagination-actions button {
  border: 1px solid #dbe3ef;
  border-radius: 6px;
  padding: 6px 10px;
  background: #fff;
  cursor: pointer;
}

.empty-box,
.empty-inline {
  color: #94a3b8;
  text-align: center;
}

.empty-box {
  padding: 18px 12px;
}

.empty-inline {
  margin-top: 10px;
  border-radius: 8px;
  padding: 10px;
  background: #f8fafc;
  font-size: 13px;
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

@media (max-width: 1180px) {
  .filter-panel,
  .route-page {
    grid-template-columns: 1fr;
  }

  .route-editor-panel {
    position: static;
  }
}

@media (max-width: 760px) {
  .form-grid,
  .recipe-material-row {
    grid-template-columns: 1fr;
  }

  .recipe-card-head,
  .pagination-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .row-actions,
  .recipe-metrics,
  .pagination-actions {
    justify-content: flex-start;
  }
}
</style>
