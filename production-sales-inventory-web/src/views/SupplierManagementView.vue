<template>
  <div class="page">
    <div class="page-header">
      <div>
        <p class="page-eyebrow">供应商管理</p>
        <h1>供应商资料与供货规则</h1>
        <p>先选择供应商，再维护它可以供应的原材料、价格、起订量和交期。</p>
      </div>
      <button type="button" class="primary-button" @click="openSupplierModal()">新增供应商</button>
    </div>

    <p v-if="message" class="operation-message">{{ message }}</p>

    <div class="supplier-workspace">
      <aside class="panel supplier-directory">
        <div class="section-header">
          <div>
            <h2>供应商资料</h2>
            <p>{{ suppliers.length }} 家供应商</p>
          </div>
          <button type="button" class="small-button" @click="openSupplierModal()">新增</button>
        </div>

        <label class="search-field">
          <span class="sr-only">搜索供应商</span>
          <input
            v-model="supplierQuery"
            type="search"
            placeholder="搜索供应商、联系人或电话"
            @keyup.enter="loadSuppliers"
          />
        </label>

        <div class="supplier-list">
          <div v-if="suppliersLoading" class="empty-box">正在加载供应商...</div>
          <div v-else-if="suppliers.length === 0" class="empty-box">暂无供应商</div>
          <button
            v-for="supplier in suppliers"
            v-else
            :key="supplier.id"
            type="button"
            class="supplier-card"
            :class="{ active: selectedSupplierId === supplier.id }"
            @click="selectSupplier(supplier)"
          >
            <span class="supplier-avatar">{{ getSupplierInitial(supplier.name) }}</span>
            <span class="supplier-card-body">
              <strong>{{ supplier.name }}</strong>
              <span>{{ supplier.contactName || '未填联系人' }} · {{ supplier.phone || '未填电话' }}</span>
              <small>{{ supplierRuleCount(supplier.id) }} 条供货规则</small>
            </span>
            <span class="supplier-card-actions" @click.stop>
              <button type="button" class="text-button" @click="openSupplierModal(supplier)">编辑</button>
              <button type="button" class="text-button danger-text" @click="deleteSupplier(supplier)">删除</button>
            </span>
          </button>
        </div>
        <div v-if="suppliers.length > 3" class="supplier-scroll-hint">向下滚动查看更多供应商</div>
      </aside>

      <main class="supplier-main">
        <section v-if="selectedSupplier" class="panel selected-supplier-panel">
          <div class="selected-supplier-title">
            <div>
              <p class="panel-eyebrow">当前供应商</p>
              <h2>{{ selectedSupplier.name }}</h2>
            </div>
            <div class="selected-supplier-actions">
              <button type="button" class="secondary-button" @click="openSupplierModal(selectedSupplier)">编辑资料</button>
              <button type="button" class="secondary-button" @click="clearSelectedSupplier">查看全部规则</button>
            </div>
          </div>
          <div class="supplier-profile-grid">
            <div>
              <span>联系人</span>
              <strong>{{ selectedSupplier.contactName || '未填写' }}</strong>
            </div>
            <div>
              <span>电话</span>
              <strong>{{ selectedSupplier.phone || '未填写' }}</strong>
            </div>
            <div>
              <span>供货规则</span>
              <strong>{{ selectedSupplierRuleCount }} 条</strong>
            </div>
            <div class="profile-wide">
              <span>地址 / 备注</span>
              <strong>{{ selectedSupplier.address || selectedSupplier.remark || '未填写' }}</strong>
            </div>
          </div>
        </section>

        <section v-else class="panel selected-supplier-panel empty-selected-panel">
          <div>
            <p class="panel-eyebrow">当前范围</p>
            <h2>全部供货规则</h2>
            <p>从左侧选择一个供应商，可以只维护它的原材料规则。</p>
          </div>
        </section>

        <section class="panel rule-list-panel">
          <div class="rule-list-toolbar">
            <div>
              <h2>{{ ruleScopeTitle }}</h2>
              <p>共 {{ visibleSupplierMaterials.length }} 条，新增规则从右侧按钮打开。</p>
            </div>
            <div class="rule-tools">
              <label class="search-field compact-search">
                <span class="sr-only">搜索规则</span>
                <input v-model="ruleQuery" type="search" placeholder="搜索原材料或供应商" />
              </label>
              <div class="segmented-control">
                <button type="button" :class="{ active: !showAllRules }" :disabled="!selectedSupplier" @click="showAllRules = false">
                  当前供应商
                </button>
                <button type="button" :class="{ active: showAllRules }" @click="showAllRules = true">全部</button>
              </div>
              <button type="button" class="primary-button" @click="openSupplierMaterialModal()">新增供货规则</button>
            </div>
          </div>

          <div v-if="visibleSupplierMaterials.length === 0" class="empty-box">暂无供货规则</div>
          <div v-else class="rule-table">
            <div class="rule-table-head">
              <span>原材料</span>
              <span>供应商</span>
              <span>价格</span>
              <span>起订/倍数</span>
              <span>交期</span>
              <span>操作</span>
            </div>
            <div v-for="item in visibleSupplierMaterials" :key="item.id" class="rule-table-row">
              <div class="cell-main">
                <strong>{{ item.productCode }} · {{ item.productName }}</strong>
                <span v-if="item.remark">{{ item.remark }}</span>
              </div>
              <div>{{ item.supplierName }}</div>
              <div class="money-cell">{{ formatMoney(item.defaultUnitPrice) }}</div>
              <div>起订 {{ item.minOrderQuantity }} · 倍数 {{ item.orderMultiple }}</div>
              <div>{{ formatLeadTime(item.leadTimeDays) }}</div>
              <div class="row-actions">
                <button type="button" class="text-button" @click="editSupplierMaterial(item)">编辑</button>
                <button type="button" class="text-button danger-text" @click="deleteSupplierMaterial(item)">删除</button>
              </div>
            </div>
          </div>
        </section>
      </main>
    </div>

    <div v-if="supplierModalOpen" class="modal-backdrop">
      <div class="modal-content supplier-modal">
        <div class="modal-header">
          <h2>{{ supplierForm.id ? '编辑供应商' : '新增供应商' }}</h2>
          <button type="button" class="icon-button" @click="closeSupplierModal">×</button>
        </div>
        <div class="modal-body">
          <label>
            <span>供应商名称</span>
            <input v-model="supplierForm.name" type="text" placeholder="请输入供应商名称" />
          </label>
          <div class="form-grid modal-form-grid">
            <label>
              <span>联系人</span>
              <input v-model="supplierForm.contactName" type="text" placeholder="联系人" />
            </label>
            <label>
              <span>电话</span>
              <input v-model="supplierForm.phone" type="text" placeholder="联系电话" />
            </label>
          </div>
          <label>
            <span>地址</span>
            <input v-model="supplierForm.address" type="text" placeholder="供应商地址" />
          </label>
          <label>
            <span>备注</span>
            <textarea v-model="supplierForm.remark" rows="3" placeholder="可填写常采购品类、账期等"></textarea>
          </label>
        </div>
        <div class="modal-actions">
          <button type="button" class="secondary-button" @click="closeSupplierModal">取消</button>
          <button type="button" class="primary-button" :disabled="supplierSubmitting" @click="submitSupplier">
            {{ supplierSubmitting ? '保存中...' : '保存供应商' }}
          </button>
        </div>
      </div>
    </div>

    <div v-if="supplierMaterialModalOpen" class="modal-backdrop">
      <div class="modal-content supplier-material-modal">
        <div class="modal-header">
          <h2>{{ supplierMaterialForm.id ? '编辑供货规则' : '新增供货规则' }}</h2>
          <button type="button" class="icon-button" @click="closeSupplierMaterialModal">×</button>
        </div>
        <div class="modal-body">
          <p class="modal-help">绑定供应商和原材料后，采购建议会自动按供应商分组，并计算金额和预计到货。</p>

          <div class="material-filter">
            <label>
              <span>原材料筛选</span>
              <input v-model="materialQuery" type="search" placeholder="输入编号或名称，过滤下面的原材料下拉框" />
            </label>
          </div>

          <div class="rule-form-layout">
            <label>
              <span>供应商</span>
              <select v-model.number="supplierMaterialForm.supplierId">
                <option :value="0" disabled>选择供应商</option>
                <option v-for="supplier in suppliers" :key="supplier.id" :value="supplier.id">
                  {{ supplier.name }}
                </option>
              </select>
            </label>
            <label class="material-picker">
              <span>原材料</span>
              <select v-model.number="supplierMaterialForm.productId" @change="syncSupplierMaterialPrice">
                <option :value="0" disabled>选择原材料</option>
                <option v-for="product in filteredRawMaterials" :key="product.id" :value="product.id">
                  {{ product.sku }} · {{ product.name }}
                </option>
              </select>
            </label>
            <label>
              <span>默认单价</span>
              <input v-model.number="supplierMaterialForm.defaultUnitPrice" type="number" min="0" step="0.01" placeholder="0.00" />
            </label>
            <label>
              <span>起订量</span>
              <input v-model.number="supplierMaterialForm.minOrderQuantity" type="number" min="1" placeholder="1" />
            </label>
            <label>
              <span>采购倍数</span>
              <input v-model.number="supplierMaterialForm.orderMultiple" type="number" min="1" placeholder="1" />
            </label>
            <label>
              <span>交期天数</span>
              <input v-model.number="supplierMaterialForm.leadTimeDays" type="number" min="0" placeholder="0" />
            </label>
          </div>
        </div>
        <div class="modal-actions">
          <button type="button" class="secondary-button" @click="closeSupplierMaterialModal">取消</button>
          <button type="button" class="primary-button" :disabled="supplierMaterialSubmitting" @click="submitSupplierMaterial">
            {{ supplierMaterialSubmitting ? '保存中...' : supplierMaterialForm.id ? '保存修改' : '添加规则' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ApiError } from '@/api/http'
import { productApi, type Product } from '@/api/product'
import { productionApi, type SupplierMaterial } from '@/api/production'
import { supplierApi, type Supplier } from '@/api/supplier'

const route = useRoute()
const suppliers = ref<Supplier[]>([])
const products = ref<Product[]>([])
const supplierMaterials = ref<SupplierMaterial[]>([])
const suppliersLoading = ref(false)
const supplierModalOpen = ref(false)
const supplierMaterialModalOpen = ref(false)
const supplierSubmitting = ref(false)
const supplierMaterialSubmitting = ref(false)
const selectedSupplierId = ref<number | null>(null)
const showAllRules = ref(false)
const supplierQuery = ref('')
const materialQuery = ref('')
const ruleQuery = ref('')
const message = ref('')
const supplierForm = reactive({
  id: null as number | null,
  name: '',
  contactName: '',
  phone: '',
  address: '',
  remark: ''
})
const supplierMaterialForm = reactive({
  id: null as number | null,
  supplierId: 0,
  productId: 0,
  defaultUnitPrice: 0,
  minOrderQuantity: 1,
  orderMultiple: 1,
  leadTimeDays: 0,
  preferred: true,
  remark: ''
})
let supplierSearchTimer: number | undefined
let messageTimer: number | undefined
let supplierSelectionInitialized = false

const rawMaterials = computed(() => products.value.filter(product => product.type === 'RAW_MATERIAL'))
const filteredRawMaterials = computed(() => filterProducts(rawMaterials.value, materialQuery.value))
const selectedSupplier = computed(() => suppliers.value.find(supplier => supplier.id === selectedSupplierId.value) ?? null)
const selectedSupplierRuleCount = computed(() => {
  if (!selectedSupplierId.value) return 0
  return supplierRuleCount(selectedSupplierId.value)
})
const sortedSupplierMaterials = computed(() => {
  return [...supplierMaterials.value].sort((first, second) => {
    const supplierCompare = first.supplierName.localeCompare(second.supplierName, 'zh-CN')
    if (supplierCompare !== 0) return supplierCompare
    return first.productCode.localeCompare(second.productCode, 'zh-CN')
  })
})
const visibleSupplierMaterials = computed(() => {
  const normalizedQuery = ruleQuery.value.trim().toLowerCase()
  return sortedSupplierMaterials.value.filter(item => {
    const inScope = showAllRules.value || !selectedSupplierId.value || item.supplierId === selectedSupplierId.value
    if (!inScope) return false
    if (!normalizedQuery) return true
    return [
      item.productCode,
      item.productName,
      item.supplierName,
      item.remark ?? ''
    ].some(value => value.toLowerCase().includes(normalizedQuery))
  })
})
const ruleScopeTitle = computed(() => {
  if (showAllRules.value || !selectedSupplier.value) return '全部供货规则'
  return `${selectedSupplier.value.name} 的供货规则`
})

watch(supplierQuery, () => {
  window.clearTimeout(supplierSearchTimer)
  supplierSearchTimer = window.setTimeout(() => loadSuppliers(), 300)
})

watch(selectedSupplierId, supplierId => {
  if (supplierId) {
    showAllRules.value = false
  }
  if (!supplierMaterialForm.id) {
    supplierMaterialForm.supplierId = supplierId ?? 0
  }
})

onMounted(async () => {
  await Promise.all([loadSuppliers(), loadProducts(), loadSupplierMaterials()])
  prefillFromQuery()
})

async function loadSuppliers() {
  suppliersLoading.value = true
  try {
    const result = await supplierApi.getSuppliers(0, 200, supplierQuery.value)
    suppliers.value = result.content
    syncSelectedSupplier()
  } catch (error) {
    showMessage(getErrorMessage(error, '加载供应商失败'))
  } finally {
    suppliersLoading.value = false
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

async function loadSupplierMaterials() {
  try {
    supplierMaterials.value = await productionApi.getSupplierMaterials()
  } catch (error) {
    showMessage(getErrorMessage(error, '加载供货规则失败'))
  }
}

async function prefillFromQuery() {
  const productId = Number(route.query.productId || 0)
  if (productId <= 0) return

  supplierMaterialForm.productId = productId
  const product = rawMaterials.value.find(item => item.id === productId)
  materialQuery.value = product ? `${product.sku} ${product.name}` : ''
  syncSupplierMaterialPrice()
  if (suppliers.value.length === 1) {
    selectedSupplierId.value = suppliers.value[0]?.id ?? null
  }
  await nextTick()
  supplierMaterialModalOpen.value = true
  showMessage('已带出原材料，请选择供应商后保存供货规则。')
}

function selectSupplier(supplier: Supplier) {
  selectedSupplierId.value = supplier.id
}

function clearSelectedSupplier() {
  selectedSupplierId.value = null
  showAllRules.value = true
  if (!supplierMaterialForm.id) {
    supplierMaterialForm.supplierId = 0
  }
}

function syncSelectedSupplier() {
  if (suppliers.value.length === 0) {
    selectedSupplierId.value = null
    showAllRules.value = true
    supplierSelectionInitialized = true
    if (!supplierMaterialForm.id) {
      supplierMaterialForm.supplierId = 0
    }
    return
  }

  const selectedStillVisible = suppliers.value.some(supplier => supplier.id === selectedSupplierId.value)
  if (!supplierSelectionInitialized || (selectedSupplierId.value && !selectedStillVisible)) {
    selectedSupplierId.value = suppliers.value[0]?.id ?? null
  }
  supplierSelectionInitialized = true

  if (!supplierMaterialForm.id && selectedSupplierId.value) {
    supplierMaterialForm.supplierId = selectedSupplierId.value
  }
}

function openSupplierModal(supplier?: Supplier) {
  resetSupplierForm()
  if (supplier) {
    supplierForm.id = supplier.id
    supplierForm.name = supplier.name
    supplierForm.contactName = supplier.contactName ?? ''
    supplierForm.phone = supplier.phone ?? ''
    supplierForm.address = supplier.address ?? ''
    supplierForm.remark = supplier.remark ?? ''
  }
  supplierModalOpen.value = true
}

function closeSupplierModal() {
  supplierModalOpen.value = false
}

function openSupplierMaterialModal() {
  resetSupplierMaterialForm()
  supplierMaterialModalOpen.value = true
}

function closeSupplierMaterialModal() {
  supplierMaterialModalOpen.value = false
  resetSupplierMaterialForm()
}

async function submitSupplier() {
  if (!supplierForm.name.trim()) {
    showMessage('请填写供应商名称。')
    return
  }

  supplierSubmitting.value = true
  try {
    const payload = {
      name: supplierForm.name.trim(),
      contactName: supplierForm.contactName.trim() || undefined,
      phone: supplierForm.phone.trim() || undefined,
      address: supplierForm.address.trim() || undefined,
      remark: supplierForm.remark.trim() || undefined
    }
    const savedSupplier = supplierForm.id
      ? await supplierApi.updateSupplier(supplierForm.id, payload)
      : await supplierApi.createSupplier(payload)
    showMessage(supplierForm.id ? '供应商已更新。' : '供应商已新增。')
    selectedSupplierId.value = savedSupplier.id
    closeSupplierModal()
    await loadSuppliers()
  } catch (error) {
    showMessage(getErrorMessage(error, '保存供应商失败'))
  } finally {
    supplierSubmitting.value = false
  }
}

async function deleteSupplier(supplier: Supplier) {
  const confirmed = window.confirm(`确定删除供应商「${supplier.name}」吗？`)
  if (!confirmed) return

  try {
    await supplierApi.deleteSupplier(supplier.id)
    showMessage('供应商已删除。')
    if (selectedSupplierId.value === supplier.id) {
      selectedSupplierId.value = null
      showAllRules.value = true
    }
    await Promise.all([loadSuppliers(), loadSupplierMaterials()])
  } catch (error) {
    showMessage(getErrorMessage(error, '删除供应商失败'))
  }
}

function syncSupplierMaterialPrice() {
  const product = rawMaterials.value.find(candidate => candidate.id === supplierMaterialForm.productId)
  supplierMaterialForm.defaultUnitPrice = Number(product?.costPrice ?? 0)
}

async function submitSupplierMaterial() {
  if (supplierMaterialForm.supplierId <= 0 || supplierMaterialForm.productId <= 0) {
    showMessage('请选择供应商和原材料。')
    return
  }
  if (!Number.isFinite(supplierMaterialForm.defaultUnitPrice) || supplierMaterialForm.defaultUnitPrice < 0) {
    showMessage('默认单价不能小于 0。')
    return
  }
  if (!Number.isFinite(supplierMaterialForm.minOrderQuantity) || supplierMaterialForm.minOrderQuantity <= 0) {
    showMessage('起订量必须大于 0。')
    return
  }
  if (!Number.isFinite(supplierMaterialForm.orderMultiple) || supplierMaterialForm.orderMultiple <= 0) {
    showMessage('采购倍数必须大于 0。')
    return
  }
  if (!Number.isFinite(supplierMaterialForm.leadTimeDays) || supplierMaterialForm.leadTimeDays < 0) {
    showMessage('交期不能小于 0。')
    return
  }

  supplierMaterialSubmitting.value = true
  try {
    const payload = {
      supplierId: supplierMaterialForm.supplierId,
      productId: supplierMaterialForm.productId,
      defaultUnitPrice: Number(supplierMaterialForm.defaultUnitPrice),
      minOrderQuantity: Number(supplierMaterialForm.minOrderQuantity),
      orderMultiple: Number(supplierMaterialForm.orderMultiple),
      leadTimeDays: Number(supplierMaterialForm.leadTimeDays),
      preferred: supplierMaterialForm.preferred,
      remark: supplierMaterialForm.remark.trim() || undefined
    }
    if (supplierMaterialForm.id) {
      await productionApi.updateSupplierMaterial(supplierMaterialForm.id, payload)
      showMessage('供货规则已更新。')
    } else {
      await productionApi.createSupplierMaterial(payload)
      showMessage('供货规则已添加。')
    }
    selectedSupplierId.value = payload.supplierId
    supplierMaterialModalOpen.value = false
    resetSupplierMaterialForm()
    await loadSupplierMaterials()
  } catch (error) {
    showMessage(getErrorMessage(error, '保存供货规则失败'))
  } finally {
    supplierMaterialSubmitting.value = false
  }
}

function editSupplierMaterial(item: SupplierMaterial) {
  supplierMaterialForm.id = item.id
  supplierMaterialForm.supplierId = item.supplierId
  supplierMaterialForm.productId = item.productId
  supplierMaterialForm.defaultUnitPrice = Number(item.defaultUnitPrice)
  supplierMaterialForm.minOrderQuantity = item.minOrderQuantity
  supplierMaterialForm.orderMultiple = item.orderMultiple
  supplierMaterialForm.leadTimeDays = item.leadTimeDays
  supplierMaterialForm.preferred = item.preferred
  supplierMaterialForm.remark = item.remark ?? ''
  selectedSupplierId.value = item.supplierId
  materialQuery.value = `${item.productCode} ${item.productName}`
  supplierMaterialModalOpen.value = true
}

async function deleteSupplierMaterial(item: SupplierMaterial) {
  const confirmed = window.confirm(`确定删除「${item.supplierName} - ${item.productName}」这条供货规则吗？`)
  if (!confirmed) return

  try {
    await productionApi.deleteSupplierMaterial(item.id)
    showMessage('供货规则已删除。')
    if (supplierMaterialForm.id === item.id) {
      resetSupplierMaterialForm()
    }
    await loadSupplierMaterials()
  } catch (error) {
    showMessage(getErrorMessage(error, '删除供货规则失败'))
  }
}

function resetSupplierForm() {
  supplierForm.id = null
  supplierForm.name = ''
  supplierForm.contactName = ''
  supplierForm.phone = ''
  supplierForm.address = ''
  supplierForm.remark = ''
}

function resetSupplierMaterialForm() {
  supplierMaterialForm.id = null
  supplierMaterialForm.supplierId = selectedSupplierId.value ?? 0
  supplierMaterialForm.productId = 0
  supplierMaterialForm.defaultUnitPrice = 0
  supplierMaterialForm.minOrderQuantity = 1
  supplierMaterialForm.orderMultiple = 1
  supplierMaterialForm.leadTimeDays = 0
  supplierMaterialForm.preferred = true
  supplierMaterialForm.remark = ''
  materialQuery.value = ''
}

function supplierRuleCount(supplierId: number) {
  return supplierMaterials.value.filter(item => item.supplierId === supplierId).length
}

function filterProducts(productList: Product[], query: string) {
  const normalizedQuery = query.trim().toLowerCase()
  if (!normalizedQuery) return productList
  return productList.filter(product => [
    product.sku,
    product.name
  ].some(value => value.toLowerCase().includes(normalizedQuery)))
}

function getSupplierInitial(name: string) {
  return name.trim().slice(0, 1).toUpperCase() || '供'
}

function formatMoney(value: number) {
  return new Intl.NumberFormat('zh-CN', {
    style: 'currency',
    currency: 'CNY'
  }).format(Number(value || 0))
}

function formatLeadTime(days: number) {
  return Number(days) > 0 ? `${days} 天` : '当天'
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
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 24px;
  margin-bottom: 16px;
}

.page-eyebrow,
.panel-eyebrow {
  margin: 0 0 6px;
  color: #2563eb;
  font-size: 13px;
  font-weight: 700;
}

.page-header h1 {
  margin: 0;
  color: #0f172a;
  font-size: 20px;
  line-height: 1.25;
}

.page-header p,
.section-header p,
.rule-list-toolbar p,
.empty-selected-panel p {
  margin: 6px 0 0;
  color: #64748b;
  font-size: 13px;
  line-height: 1.6;
}

.primary-button,
.secondary-button,
.small-button {
  border-radius: 8px;
  font-weight: 700;
  cursor: pointer;
  white-space: nowrap;
}

.primary-button {
  border: 0;
  padding: 10px 16px;
  background: #2563eb;
  color: #fff;
}

.secondary-button,
.small-button {
  border: 1px solid #dbe3ef;
  background: #fff;
  color: #334155;
}

.secondary-button {
  padding: 9px 13px;
}

.small-button {
  padding: 7px 11px;
}

.primary-button:disabled,
.segmented-control button:disabled {
  cursor: not-allowed;
  opacity: 0.55;
}

.operation-message {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 80;
  border: 1px solid #bfdbfe;
  border-radius: 8px;
  padding: 12px 16px;
  background: #eff6ff;
  color: #1d4ed8;
  box-shadow: 0 16px 36px rgba(37, 99, 235, 0.14);
}

.supplier-workspace {
  display: grid;
  grid-template-columns: 320px minmax(0, 1fr);
  gap: 18px;
  align-items: start;
}

.panel {
  min-width: 0;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.06);
}

.supplier-directory {
  position: sticky;
  top: 18px;
  display: flex;
  flex-direction: column;
  height: min(860px, calc(100vh - 140px));
  min-height: 720px;
  max-height: 900px;
  overflow: hidden;
}

.section-header,
.rule-list-toolbar,
.selected-supplier-title {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.section-header,
.selected-supplier-panel {
  padding: 18px;
}

.section-header {
  flex: 0 0 auto;
  border-bottom: 1px solid #edf2f7;
}

.section-header h2,
.rule-list-toolbar h2,
.selected-supplier-title h2 {
  margin: 0;
  color: #0f172a;
  font-size: 16px;
  line-height: 1.3;
}

.search-field {
  flex: 0 0 auto;
  display: block;
  padding: 14px 18px;
}

.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
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
  background: #fff;
}

input:focus,
select:focus,
textarea:focus {
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.12);
}

.supplier-list {
  flex: 1 1 auto;
  min-height: 0;
  max-height: none;
  overflow-y: auto;
  padding: 0 10px 18px;
  scrollbar-color: #94a3b8 #f1f5f9;
  scrollbar-width: thin;
}

.supplier-list::-webkit-scrollbar {
  width: 8px;
}

.supplier-list::-webkit-scrollbar-track {
  border-radius: 999px;
  background: #f1f5f9;
}

.supplier-list::-webkit-scrollbar-thumb {
  border: 2px solid #f1f5f9;
  border-radius: 999px;
  background: #94a3b8;
}

.supplier-list::-webkit-scrollbar-thumb:hover {
  background: #64748b;
}

.supplier-directory::after {
  position: absolute;
  right: 8px;
  bottom: 34px;
  left: 8px;
  height: 28px;
  pointer-events: none;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0), #ffffff);
  content: '';
}

