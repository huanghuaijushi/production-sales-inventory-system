<template>
  <div class="product-management">
    <section class="category-card">
      <header class="category-card__header">
        <div>
          <h1>分类管理</h1>
          <p>{{ visibleCategories.length }} 个分类</p>
        </div>
        <button class="btn btn-primary" @click="openModal()">
          <PlusIcon class="icon" />
          新增分类
        </button>
      </header>

      <div class="category-toolbar">
        <label class="search-field" aria-label="搜索分类名称">
          <MagnifyingGlassIcon class="search-icon" />
          <input v-model.trim="searchKeyword" type="search" placeholder="搜索分类名称" />
        </label>
      </div>

      <div class="category-tree">
        <div v-if="loading" class="tree-state">正在加载分类...</div>
        <template v-else>
          <div v-for="group in categoryGroups" :key="group.key" class="tree-group">
            <button
              class="tree-row tree-row--group"
              :class="{ 'is-selected': selectedNode === `group:${group.key}` }"
              @click="toggleGroup(group.key)"
            >
              <ChevronRightIcon class="tree-caret" :class="{ 'is-open': isGroupOpen(group.key) }" />
              <FolderIcon class="tree-folder" />
              <span class="tree-name">{{ group.label }}</span>
              <span class="tree-count">{{ group.items.length }}</span>
            </button>

            <div v-if="isGroupOpen(group.key)" class="tree-children">
              <div
                v-for="category in group.items"
                :key="category.id"
                class="tree-row tree-row--leaf"
                :class="{
                  'is-selected': selectedNode === `category:${category.id}`,
                  'is-disabled': !category.enabled
                }"
                role="button"
                tabindex="0"
                @click="selectNode(`category:${category.id}`)"
                @keydown.enter.prevent="selectNode(`category:${category.id}`)"
              >
                <FolderIcon class="tree-folder tree-folder--leaf" />
                <div class="tree-leaf-main">
                  <span class="tree-name">{{ category.name }}</span>
                  <span class="tree-meta">{{ category.typeText || '通用分类' }} · 排序 {{ category.sortOrder }}</span>
                </div>
                <span class="status-dot" :class="category.enabled ? 'status-dot--active' : 'status-dot--disabled'"></span>
                <button class="row-action" type="button" title="编辑分类" @click.stop="openModal(category)">
                  <PencilSquareIcon class="row-action-icon" />
                </button>
              </div>
            </div>
          </div>

          <p v-if="visibleCategories.length === 0" class="tree-empty tree-empty--card">没有匹配的分类</p>
        </template>
      </div>

      <footer class="category-tip">
        <InformationCircleIcon class="tip-icon" />
        <span>选择分类以筛选右侧产品列表</span>
      </footer>
    </section>

    <Teleport to="body">
      <div v-if="modalOpen" class="modal-overlay" @click="closeModal">
        <div class="modal-content modal-content--small" @click.stop>
          <header class="modal-header">
            <h2>{{ editingCategory ? '编辑分类' : '新增分类' }}</h2>
            <button type="button" class="modal-close" @click="closeModal">
              <XMarkIcon class="close-icon" />
            </button>
          </header>

          <form class="modal-body" @submit.prevent="submitCategory">
            <label class="form-field">
              <span>分类名称 *</span>
              <input v-model="form.name" type="text" required placeholder="例如：粽子、原料、包装" />
            </label>
            <label class="form-field">
              <span>适用类型</span>
              <select v-model="form.type">
                <option value="">通用</option>
                <option value="FINISHED_PRODUCT">成品 / 半成品</option>
                <option value="RAW_MATERIAL">原料 / 包装</option>
              </select>
            </label>
            <label class="form-field">
              <span>排序</span>
              <input v-model.number="form.sortOrder" type="number" min="0" step="1" />
            </label>
            <label class="checkbox-field checkbox-field--modal">
              <input v-model="form.enabled" type="checkbox" />
              <span>启用分类</span>
            </label>
            <label class="form-field">
              <span>备注</span>
              <textarea v-model="form.remark" rows="3" placeholder="可选"></textarea>
            </label>

            <p v-if="message" class="form-message">{{ message }}</p>

            <div class="modal-actions">
              <button type="button" class="btn btn-secondary" @click="closeModal">取消</button>
              <button type="submit" class="btn btn-primary" :disabled="submitting">{{ submitting ? '保存中...' : '保存分类' }}</button>
            </div>
          </form>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import {
  ChevronRightIcon,
  FolderIcon,
  InformationCircleIcon,
  MagnifyingGlassIcon,
  PencilSquareIcon,
  PlusIcon,
  XMarkIcon
} from '@heroicons/vue/24/outline'
import { productCategoryApi, type ProductCategory } from '@/api/productCategory'

