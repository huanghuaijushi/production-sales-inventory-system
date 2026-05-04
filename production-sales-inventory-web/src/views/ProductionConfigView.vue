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
    </section>

    <div class="config-workspace">
      <section class="panel">
        <div class="panel-header">
          <div>
            <h2>{{ bomForm.id ? '编辑配方' : '添加配方' }}</h2>
            <p>单位用量表示生产 1 个成品需要多少当前库存单位的原材料。</p>
          </div>
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
          <button v-if="bomForm.id" type="button" class="secondary-button" @click="resetBomForm">
            取消编辑
          </button>
          <button type="button" class="primary-button" :disabled="bomSubmitting" @click="submitBomItem">
            {{ bomSubmitting ? '保存中...' : (bomForm.id ? '更新配方' : '添加配方') }}
          </button>
        </div>
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
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ApiError } from '@/api/http'
import { productApi, type Product } from '@/api/product'
import { productionApi, type BomItem } from '@/api/production'

const products = ref<Product[]>([])
const bomItems = ref<BomItem[]>([])
const bomSubmitting = ref(false)
const message = ref('')
const selectedFinishedProductId = ref(0)
const recipeSearch = ref('')
const recipePage = ref(1)
const recipePageSize = ref(3)
const bomFinishedQuery = ref('')
const bomMaterialQuery = ref('')
const bomForm = reactive({
  id: null as number | null,
  finishedProductId: 0,
  materialProductId: 0,
  quantityPerUnit: 1,
  lossRate: 0
})
let messageTimer: number | undefined

const rawMaterialProducts = computed(() => products.value.filter(product => product.type === 'RAW_MATERIAL'))
const finishedProducts = computed(() => products.value.filter(product => product.type === 'FINISHED_PRODUCT'))
const filteredFinishedProductsForBom = computed(() => filterProducts(finishedProducts.value, bomFinishedQuery.value))
const filteredRawMaterialProductsForBom = computed(() => filterProducts(rawMaterialProducts.value, bomMaterialQuery.value))
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

watch([selectedFinishedProductId, recipeSearch, recipePageSize], () => {
  recipePage.value = 1
})

watch(recipeTotalPages, (totalPages) => {
  if (recipePage.value > totalPages) recipePage.value = totalPages
})

onMounted(() => {
  loadInitialData()
})

async function loadInitialData() {
  await Promise.all([loadProducts(), loadBomItems()])
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
      resetBomForm()
    } else {
      await productionApi.createBomItem(payload)
      bomForm.materialProductId = 0
      bomForm.quantityPerUnit = 1
      bomForm.lossRate = 0
      bomMaterialQuery.value = ''
      showMessage('配方已添加。')
    }
    await loadBomItems()
  } catch (error) {
    showMessage(getErrorMessage(error, '保存配方失败'))
  } finally {
    bomSubmitting.value = false
  }
}

function editBomItem(item: BomItem) {
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

function resetBomForm() {
  bomForm.id = null
  bomForm.finishedProductId = 0
  bomForm.materialProductId = 0
  bomForm.quantityPerUnit = 1
  bomForm.lossRate = 0
  bomFinishedQuery.value = ''
  bomMaterialQuery.value = ''
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

.filter-panel {
  display: grid;
  grid-template-columns: minmax(260px, 1fr) 240px 160px;
  gap: 12px;
  margin-bottom: 14px;
  padding: 14px;
}

.config-workspace {
  display: grid;
  grid-template-columns: minmax(340px, 0.8fr) minmax(0, 1.45fr);
  gap: 16px;
  align-items: start;
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

.text-button {
  border: 0;
  padding: 0;
  background: transparent;
  color: #475569;
  font-weight: 700;
  cursor: pointer;
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
  .config-workspace {
    grid-template-columns: 1fr;
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