.supplier-scroll-hint {
  flex: 0 0 auto;
  border-top: 1px solid #edf2f7;
  padding: 8px 12px;
  background: #ffffff;
  color: #64748b;
  font-size: 12px;
  text-align: center;
}

.supplier-card {
  display: grid;
  grid-template-columns: 42px minmax(0, 1fr);
  gap: 12px;
  align-items: start;
  width: 100%;
  border: 1px solid transparent;
  border-radius: 8px;
  padding: 10px;
  background: transparent;
  color: inherit;
  text-align: left;
  cursor: pointer;
}

.supplier-card + .supplier-card {
  margin-top: 6px;
}

.supplier-card:hover,
.supplier-card.active {
  border-color: #bfdbfe;
  background: #eff6ff;
}

.supplier-avatar {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 42px;
  height: 42px;
  border-radius: 8px;
  background: #dbeafe;
  color: #1d4ed8;
  font-weight: 800;
}

.supplier-card-body {
  min-width: 0;
}

.supplier-card strong,
.cell-main strong {
  display: block;
  color: #0f172a;
  font-size: 14px;
  line-height: 1.45;
}

.supplier-card-body > span,
.supplier-card-body > small,
.cell-main span {
  display: block;
  margin-top: 4px;
  color: #64748b;
  line-height: 1.45;
}

