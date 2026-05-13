<template>
  <div class="product-management">
    <div class="page-header">
      <div>
        <h1>库存产品</h1>
        <p>维护仓库里真实入库、出库和计成本的库存产品资料。</p>
      </div>
    </div>

    <div class="product-workbench">
      <aside class="category-sidebar">
        <div class="category-sidebar__header">
          <h2>分类管理</h2>
          <button class="category-add-btn" type="button" @click="openCategoryModal()">
            <PlusIcon class="icon" />
            新增分类
          </button>
        </div>

        <div class="category-search">
          <MagnifyingGlassIcon class="category-search__icon" />
          <input v-model="categoryKeyword" type="text" placeholder="搜索分类名称" />
        </div>

        <div class="category-tree" :class="{ 'is-empty': visibleCategories.length === 0 }">
          <button
            v-for="category in visibleCategories"
            :key="category.id"
            class="category-tree__item"
            :class="{ active: selectedCategory === category.name }"
            type="button"
            @click="selectedCategory = category.name"
          >
            <span class="tree-folder"></span>
            <span class="tree-name">{{ category.name }}</span>
            <span class="tree-count">{{ categoryProductCount(category.name) }}</span>
          </button>
        </div>
      </aside>

      <section class="product-list-card">
        <div class="product-list-card__header">
          <h2>库存产品列表</h2>
        </div>

        <div class="product-toolbar">
          <div class="search-box product-search-box">
            <input
              type="text"
              v-model="searchQuery"
              placeholder="搜索库存产品名称、编码或规格"
              @input="handleSearch()"
            />
            <MagnifyingGlassIcon class="search-icon" />
          </div>
          <label class="toolbar-field">
            <span>类型</span>
            <select v-model="productTypeFilter">
              <option value="">全部</option>
              <option value="FINISHED_PRODUCT">成品</option>
              <option value="SEMI_FINISHED_PRODUCT">半成品</option>
              <option value="RAW_MATERIAL">原料</option>
              <option value="PACKAGING_MATERIAL">包装物料</option>
            </select>
          </label>
          <label class="toolbar-field">
            <span>分类</span>
            <select v-model="selectedCategory">
              <option value="">全部</option>
              <option v-for="category in visibleCategories" :key="`filter-${category.id}`" :value="category.name">{{ category.name }}</option>
            </select>
          </label>
          <button class="btn btn-outline" type="button" @click="resetProductFilters">重置</button>
          <button class="btn btn-primary product-create-btn" type="button" @click="openCreateModal">
            <PlusIcon class="icon" />
            新增库存产品
          </button>
        </div>

        <div class="products-table products-table--inside-card">
          <table>
            <thead>
              <tr>
                <th>库存产品编码</th>
                <th>库存产品名称</th>
                <th>类型</th>
                <th>分类</th>
                <th>规格型号</th>
                <th>计量单位</th>
                <th>预警库存</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="product in filteredProducts" :key="product.id">
                <td>{{ product.sku }}</td>
                <td>{{ product.name }}</td>
                <td>{{ productTypeLabel(product.type) }}</td>
                <td>{{ product.category || '-' }}</td>
                <td>{{ product.specification || '-' }}</td>
                <td>{{ product.unit }}</td>
                <td>{{ product.alertQuantity ?? 0 }}</td>
                <td>
                  <div class="table-actions">
                    <button class="btn btn-sm btn-secondary table-action-edit" @click="editProduct(product)">编辑</button>
                    <button class="btn btn-sm btn-danger table-action-delete" @click="openDeleteModal(product)">删除</button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="pagination" v-if="totalElements > 0">
          <div class="pagination-summary">
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
      </section>
    </div>

    <!-- 创建/编辑分类模态框 -->
    <Teleport to="body">
      <div v-if="showCategoryModal" class="modal-overlay" @click="closeCategoryModal">
        <div class="modal-content modal-content--small" @click.stop>
          <header class="modal-header">
            <h2>{{ categoryEditing ? '编辑分类' : '新增分类' }}</h2>
            <button type="button" class="modal-close" @click="closeCategoryModal">
              <XMarkIcon class="close-icon" />
            </button>
          </header>

          <form class="modal-body" @submit.prevent="handleCategorySubmit">
            <label class="form-field">
              <span>分类名称 *</span>
              <input v-model="categoryForm.name" type="text" required placeholder="例如：粽子、原料、包装" />
            </label>
            <label class="form-field">
              <span>适用类型</span>
              <select v-model="categoryForm.type">
                <option value="">通用</option>
                <option value="FINISHED_PRODUCT">成品</option>
                <option value="SEMI_FINISHED_PRODUCT">半成品</option>
                <option value="RAW_MATERIAL">原料</option>
                <option value="PACKAGING_MATERIAL">包装物料</option>
              </select>
            </label>
            <label class="form-field">
              <span>排序</span>
              <input v-model.number="categoryForm.sortOrder" type="number" min="0" step="1" />
            </label>
            <label class="checkbox-field checkbox-field--modal">
              <input v-model="categoryForm.enabled" type="checkbox" />
              <span>启用分类</span>
            </label>
            <label class="form-field">
              <span>备注</span>
              <textarea v-model="categoryForm.remark" rows="3" placeholder="可选"></textarea>
            </label>

            <p v-if="categoryMessage" class="form-message">{{ categoryMessage }}</p>

            <div class="modal-actions">
              <button type="button" class="btn btn-secondary" @click="closeCategoryModal">取消</button>
              <button type="submit" class="btn btn-primary" :disabled="categorySubmitting">
                {{ categorySubmitting ? '保存中...' : '保存分类' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </Teleport>

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
                  <option value="FINISHED_PRODUCT">成品</option>
                  <option value="SEMI_FINISHED_PRODUCT">半成品</option>
                  <option value="RAW_MATERIAL">原料</option>
                  <option value="PACKAGING_MATERIAL">包装物料</option>
                </select>
              </label>

              <label class="form-field">
                <span>产品类别</span>
                <select v-model.number="createForm.categoryId">
                  <option value="">请选择分类</option>
                  <option v-for="category in filteredCategoryOptions(createForm.type)" :key="`create-${category.id}`" :value="category.id">
                    {{ category.name }}
                  </option>
                </select>
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
                  <option value="FINISHED_PRODUCT">成品</option>
                  <option value="SEMI_FINISHED_PRODUCT">半成品</option>
                  <option value="RAW_MATERIAL">原料</option>
                  <option value="PACKAGING_MATERIAL">包装物料</option>
                </select>
              </label>

              <label class="form-field">
                <span>产品类别</span>
                <select v-model.number="editForm.categoryId">
                  <option value="">请选择分类</option>
                  <option v-for="category in filteredCategoryOptions(editForm.type)" :key="`edit-${category.id}`" :value="category.id">
                    {{ category.name }}
                  </option>
                </select>
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
import { productCategoryApi, type ProductCategory } from '@/api/productCategory'

const route = useRoute()
const products = ref<Product[]>([])
const categories = ref<ProductCategory[]>([])
const searchQuery = ref('')
const categoryKeyword = ref('')
const selectedCategory = ref('')
const productTypeFilter = ref('')
const showCategoryModal = ref(false)
const showCreateModal = ref(false)
const showEditModal = ref(false)
const showDeleteModal = ref(false)
const categoryLoading = ref(false)
const categorySubmitting = ref(false)
const loading = ref(false)
const deleteLoading = ref(false)
const categoryMessage = ref('')
const formMessage = ref('')
const deleteMessage = ref('')
const categoryEditing = ref<ProductCategory | null>(null)
const editingProduct = ref<Product | null>(null)
const deletingProduct = ref<Product | null>(null)
const currentPage = ref(0)
const pageSize = ref(10)
const totalElements = ref(0)
const totalPages = ref(0)
const categoryOptions = ref<ProductCategory[]>([])
const categoryForm = ref({
  name: '',
  type: '' as '' | 'FINISHED_PRODUCT' | 'RAW_MATERIAL',
  sortOrder: 0,
  enabled: true,
  remark: ''
})

const displayTotalPages = computed(() => Math.max(totalPages.value, 1))

const normalizedCategories = computed(() => {
  return categories.value
})

const visibleCategories = computed(() => normalizedCategories.value.filter(category => {
  const matchesKeyword = !categoryKeyword.value.trim() || category.name.includes(categoryKeyword.value.trim())
  return matchesKeyword
}))

const filteredProducts = computed(() => products.value.filter(product => {
  const matchesCategory = !selectedCategory.value || product.category === selectedCategory.value
  const matchesType = !productTypeFilter.value || product.type === productTypeFilter.value
  return matchesCategory && matchesType
}))

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
  categoryId: undefined,
  unit: ''
})