type CategoryGroupKey = 'FINISHED_PRODUCT' | 'RAW_MATERIAL' | 'COMMON'

const categories = ref<ProductCategory[]>([])
const loading = ref(false)
const submitting = ref(false)
const modalOpen = ref(false)
const searchKeyword = ref('')
const selectedNode = ref('all')
const openGroups = reactive<Record<CategoryGroupKey, boolean>>({
  FINISHED_PRODUCT: true,
  RAW_MATERIAL: true,
  COMMON: true
})
const message = ref('')
const editingCategory = ref<ProductCategory | null>(null)

const form = reactive({
  name: '',
  type: '' as '' | 'FINISHED_PRODUCT' | 'RAW_MATERIAL',
  sortOrder: 0,
  enabled: true,
  remark: ''
})

const visibleCategories = computed(() => {
  const keyword = searchKeyword.value.trim().toLowerCase()
  return categories.value.filter(category => {
    const matchesKeyword = keyword.length === 0
      || category.name.toLowerCase().includes(keyword)
      || (category.remark ?? '').toLowerCase().includes(keyword)
    return matchesKeyword
  })
})

const categoryGroups = computed(() => {
  return [
    {
      key: 'FINISHED_PRODUCT' as const,
      label: '成品 / 半成品',
      items: visibleCategories.value.filter(category => getCategoryGroupKey(category) === 'FINISHED_PRODUCT')
    },
    {
      key: 'RAW_MATERIAL' as const,
      label: '原料 / 包装',
      items: visibleCategories.value.filter(category => getCategoryGroupKey(category) === 'RAW_MATERIAL')
    },
    {
      key: 'COMMON' as const,
      label: '通用分类',
      items: visibleCategories.value.filter(category => getCategoryGroupKey(category) === 'COMMON')
    }
  ].filter(group => group.items.length > 0)
})

async function loadCategories() {
  loading.value = true
  try {
    categories.value = await productCategoryApi.getCategories()
  } catch (error) {
    message.value = error instanceof Error ? error.message : '加载分类失败'
  } finally {
    loading.value = false
  }
}

function openModal(category?: ProductCategory) {
  editingCategory.value = category || null
  message.value = ''
  form.name = category?.name || ''
  form.type = category?.type || ''
  form.sortOrder = category?.sortOrder || 0
  form.enabled = category?.enabled ?? true
  form.remark = category?.remark || ''
  modalOpen.value = true
}

function closeModal() {
  modalOpen.value = false
  editingCategory.value = null
  message.value = ''
}

async function submitCategory() {
  submitting.value = true
  message.value = ''
  const payload = {
    name: form.name,
    type: form.type || undefined,
    sortOrder: form.sortOrder,
    enabled: form.enabled,
    remark: form.remark || undefined
  }
  try {
    if (editingCategory.value) {
      await productCategoryApi.updateCategory(editingCategory.value.id, payload)
    } else {
      await productCategoryApi.createCategory(payload)
    }
    closeModal()
    await loadCategories()
  } catch (error) {
    message.value = error instanceof Error ? error.message : '保存分类失败'
  } finally {
    submitting.value = false
  }
}

function getCategoryGroupKey(category: ProductCategory): CategoryGroupKey {
  if (category.type === 'FINISHED_PRODUCT') {
    return 'FINISHED_PRODUCT'
  }
  if (category.type === 'RAW_MATERIAL') {
    return 'RAW_MATERIAL'
  }
  return 'COMMON'
}