.supplier-card-actions {
  grid-column: 2;
  display: flex;
  gap: 12px;
  margin-top: 2px;
  opacity: 0.72;
  transition: opacity 0.16s ease;
}

.supplier-card:hover .supplier-card-actions,
.supplier-card.active .supplier-card-actions {
  opacity: 1;
}

.supplier-main {
  display: grid;
  gap: 18px;
  min-width: 0;
}

.selected-supplier-panel {
  display: grid;
  gap: 12px;
}

.selected-supplier-actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 10px;
}

.supplier-profile-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.supplier-profile-grid div {
  min-width: 0;
  border: 1px solid #edf2f7;
  border-radius: 8px;
  padding: 10px 12px;
  background: #f8fafc;
}

.supplier-profile-grid span {
  display: block;
  margin-bottom: 6px;
  color: #64748b;
  font-size: 12px;
}

.supplier-profile-grid strong {
  display: block;
  color: #0f172a;
  font-size: 14px;
  line-height: 1.45;
}

.profile-wide {
  grid-column: span 1;
}

.rule-form-panel {
  flex: 0 0 auto;
  padding: 16px;
}

.rule-form-panel .section-header {
  padding: 0 0 10px;
  border-bottom: 0;
}

.material-filter {
  margin-bottom: 10px;
  border: 1px solid #edf2f7;
  border-radius: 8px;
  padding: 8px 10px;
  background: #f8fafc;
}