const editForm = ref<ProductRequest>({
  code: '',
  name: '',
  type: '',
  categoryId: undefined,
  unit: '',
  specification: '',
  costPrice: undefined,
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

const loadCategories = async () => {
  categoryLoading.value = true
  try {
    categories.value = await productCategoryApi.getCategories(undefined, true)
  } catch (error) {
    console.error('加载分类列表失败:', error)
  } finally {
    categoryLoading.value = false
  }
}

const loadCategoryOptions = async (type?: string) => {
  try {
    categoryOptions.value = await productCategoryApi.getCategories(type || undefined, true)
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

const openCategoryModal = (category?: ProductCategory) => {
  categoryEditing.value = category ?? null
  categoryMessage.value = ''
  categoryForm.value = category
    ? {
        name: category.name,
        type: category.type ?? '',
        sortOrder: category.sortOrder,
        enabled: category.enabled,
        remark: category.remark || ''
      }
    : {
        name: '',
        type: '',
        sortOrder: 0,
        enabled: true,
        remark: ''
      }
  showCategoryModal.value = true
}

const closeCategoryModal = () => {
  if (categorySubmitting.value) return
  showCategoryModal.value = false
  categoryEditing.value = null
  categoryMessage.value = ''
}

const handleCategorySubmit = async () => {
  categorySubmitting.value = true
  categoryMessage.value = ''
  try {
    const payload = {
      name: categoryForm.value.name,
      type: categoryForm.value.type || undefined,
      sortOrder: categoryForm.value.sortOrder,
      enabled: categoryForm.value.enabled,
      remark: categoryForm.value.remark || undefined
    }
    if (categoryEditing.value) {
      await productCategoryApi.updateCategory(categoryEditing.value.id, payload)
    } else {
      await productCategoryApi.createCategory(payload)
    }
    closeCategoryModal()
    await Promise.all([loadCategories(), loadCategoryOptions()])
  } catch (error) {
    console.error('保存分类失败:', error)
    categoryMessage.value = error instanceof Error ? error.message : '保存分类失败，请稍后重试。'
  } finally {
    categorySubmitting.value = false
  }
}

const openCreateModal = async () => {
  showCreateModal.value = true
  await loadCategoryOptions(createForm.value.type)
}

const handleCreateProduct = async () => {
  loading.value = true
  formMessage.value = ''
  try {
    await productApi.createProduct(normalizeProductPayload(createForm.value))
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
    categoryId: undefined,
    unit: ''
  }
}

const editProduct = async (product: Product) => {
  await loadCategoryOptions(product.type)
  editingProduct.value = product
  editForm.value = {
    code: product.sku,
    name: product.name,
    type: product.type,
    categoryId: product.categoryId,
    unit: product.unit,
    specification: product.specification || '',
    costPrice: product.costPrice,
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
    await productApi.updateProduct(editingProduct.value.id, normalizeProductPayload(editForm.value))
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

function filteredCategoryOptions(type: string) {
  if (!type) return categoryOptions.value
  return categoryOptions.value.filter(category => !category.type || category.type === type)
}

function normalizeProductPayload(payload: ProductRequest): ProductRequest {
  return {
    ...payload,
    categoryId: typeof payload.categoryId === 'number' && payload.categoryId > 0 ? payload.categoryId : undefined,
    category: undefined
  }
}

function categoryProductCount(categoryName: string) {
  return products.value.filter(product => product.category === categoryName).length
}

function resetProductFilters() {
  selectedCategory.value = ''
  productTypeFilter.value = ''
  searchQuery.value = ''
  handleSearch(0)
}

function productTypeLabel(type: Product['type']) {
  switch (type) {
    case 'FINISHED_PRODUCT': return '成品'
    case 'SEMI_FINISHED_PRODUCT': return '半成品'
    case 'RAW_MATERIAL': return '原料'
    case 'PACKAGING_MATERIAL': return '包装物料'
    default: return type
  }
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

watch(
  () => createForm.value.type,
  async (type) => {
    createForm.value.categoryId = undefined
    await loadCategoryOptions(type)
  }
)

watch(
  () => editForm.value.type,
  async (type) => {
    if (!showEditModal.value) {
      return
    }
    editForm.value.categoryId = undefined
    await loadCategoryOptions(type)
  }
)

onMounted(() => {
  loadCategories()
  loadCategoryOptions()
  applyRouteSearch()
})
</script>

<style scoped>
.product-management {
  padding: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 14px;
}

.page-header h1 {
  margin: 0;
  color: #0f172a;
  font-size: 20px;
  line-height: 1.2;
}

.page-header p {
  margin: 6px 0 0;
  color: #64748b;
  font-size: 13px;
  line-height: 1.5;
}

.filters-section {
  margin-bottom: 14px;
}

.product-workbench {
  display: grid;
  grid-template-columns: 220px minmax(0, 1fr);
  gap: 14px;
  align-items: stretch;
  min-height: calc(100vh - 190px);
}

.category-sidebar,
.product-list-card {
  background: #ffffff;
  border: 1px solid #e9eef6;
  border-radius: 14px;
  box-shadow: 0 6px 18px rgba(15, 23, 42, 0.04);
}

.category-sidebar {
  min-height: 100%;
  padding: 14px 10px 12px;
  display: flex;
  flex-direction: column;
}

.category-sidebar__header,
.product-list-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.category-add-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 34px;
  padding: 0 11px;
  border: 1px solid #bfdbfe;
  border-radius: 10px;
  background: #eff6ff;
  color: #2563eb;
  font-size: 12px;
  font-weight: 800;
  cursor: pointer;
  transition: background 0.2s ease, border-color 0.2s ease, box-shadow 0.2s ease;
}

.category-add-btn:hover {
  border-color: #93c5fd;
  background: #dbeafe;
  box-shadow: 0 6px 14px rgba(37, 99, 235, 0.12);
}

.category-sidebar__header h2,
.product-list-card__header h2 {
  margin: 0;
  color: #0f172a;
  font-size: 16px;
  line-height: 1.3;
}

.category-search {
  position: relative;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.category-search input {
  flex: 1;
  min-width: 0;
  width: 100%;
  height: 38px;
  padding: 0 12px 0 36px;
  border: 1px solid #dbe3ef;
  border-radius: 10px;
  background: #fbfcfe;
  color: #0f172a;
  font-size: 13px;
}

.category-search__icon {
  position: absolute;
  left: 12px;
  top: 50%;
  width: 16px;
  height: 16px;
  color: #94a3b8;
  transform: translateY(-50%);
}

.category-tree {
  display: grid;
  align-content: start;
  gap: 6px;
  flex: 1;
  min-height: 360px;
  padding: 10px 8px;
  border-radius: 10px;
  background: #edf5ff;
  overflow-y: auto;
}

.category-tree__item {
  display: grid;
  grid-template-columns: 8px minmax(0, 1fr) auto;
  align-items: center;
  gap: 9px;
  width: 100%;
  padding: 9px 8px;
  border: none;
  border-radius: 8px;
  background: transparent;
  color: #4b5563;
  font-size: 13px;
  text-align: left;
  cursor: pointer;
}

.category-tree__item:hover {
  background: rgba(255, 255, 255, 0.55);
}

.category-tree__item.active {
  background: #ffffff;
  color: #2563eb;
  box-shadow: 0 2px 8px rgba(37, 99, 235, 0.08);
}

.tree-folder {
  width: 6px;
  height: 6px;
  border-radius: 999px;
  background: currentColor;
  opacity: 0.45;
}

.tree-name {
  min-width: 0;
  font-weight: 700;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.tree-count {
  min-width: 28px;
  height: 22px;
  padding: 0 8px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.7);
  color: #64748b;
  font-variant-numeric: tabular-nums;
  font-feature-settings: "tnum";
  font-size: 12px;
  font-weight: 800;
  line-height: 22px;
  text-align: right;
}

.category-tree__item.active .tree-count {
  background: #eff6ff;
  color: #2563eb;
}

.product-list-card {
  padding: 14px 14px 10px;
  min-width: 0;
  min-height: 100%;
  display: flex;
  flex-direction: column;
}

.product-toolbar {
  display: grid;
  grid-template-columns: minmax(280px, 1fr) minmax(122px, 150px) minmax(122px, 150px) 68px 116px;
  gap: 10px;
  align-items: center;
  margin-bottom: 12px;
}

.product-toolbar label {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr);
  align-items: center;
  gap: 8px;
  color: #64748b;
  font-size: 13px;
}

.product-toolbar select {
  width: 100%;
  height: 38px;
  padding: 0 10px;
  border: 1px solid #dbe3ef;
  border-radius: 10px;
  background: #ffffff;
  color: #334155;
}

.product-search-box {
  max-width: none;
}

.products-table--inside-card {
  flex: 1;
  border-radius: 12px;
  box-shadow: none;
}

.products-table--inside-card table {
  min-width: 850px;
}

.panel-section {
  margin-bottom: 16px;
  padding: 14px 16px 16px;
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.06);
}

.panel-header-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 10px;
}

.panel-header-row--compact {
  margin-bottom: 10px;
}

.panel-header-row h2 {
  margin: 0;
  color: #0f172a;
  font-size: 16px;
  line-height: 1.3;
}

.panel-header-row p {
  margin: 6px 0 0;
  color: #64748b;
  font-size: 13px;
  line-height: 1.5;
}

.category-filters-inline {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
  padding: 10px 12px;
  background: #f8fafc;
  border: 1px solid #edf2f7;
  border-radius: 10px;
}

.category-filters-inline label:not(.checkbox-field) {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: #334155;
  font-size: 13px;
  font-weight: 700;
}

.category-filters-inline select {
  min-width: 170px;
  height: 36px;
  padding: 0 10px;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  background: #ffffff;
  color: #0f172a;
  font-size: 13px;
}

.checkbox-field {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: #334155;
  font-size: 13px;
  font-weight: 700;
}

.checkbox-field input {
  width: 16px;
  height: 16px;
  accent-color: #3b82f6;
}

.checkbox-field--modal {
  margin: 4px 0 12px;
}

.products-table--compact {
  margin-top: 10px;
}

.products-table--compact table {
  min-width: 760px;
}

.empty-cell {
  padding: 28px 16px;
  text-align: center;
  color: #94a3b8;
}

.status-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 48px;
  height: 24px;
  padding: 0 8px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
}

.status-active {
  color: #047857;
  background: #ecfdf5;
}

.status-disabled {
  color: #64748b;
  background: #f1f5f9;
}

.search-box {
  position: relative;
  max-width: 400px;
}

.search-box input {
  width: 100%;
  padding: 9px 12px 9px 38px;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  color: #0f172a;
  font-size: 14px;
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
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.06);
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
  padding: 11px 14px;
  text-align: left;
  border-bottom: 1px solid #edf2f7;
  color: #334155;
  font-size: 13px;
}

.category-panel th,
.category-panel td {
  padding-top: 10px;
  padding-bottom: 10px;
}

th {
  font-weight: 700;
  color: #475569;
}

.btn-sm {
  padding: 6px 10px;
  font-size: 12px;
}

.table-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.table-action-edit,
.table-action-delete {
  min-width: 54px;
  justify-content: center;
  border-radius: 999px;
  padding: 5px 12px;
  font-size: 12px;
  line-height: 1.1;
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
  border-radius: 8px;
  box-shadow: 0 24px 60px rgba(15, 23, 42, 0.22);
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
  padding: 20px 22px;
  border-bottom: 1px solid #e2e8f0;
}

.modal-header h2 {
  margin: 0;
  color: #0f172a;
  font-size: 20px;
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
  padding: 20px 22px;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin-bottom: 18px;
}

.form-field {
  display: flex;
  flex-direction: column;
}

.form-field.full-width {
  grid-column: 1 / -1;
}

.form-field span {
  margin-bottom: 6px;
  color: #334155;
  font-size: 13px;
  font-weight: 700;
}

.form-field input,
.form-field select,
.form-field textarea {
  padding: 9px 11px;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  color: #0f172a;
  font-size: 14px;
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
  gap: 10px;
  padding-top: 16px;
  border-top: 1px solid #e2e8f0;
}

.btn {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 9px 14px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-sm {
  padding: 6px 10px;
  font-size: 12px;
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

.product-create-btn {
  height: 38px;
  justify-content: center;
  padding: 0 14px;
  border-radius: 10px;
  white-space: nowrap;
}

.btn-secondary,
.btn-soft {
  background: #f8fafc;
  color: #334155;
  border: 1px solid #e2e8f0;
}

.btn-secondary:hover:not(:disabled),
.btn-soft:hover:not(:disabled) {
  background: #eef4ff;
  border-color: #bfdbfe;
  color: #2563eb;
}

.btn-danger {
  background: #dc2626;
  color: #ffffff;
}

.btn-danger:hover:not(:disabled) {
  background: #b91c1c;
}

.table-action-delete {
  border: 1px solid #fecaca;
  background: #fef2f2;
  color: #dc2626;
}

.table-action-delete:hover:not(:disabled) {
  border-color: #fca5a5;
  background: #fee2e2;
  color: #b91c1c;
  box-shadow: 0 4px 10px rgba(220, 38, 38, 0.12);
}

.table-action-edit {
  border: 1px solid #dbeafe;
  background: #eff6ff;
  color: #2563eb;
}

.table-action-edit:hover:not(:disabled) {
  border-color: #bfdbfe;
  background: #dbeafe;
  color: #1d4ed8;
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

@media (max-width: 1024px) {
  .product-workbench {
    grid-template-columns: 1fr;
  }

  .product-toolbar {
    grid-template-columns: 1fr 1fr;
  }

  .product-search-box {
    grid-column: 1 / -1;
  }
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: stretch;
  }

  .product-toolbar {
    grid-template-columns: 1fr;
  }
}

.icon {
  width: 1.05rem;
  height: 1.05rem;
}

/* 分页组件样式 */
.pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1rem 1.5rem;
  flex-wrap: wrap;
  margin-top: 14px;
  padding: 14px 16px;
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.06);
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