function isGroupOpen(groupKey: CategoryGroupKey) {
  return openGroups[groupKey]
}

function toggleGroup(groupKey: CategoryGroupKey) {
  selectedNode.value = `group:${groupKey}`
  openGroups[groupKey] = !openGroups[groupKey]
}

function selectNode(node: string) {
  selectedNode.value = node
}

onMounted(loadCategories)
</script>

<style scoped>
.product-management {
  display: flex;
  align-items: flex-start;
  width: 100%;
  min-height: calc(100vh - 132px);
  padding: 0;
}

.category-card {
  display: flex;
  flex-direction: column;
  width: min(100%, 420px);
  min-height: calc(100vh - 150px);
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.08);
  overflow: hidden;
}

.category-card__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 20px 22px 16px;
}

.category-card__header h1 {
  margin: 0;
  color: #0f172a;
  font-size: 18px;
  font-weight: 800;
  line-height: 1.2;
}

.category-card__header p {
  margin: 8px 0 0;
  color: #64748b;
  font-size: 13px;
}

.category-toolbar {
  display: grid;
  gap: 12px;
  padding: 0 22px 16px;
}

.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.2s ease;
}

.btn-primary {
  background: #2563eb;
  color: white;
  padding: 9px 13px;
  border: 1px solid #2563eb;
  box-shadow: 0 8px 16px rgba(37, 99, 235, 0.18);
}

.btn-secondary {
  background: #f1f5f9;
  color: #334155;
  padding: 9px 14px;
}

.btn-sm {
  padding: 6px 10px;
  font-size: 12px;
}

.search-field {
  position: relative;
  display: flex;
  align-items: center;
}