.material-filter input {
  background: #fff;
}

.rule-form-layout {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  align-items: end;
}

.rule-form-layout > label {
  min-width: 0;
}

.material-picker {
  min-width: 0;
}

label span {
  display: block;
  margin-bottom: 6px;
  color: #334155;
  font-size: 13px;
  font-weight: 700;
}

.form-actions,
.modal-actions,
.row-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 10px;
  flex-wrap: wrap;
}

.form-actions {
  margin-top: 10px;
}

.rule-list-panel {
  display: flex;
  flex-direction: column;
  max-height: min(520px, calc(100vh - 280px));
  overflow: hidden;
}

.rule-list-toolbar {
  flex: 0 0 auto;
  align-items: center;
  flex-wrap: wrap;
  padding: 14px 16px;
  border-bottom: 1px solid #edf2f7;
}

.rule-list-toolbar > div:first-child {
  flex: 1 1 280px;
  min-width: 260px;
}

.rule-tools {
  display: flex;
  align-items: center;
  flex: 2 1 560px;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 10px;
  min-width: 0;
}

.compact-search {
  flex: 1 1 240px;
  min-width: min(240px, 100%);
  padding: 0;
}

.rule-tools > .primary-button {
  flex: 0 0 auto;
}

.segmented-control {
  display: inline-flex;
  border: 1px solid #dbe3ef;
  border-radius: 8px;
  overflow: hidden;
  background: #f8fafc;
}

