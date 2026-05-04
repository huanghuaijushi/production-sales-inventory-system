<template>
  <div class="product-management">
    <div class="page-header">
      <h1>产品管理</h1>
      <button class="btn btn-primary" @click="showCreateModal = true">
        <PlusIcon class="icon" />
        新增产品
      </button>
    </div>

    <div class="filters-section">
      <div class="search-box">
        <input
          type="text"
          v-model="searchQuery"
          placeholder="搜索产品名称或编码..."
          @input="handleSearch()"
        />
        <MagnifyingGlassIcon class="search-icon" />
      </div>
    </div>

    <div class="products-table">
      <table>
        <thead>
          <tr>
            <th>产品编码</th>
            <th>产品名称</th>
            <th>类型</th>
            <th>类别</th>
            <th>规格型号</th>
            <th>计量单位</th>
            <th>预警库存</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="product in products" :key="product.id">
            <td>{{ product.sku }}</td>
            <td>{{ product.name }}</td>
            <td>{{ productTypeLabel(product.type) }}</td>
            <td>{{ product.category || '-' }}</td>
            <td>{{ product.specification || '-' }}</td>
            <td>{{ product.unit }}</td>
            <td>{{ product.alertQuantity ?? 0 }}</td>
            <td>
              <div class="table-actions">
                <button class="btn btn-sm btn-secondary" @click="editProduct(product)">
                  编辑
                </button>
                <button class="btn btn-sm btn-danger" @click="openDeleteModal(product)">
                  删除
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="pagination" v-if="totalElements > 0">
      <div class="pagination-summary">
        <span>第 {{ currentPage + 1 }} 页 / 共 {{ displayTotalPages }} 页</span>
        <span>共 {{ totalElements }} 条记录，每页 {{ pageSize }} 条</span>
      </div>

      <div class="pagination-controls">
        <button
          class="btn btn-sm btn-outline"
          type="button"
          :disabled="currentPage === 0"
          @click="handlePageChange(0)"
        >
          首页
        </button>

        <button
          class="btn btn-sm btn-outline"
          type="button"
          :disabled="currentPage === 0"
          @click="handlePageChange(currentPage - 1)"
        >
          上一页
        </button>

        <button
          v-for="pageNumber in pageNumbers"
          :key="pageNumber"
          class="btn btn-sm page-number"
          type="button"
          :class="{ active: pageNumber === currentPage + 1 }"
          @click="handlePageChange(pageNumber - 1)"
        >
          {{ pageNumber }}
        </button>

        <button
          class="btn btn-sm btn-outline"
          type="button"
          :disabled="currentPage >= displayTotalPages - 1"
          @click="handlePageChange(currentPage + 1)"
        >
          下一页
        </button>

        <button
          class="btn btn-sm btn-outline"
          type="button"
          :disabled="currentPage >= displayTotalPages - 1"
          @click="handlePageChange(displayTotalPages - 1)"
        >
          末页
        </button>
      </div>
    </div>

    <div class="pagination pagination--empty" v-else>
      暂无产品数据
    </div>

    <!-- 创建产品模态框 -->
    <Teleport to="body">
      <div v-if="showCreateModal" class="modal-overlay" @click="closeCreateModal">
        <div class="modal-content" @click.stop>
          <header class="modal-header">
            <h2>新增产品</h2>
            <button type="button" class="modal-close" @click="closeCreateModal">
              <XMarkIcon class="close-icon" />
            </button>
          </header>

          <form class="modal-body" @submit.prevent="handleCreateProduct">
            <div class="form-grid">
              <label class="form-field">
                <span>产品编码 *</span>
                <input
                  type="text"
                  v-model="createForm.code"
                  placeholder="请输入产品编码"
                  required
                />
              </label>

              <label class="form-field">
                <span>产品名称 *</span>
                <input
                  type="text"
                  v-model="createForm.name"
                  placeholder="请输入产品名称"
                  required
                />
              </label>

              <label class="form-field">
                <span>产品类型 *</span>
                <select v-model="createForm.type" required>
                  <option value="">请选择产品类型</option>
                  <option value="FINISHED_PRODUCT">成品 / 半成品</option>
                  <option value="RAW_MATERIAL">原料 / 包装</option>
                </select>
              </label>

              <label class="form-field">
                <span>产品类别</span>
                <input
                  type="text"
                  v-model="createForm.category"
                  placeholder="如：粽子、原料、包装"
                />
              </label>

              <label class="form-field">
                <span>计量单位 *</span>
                <input
                  type="text"
                  v-model="createForm.unit"
                  placeholder="如：个、米、kg等"
                  required
                />
              </label>

              <label class="form-field">
                <span>规格型号</span>
                <input
                  type="text"
                  v-model="createForm.specification"
                  placeholder="请输入规格型号"
                />
              </label>

              <label class="form-field">
                <span>成本价</span>
                <input
                  type="number"
                  v-model.number="createForm.costPrice"
                  step="0.01"
                  placeholder="可选"
                />
              </label>

              <label class="form-field">
                <span>销售价</span>
                <input
                  type="number"
                  v-model.number="createForm.salePrice"
                  step="0.01"
                  placeholder="可选"
                />
              </label>

              <label class="form-field">
                <span>预警数量</span>
                <input
                  type="number"
                  v-model.number="createForm.alertQuantity"
                  min="0"
                  placeholder="库存预警数量"
                />
              </label>

              <label class="form-field full-width">
                <span>产品描述</span>
                <textarea
                  v-model="createForm.description"
                  placeholder="可选，产品描述信息"
                  rows="3"
                ></textarea>
              </label>
            </div>

            <p v-if="formMessage" class="form-message">{{ formMessage }}</p>

            <div class="modal-actions">
              <button type="button" class="btn btn-secondary" @click="closeCreateModal">取消</button>
              <button type="submit" class="btn btn-primary" :disabled="loading">
                <span v-if="!loading">创建产品</span>
                <span v-else class="loading-text">
                  <svg viewBox="0 0 24 24" class="loading-icon">
                    <circle cx="12" cy="12" r="9" />
                  </svg>
                  创建中...
                </span>
              </button>
            </div>
          </form>
        </div>
      </div>
    </Teleport>

    <!-- 编辑产品模态框 -->
    <Teleport to="body">
      <div v-if="showEditModal" class="modal-overlay" @click="closeEditModal">
        <div class="modal-content" @click.stop>
          <header class="modal-header">
            <h2>编辑产品</h2>
            <button type="button" class="modal-close" @click="closeEditModal">
              <XMarkIcon class="close-icon" />
            </button>
          </header>

          <form class="modal-body" @submit.prevent="handleUpdateProduct">
            <div class="form-grid">
              <label class="form-field">
                <span>产品编码 *</span>
                <input
                  type="text"
                  v-model="editForm.code"
                  placeholder="请输入产品编码"
                  required
                />
              </label>

              <label class="form-field">
                <span>产品名称 *</span>
                <input
                  type="text"
                  v-model="editForm.name"
                  placeholder="请输入产品名称"
                  required
                />
              </label>

              <label class="form-field">
                <span>产品类型 *</span>
                <select v-model="editForm.type" required>
                  <option value="">请选择产品类型</option>
                  <option value="FINISHED_PRODUCT">成品 / 半成品</option>
                  <option value="RAW_MATERIAL">原料 / 包装</option>
                </select>
              </label>

              <label class="form-field">
                <span>产品类别</span>
                <input
                  type="text"
                  v-model="editForm.category"
                  placeholder="如：粽子、原料、包装"
                />
              </label>

              <label class="form-field">
                <span>计量单位 *</span>
                <input
                  type="text"
                  v-model="editForm.unit"
                  placeholder="如：个、米、kg等"
                  required
                />
              </label>

              <label class="form-field">
                <span>规格型号</span>
                <input
                  type="text"
                  v-model="editForm.specification"
                  placeholder="请输入规格型号"
                />
              </label>

              <label class="form-field">
                <span>成本价</span>
                <input
                  type="number"
                  v-model.number="editForm.costPrice"
                  step="0.01"
                  placeholder="可选"
                />
              </label>

              <label class="form-field">
                <span>销售价</span>
                <input
                  type="number"
                  v-model.number="editForm.salePrice"
                  step="0.01"
                  placeholder="可选"
                />
              </label>

              <label class="form-field">
                <span>预警数量</span>
                <input
                  type="number"
                  v-model.number="editForm.alertQuantity"
                  min="0"
                  placeholder="库存预警数量"
                />
              </label>

              <label class="form-field full-width">
                <span>产品描述</span>
                <textarea
                  v-model="editForm.description"
                  placeholder="可选，产品描述信息"
                  rows="3"
                ></textarea>
              </label>
            </div>

            <p v-if="formMessage" class="form-message">{{ formMessage }}</p>

            <div class="modal-actions">
              <button type="button" class="btn btn-secondary" @click="closeEditModal">取消</button>
              <button type="submit" class="btn btn-primary" :disabled="loading">
                <span v-if="!loading">保存修改</span>
                <span v-else class="loading-text">
                  <svg viewBox="0 0 24 24" class="loading-icon">
                    <circle cx="12" cy="12" r="9" />
                  </svg>
                  保存中...
                </span>
              </button>
            </div>
          </form>
        </div>
      </div>
    </Teleport>

    <!-- 删除产品确认框 -->
    <Teleport to="body">
      <div v-if="showDeleteModal && deletingProduct" class="modal-overlay" @click="closeDeleteModal">
        <div class="modal-content modal-content--small" @click.stop>
          <header class="modal-header">
            <h2>删除产品</h2>
            <button type="button" class="modal-close" @click="closeDeleteModal">
              <XMarkIcon class="close-icon" />
            </button>
          </header>

          <div class="modal-body">
            <p class="delete-copy">
              确定删除产品「{{ deletingProduct.name }}」吗？系统会停用该产品并保留历史库存流水。
            </p>
            <p class="delete-hint">
              如果产品仍有库存或锁定库存，删除会被拒绝，请先完成库存处理。
            </p>
            <p v-if="deleteMessage" class="form-message">{{ deleteMessage }}</p>

            <div class="modal-actions">
              <button type="button" class="btn btn-secondary" @click="closeDeleteModal">取消</button>
              <button type="button" class="btn btn-danger" :disabled="deleteLoading" @click="handleDeleteProduct">
                {{ deleteLoading ? '删除中...' : '确认删除' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { PlusIcon, MagnifyingGlassIcon, XMarkIcon } from '@heroicons/vue/24/outline'
import { productApi, type Product, type ProductRequest, type PageResponse } from '@/api/product'

const route = useRoute()
const products = ref<Product[]>([])
const searchQuery = ref('')
const showCreateModal = ref(false)
const showEditModal = ref(false)
const showDeleteModal = ref(false)
const loading = ref(false)
const deleteLoading = ref(false)
const formMessage = ref('')
const deleteMessage = ref('')
const editingProduct = ref<Product | null>(null)
const deletingProduct = ref<Product | null>(null)
const currentPage = ref(0)
const pageSize = ref(5)
const totalElements = ref(0)
const totalPages = ref(0)

const displayTotalPages = computed(() => Math.max(totalPages.value, 1))

const pageNumbers = computed(() => {
  const maxButtons = 5
  const start = Math.max(1, currentPage.value + 1 - 2)
  const end = Math.min(displayTotalPages.value, start + maxButtons - 1)
  const pages: number[] = []

  for (let pageNumber = start; pageNumber <= end; pageNumber += 1) {
    pages.push(pageNumber)
  }

  return pages
})

const createForm = ref<ProductRequest>({
  code: '',
  name: '',
  type: '',
  category: '',
  unit: ''
})

const editForm = ref<ProductRequest>({
  code: '',
  name: '',
  type: '',
  category: '',
  unit: '',
  specification: '',
  costPrice: undefined,
  salePrice: undefined,
  alertQuantity: 0,
  description: ''
})

function applyPageResult(result: PageResponse<Product>) {
  products.value = result.content
  currentPage.value = result.number
  totalElements.value = result.totalElements
  totalPages.value = result.totalPages
}

const loadProducts = async (page: number = 0) => {
  try {
    const result: PageResponse<Product> = await productApi.getAllProducts(page, pageSize.value)
    applyPageResult(result)
  } catch (error) {
    console.error('加载产品列表失败:', error)
  }
}

const handleSearch = async (page: number = 0) => {
  if (searchQuery.value.trim()) {
    try {
      const result = await productApi.searchProducts(searchQuery.value, page, pageSize.value)
      applyPageResult(result)
    } catch (error) {
      console.error('搜索产品失败:', error)
    }
  } else {
    await loadProducts(page)
  }
}

const handleCreateProduct = async () => {
  loading.value = true
  formMessage.value = ''
  try {
    await productApi.createProduct(createForm.value)
    closeCreateModal()
    await reloadCurrentProducts()
  } catch (error) {
    console.error('创建产品失败:', error)
    formMessage.value = error instanceof Error ? error.message : '创建产品失败，请检查输入信息。'
  } finally {
    loading.value = false
  }
}

const closeCreateModal = () => {
  showCreateModal.value = false
  formMessage.value = ''
  createForm.value = {
    code: '',
    name: '',
    type: '',
    category: '',
    unit: ''
  }
}

const editProduct = (product: Product) => {
  editingProduct.value = product
  editForm.value = {
    code: product.sku,
    name: product.name,
    type: product.type,
    category: product.category || '',
    unit: product.unit,
    specification: product.specification || '',
    costPrice: product.costPrice,
    salePrice: product.salePrice,
    alertQuantity: product.alertQuantity ?? 0,
    description: product.description || ''
  }
  formMessage.value = ''
  showEditModal.value = true
}

const handleUpdateProduct = async () => {
  if (!editingProduct.value) {
    return
  }

  loading.value = true
  formMessage.value = ''
  try {
    await productApi.updateProduct(editingProduct.value.id, editForm.value)
    closeEditModal()
    await reloadCurrentProducts()
  } catch (error) {
    console.error('更新产品失败:', error)
    formMessage.value = error instanceof Error ? error.message : '更新产品失败，请检查输入信息。'
  } finally {
    loading.value = false
  }
}

const closeEditModal = () => {
  showEditModal.value = false
  editingProduct.value = null
  formMessage.value = ''
}

const openDeleteModal = (product: Product) => {
  deletingProduct.value = product
  deleteMessage.value = ''
  showDeleteModal.value = true
}

const closeDeleteModal = () => {
  if (deleteLoading.value) {
    return
  }
  showDeleteModal.value = false
  deletingProduct.value = null
  deleteMessage.value = ''
}

const handleDeleteProduct = async () => {
  if (!deletingProduct.value) {
    return
  }

  deleteLoading.value = true
  deleteMessage.value = ''
  const nextPage = products.value.length === 1 && currentPage.value > 0
    ? currentPage.value - 1
    : currentPage.value

  try {
    await productApi.deleteProduct(deletingProduct.value.id)
    showDeleteModal.value = false
    deletingProduct.value = null
    if (searchQuery.value.trim()) {
      await handleSearch(nextPage)
    } else {
      await loadProducts(nextPage)
    }
  } catch (error) {
    console.error('删除产品失败:', error)
    deleteMessage.value = error instanceof Error ? error.message : '删除产品失败，请稍后重试。'
  } finally {
    deleteLoading.value = false
  }
}

const handlePageChange = async (page: number) => {
  if (page < 0 || page >= displayTotalPages.value || page === currentPage.value) {
    return
  }

  if (searchQuery.value.trim()) {
    await handleSearch(page)
    return
  }

  await loadProducts(page)
}

async function reloadCurrentProducts() {
  if (searchQuery.value.trim()) {
    await handleSearch(currentPage.value)
    return
  }

  await loadProducts(currentPage.value)
}

function productTypeLabel(type: Product['type']) {
  return type === 'RAW_MATERIAL' ? '原料 / 包装' : '成品 / 半成品'
}

function routeSearchKeyword() {
  return typeof route.query.q === 'string' ? route.query.q : ''
}

async function applyRouteSearch() {
  searchQuery.value = routeSearchKeyword()
  if (searchQuery.value.trim()) {
    await handleSearch(0)
    return
  }
  await loadProducts(0)
}

watch(
  () => route.query.q,
  () => {
    applyRouteSearch()
  }
)

onMounted(() => {
  applyRouteSearch()
})
</script>

<style scoped>
.product-management {
  padding: 1.5rem;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.page-header h1 {
  margin: 0;
  color: #1f2937;
}

.filters-section {
  margin-bottom: 1.5rem;
}

.search-box {
  position: relative;
  max-width: 400px;
}

.search-box input {
  width: 100%;
  padding: 0.75rem 1rem 0.75rem 2.5rem;
  border: 1px solid #d1d5db;
  border-radius: 0.5rem;
  font-size: 0.875rem;
}

.search-icon {
  position: absolute;
  left: 0.75rem;
  top: 50%;
  transform: translateY(-50%);
  width: 1.25rem;
  height: 1.25rem;
  color: #9ca3af;
}

.products-table {
  background: white;
  border-radius: 0.5rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  overflow-x: auto;
}

table {
  width: 100%;
  min-width: 920px;
  border-collapse: collapse;
}

thead {
  background: #f9fafb;
}

th, td {
  padding: 0.75rem 1rem;
  text-align: left;
  border-bottom: 1px solid #e5e7eb;
}

th {
  font-weight: 600;
  color: #374151;
}

.btn-sm {
  padding: 0.25rem 0.5rem;
  font-size: 0.75rem;
}

.table-actions {
  display: flex;
  gap: 0.5rem;
}

/* 模态框样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 0.5rem;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1);
  max-width: 600px;
  width: 90%;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-content--small {
  max-width: 460px;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid #e5e7eb;
}

.modal-header h2 {
  margin: 0;
  color: #1f2937;
}

.modal-close {
  background: none;
  border: none;
  color: #6b7280;
  cursor: pointer;
  padding: 0.25rem;
  border-radius: 0.25rem;
}

.modal-close:hover {
  color: #374151;
  background: #f3f4f6;
}

.close-icon {
  width: 1.5rem;
  height: 1.5rem;
}

.modal-body {
  padding: 1.5rem;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.form-field {
  display: flex;
  flex-direction: column;
}

.form-field.full-width {
  grid-column: 1 / -1;
}

.form-field span {
  font-size: 0.875rem;
  font-weight: 500;
  color: #374151;
  margin-bottom: 0.5rem;
}

.form-field input,
.form-field select,
.form-field textarea {
  padding: 0.75rem;
  border: 1px solid #d1d5db;
  border-radius: 0.375rem;
  font-size: 0.875rem;
}

.form-field input:focus,
.form-field select:focus,
.form-field textarea:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.form-message {
  margin: 0 0 1rem;
  color: #b91c1c;
  font-size: 0.875rem;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
  padding-top: 1.5rem;
  border-top: 1px solid #e5e7eb;
}

.btn {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 0.375rem;
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-primary {
  background: #3b82f6;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #2563eb;
}

.btn-secondary {
  background: #f3f4f6;
  color: #374151;
}

.btn-secondary:hover:not(:disabled) {
  background: #e5e7eb;
}

.btn-danger {
  background: #dc2626;
  color: #ffffff;
}

.btn-danger:hover:not(:disabled) {
  background: #b91c1c;
}

.delete-copy {
  margin: 0;
  color: #111827;
  line-height: 1.7;
}

.delete-hint {
  margin: 0.75rem 0 0;
  color: #6b7280;
  font-size: 0.875rem;
  line-height: 1.6;
}

.loading-text {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.loading-icon {
  width: 1rem;
  height: 1rem;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.icon {
  width: 1.25rem;
  height: 1.25rem;
}

/* 分页组件样式 */
.pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1rem 1.5rem;
  flex-wrap: wrap;
  margin-top: 1.5rem;
  padding: 1rem;
  background: white;
  border-radius: 0.5rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.pagination-summary {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem 1rem;
  color: #6b7280;
  font-size: 0.875rem;
}

.pagination-controls {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.btn-outline {
  background: white;
  color: #374151;
  border: 1px solid #d1d5db;
}

.btn-outline:hover:not(:disabled) {
  background: #f9fafb;
  border-color: #9ca3af;
}

.page-number {
  min-width: 2.25rem;
  justify-content: center;
  background: white;
  color: #374151;
  border: 1px solid #d1d5db;
}

.page-number.active {
  background: #3b82f6;
  color: white;
  border-color: #3b82f6;
}

.pagination--empty {
  justify-content: center;
  color: #6b7280;
}
</style>