.search-field input {
  width: 100%;
  height: 44px;
  border: 1px solid #d7e0ec;
  border-radius: 8px;
  background: white;
  color: #0f172a;
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.search-field input {
  padding: 0 14px 0 42px;
}

.search-field input:focus {
  border-color: #93c5fd;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.12);
}

.search-icon {
  position: absolute;
  width: 18px;
  height: 18px;
  color: #94a3b8;
  pointer-events: none;
}

.search-icon {
  left: 14px;
}

.category-tree {
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  flex: 1;
  min-height: 420px;
  margin: 0 16px;
  padding: 12px 8px;
  border-radius: 12px;
  background: #eef5ff;
  overflow-y: auto;
}

.tree-state,
.tree-empty {
  margin: 0;
  padding: 16px 12px;
  color: #94a3b8;
  font-size: 13px;
  text-align: center;
}

.tree-empty--card {
  margin-top: 12px;
}

.tree-group + .tree-group {
  margin-top: 4px;
}

.tree-row {
  display: flex;
  align-items: center;
  width: 100%;
  min-height: 42px;
  border: 1px solid transparent;
  border-radius: 9px;
  background: transparent;
  color: #334155;
  text-align: left;
  transition: background 0.2s ease, border-color 0.2s ease, color 0.2s ease, box-shadow 0.2s ease;
}

button.tree-row {
  cursor: pointer;
  padding: 0 12px;
}

.tree-row:hover {
  background: rgba(255, 255, 255, 0.72);
}

.tree-row.is-selected {
  border-color: #bfdbfe;
  background: white;
  color: #2563eb;
  box-shadow: 0 8px 18px rgba(37, 99, 235, 0.12);
}

.tree-row--group {
  font-weight: 700;
}

.tree-row--leaf {
  min-height: 56px;
  margin-top: 6px;
  padding: 8px 10px 8px 36px;
  cursor: pointer;
}

.tree-row--leaf.is-disabled {
  color: #94a3b8;
}

.tree-row--leaf:hover .row-action,
.tree-row--leaf.is-selected .row-action {
  opacity: 1;
  transform: translateX(0);
}

.tree-children {
  position: relative;
  margin-left: 20px;
  padding: 2px 0 6px 12px;
}

.tree-children::before {
  content: "";
  position: absolute;
  left: 4px;
  top: 0;
  bottom: 12px;
  width: 1px;
  background: #d7e0ec;
}

.tree-caret {
  flex: 0 0 auto;
  width: 15px;
  height: 15px;
  margin-right: 8px;
  color: #64748b;
  transition: transform 0.2s ease, color 0.2s ease;
}

.tree-caret.is-open {
  color: #2563eb;
  transform: rotate(90deg);
}

.tree-folder {
  flex: 0 0 auto;
  width: 19px;
  height: 19px;
  margin-right: 10px;
  color: #64748b;
}

.tree-row.is-selected .tree-folder,
.tree-row.is-selected .tree-caret {
  color: #2563eb;
}

.tree-folder--leaf {
  width: 18px;
  height: 18px;
}

.tree-name {
  flex: 1;
  min-width: 0;
  font-size: 14px;
  font-weight: 700;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.tree-count {
  margin-left: 12px;
  color: #64748b;
  font-size: 14px;
  font-weight: 700;
}

.tree-row.is-selected .tree-count {
  color: #2563eb;
}

.tree-leaf-main {
  display: grid;
  flex: 1;
  min-width: 0;
  gap: 3px;
}

.tree-meta {
  color: #64748b;
  font-size: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.status-dot {
  flex: 0 0 auto;
  width: 8px;
  height: 8px;
  margin: 0 10px;
  border-radius: 999px;
}

.status-dot--active {
  background: #22c55e;
}

.status-dot--disabled {
  background: #cbd5e1;
}

.row-action {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 auto;
  width: 30px;
  height: 30px;
  border: 1px solid #dbeafe;
  border-radius: 8px;
  background: #f8fbff;
  color: #2563eb;
  cursor: pointer;
  opacity: 0;
  transform: translateX(4px);
  transition: opacity 0.2s ease, transform 0.2s ease, background 0.2s ease;
}

.row-action:hover {
  background: #eff6ff;
}

.row-action-icon {
  width: 16px;
  height: 16px;
}

.category-tip {
  display: flex;
  align-items: center;
  gap: 10px;
  min-height: 56px;
  margin-top: 16px;
  padding: 14px 20px;
  border-top: 1px solid #e2e8f0;
  background: #f8fbff;
  color: #64748b;
  font-size: 13px;
  font-weight: 600;
}

.tip-icon {
  flex: 0 0 auto;
  width: 18px;
  height: 18px;
  color: #2563eb;
}

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.45);
  display: grid;
  place-items: center;
  padding: 16px;
  z-index: 50;
}

.modal-content {
  width: min(100%, 520px);
  background: white;
  border-radius: 16px;
  box-shadow: 0 24px 60px rgba(15, 23, 42, 0.24);
  overflow: hidden;
}

.modal-content--small {
  width: min(100%, 460px);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 18px 20px;
  border-bottom: 1px solid #e2e8f0;
}

.modal-header h2 {
  margin: 0;
  font-size: 18px;
  color: #0f172a;
}

.modal-close {
  border: none;
  background: transparent;
  cursor: pointer;
  color: #64748b;
}

.close-icon {
  width: 20px;
  height: 20px;
}

.modal-body {
  display: grid;
  gap: 14px;
  padding: 20px;
}

.form-field {
  display: grid;
  gap: 6px;
}

.form-field span {
  color: #475569;
  font-size: 13px;
  font-weight: 700;
}

.form-field input,
.form-field select,
.form-field textarea {
  padding: 10px 12px;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  color: #0f172a;
  font-size: 14px;
}

.form-message {
  margin: 0;
  color: #dc2626;
  font-size: 13px;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 4px;
}

.icon {
  width: 18px;
  height: 18px;
}

.checkbox-field {
  display: inline-flex !important;
  align-items: center;
  gap: 8px !important;
  cursor: pointer;
}

.checkbox-field--modal {
  margin: 4px 0 10px;
}

@media (max-width: 768px) {
  .category-card {
    width: 100%;
    min-height: calc(100vh - 120px);
  }

  .category-card__header {
    padding: 18px 18px 14px;
  }

  .category-toolbar {
    padding: 0 18px 14px;
  }

  .category-tree {
    margin: 0 12px;
  }

  .row-action {
    opacity: 1;
    transform: none;
  }
}
</style>