.segmented-control button {
  border: 0;
  padding: 9px 11px;
  background: transparent;
  color: #64748b;
  font-weight: 700;
  cursor: pointer;
}

.segmented-control button.active {
  background: #2563eb;
  color: #fff;
}

.rule-table {
  max-height: 420px;
  overflow: auto;
  width: 100%;
}

.rule-table-head,
.rule-table-row {
  display: grid;
  grid-template-columns: minmax(180px, 1.55fr) minmax(130px, 1fr) minmax(92px, 0.72fr) minmax(120px, 0.9fr) minmax(76px, 0.58fr) minmax(96px, 0.62fr);
  gap: 12px;
  align-items: center;
  min-width: 760px;
  padding: 12px 16px;
}

.rule-table-head {
  position: sticky;
  top: 0;
  z-index: 1;
  background: #f8fafc;
  color: #64748b;
  font-size: 12px;
  font-weight: 800;
}

.rule-table-row {
  border-top: 1px solid #edf2f7;
  color: #334155;
  font-size: 13px;
}

.rule-table-row > div {
  min-width: 0;
  line-height: 1.5;
}

.rule-table-head > span:last-child,
.rule-table-row > div:last-child {
  text-align: right;
}

.rule-table-row .row-actions {
  justify-content: flex-end;
  flex-wrap: nowrap;
}

.money-cell {
  color: #0f172a;
  font-weight: 800;
}

.text-button {
  border: 0;
  padding: 0;
  background: transparent;
  color: #475569;
  font-weight: 700;
  cursor: pointer;
  white-space: nowrap;
}

.danger-text {
  color: #dc2626;
}

.empty-box {
  padding: 22px 12px;
  color: #94a3b8;
  text-align: center;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
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
  width: min(560px, 100%);
  max-height: 90vh;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 24px 60px rgba(15, 23, 42, 0.22);
}

.supplier-material-modal {
  width: min(920px, 100%);
}

.modal-help {
  margin: 0;
  color: #64748b;
  line-height: 1.6;
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
  width: 36px;
  height: 36px;
  border: 0;
  border-radius: 8px;
  background: #f1f5f9;
  color: #334155;
  font-size: 22px;
  cursor: pointer;
}

.modal-body {
  display: grid;
  gap: 16px;
  overflow-y: auto;
  padding: 22px 24px;
}

.modal-actions {
  padding: 16px 24px;
  border-top: 1px solid #e2e8f0;
}

@media (max-width: 1180px) {
  .supplier-workspace {
    grid-template-columns: 1fr;
  }

  .supplier-directory {
    position: static;
    height: auto;
    min-height: 0;
    max-height: none;
  }

  .supplier-list {
    max-height: 360px;
  }

  .rule-tools {
    min-width: 0;
  }

  .rule-list-panel {
    min-height: 0;
  }

  .rule-table {
    overflow-x: auto;
  }
}

@media (max-width: 980px) {
  .supplier-profile-grid,
  .rule-form-layout {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .rule-list-toolbar {
    flex-direction: column;
  }

  .rule-tools {
    width: 100%;
    justify-content: stretch;
  }

  .rule-table-head {
    display: none;
  }

  .rule-table-row {
    grid-template-columns: 1fr;
    gap: 8px;
    min-width: 0;
  }

  .row-actions {
    justify-content: flex-start;
  }

  .rule-table-row > div:last-child {
    text-align: left;
  }
}

@media (max-width: 760px) {
  .page-header,
  .section-header,
  .selected-supplier-title,
  .rule-tools {
    flex-direction: column;
  }

  .supplier-profile-grid,
  .rule-form-layout,
  .modal-form-grid {
    grid-template-columns: 1fr;
  }

  .compact-search {
    width: 100%;
  }

  .form-actions,
  .modal-actions,
  .selected-supplier-actions {
    justify-content: flex-start;
  }
}
</style>
