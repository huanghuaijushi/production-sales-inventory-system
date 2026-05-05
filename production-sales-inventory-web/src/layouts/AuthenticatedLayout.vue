<template>
  <div class="dashboard-layout">
    <aside class="sidebar">
      <div class="sidebar-header">
        <div class="logo">
          <div class="logo-icon">HH</div>
          <div class="logo-text">
            <div class="logo-title">HHJS</div>
            <div class="logo-subtitle">产销存系统</div>
          </div>
        </div>
      </div>

      <nav class="sidebar-nav">
        <RouterLink to="/dashboard" class="nav-item" :class="{ active: isActive('dashboard') }">
          <HomeIcon class="nav-icon" />
          <span class="nav-text">控制台首页</span>
        </RouterLink>
        <RouterLink to="/inventory" class="nav-item" :class="{ active: isActive('inventory') }">
          <CubeIcon class="nav-icon" />
          <span class="nav-text">库存管理</span>
        </RouterLink>
        <RouterLink to="/products" class="nav-item" :class="{ active: isActive('products') }">
          <TagIcon class="nav-icon" />
          <span class="nav-text">产品管理</span>
        </RouterLink>
        <RouterLink to="/purchase" class="nav-item" :class="{ active: isActive('purchase') }">
          <ShoppingCartIcon class="nav-icon" />
          <span class="nav-text">采购管理</span>
        </RouterLink>
        <RouterLink to="/suppliers" class="nav-item" :class="{ active: isActive('suppliers') }">
          <TruckIcon class="nav-icon" />
          <span class="nav-text">供应商管理</span>
        </RouterLink>
        <RouterLink to="/production" class="nav-item" :class="{ active: isActive('production') }">
          <ClipboardDocumentCheckIcon class="nav-icon" />
          <span class="nav-text">生产计划</span>
        </RouterLink>
        <RouterLink to="/production-config" class="nav-item" :class="{ active: isActive('production-config') }">
          <ClipboardDocumentListIcon class="nav-icon" />
          <span class="nav-text">生产配置</span>
        </RouterLink>
        <RouterLink to="/users" class="nav-item" :class="{ active: isActive('users') }">
          <UserGroupIcon class="nav-icon" />
          <span class="nav-text">用户管理</span>
        </RouterLink>
        <a href="#" class="nav-item">
          <ChartBarIcon class="nav-icon" />
          <span class="nav-text">销售统计</span>
        </a>
        <a href="#" class="nav-item">
          <CogIcon class="nav-icon" />
          <span class="nav-text">系统设置</span>
        </a>
      </nav>

      <div class="sidebar-footer">
        <div class="decoration">📦</div>
      </div>
    </aside>

    <div class="main-content">
      <header class="topbar">
        <div class="breadcrumb">
          <span>{{ breadcrumb.main }}</span>
          <span class="separator">/</span>
          <span>{{ breadcrumb.sub }}</span>
        </div>

        <div class="search-box" ref="searchBoxRef">
          <MagnifyingGlassIcon class="search-icon" />
          <input
            v-model="globalSearchQuery"
            type="text"
            placeholder="搜索商品、库存或 SKU"
            @focus="openSearchPanel"
            @keyup.enter="submitGlobalSearch"
          />

          <div v-if="searchPanelOpen" class="search-panel">
            <div v-if="globalSearchQuery.trim().length === 0" class="search-empty">
              输入商品名称、SKU、分类或规格进行搜索
            </div>
            <div v-else-if="globalSearchLoading" class="search-empty">
              正在搜索...
            </div>
            <template v-else>
              <div v-if="productResults.length > 0" class="search-group">
                <div class="search-group-title">产品</div>
                <button
                  v-for="product in productResults"
                  :key="`product-${product.id}`"
                  type="button"
                  class="search-result"
                  @click="goProductSearch(product.name)"
                >
                  <span class="result-title">{{ product.name }}</span>
                  <span class="result-meta">{{ product.sku }} · {{ product.category || '未分类' }}</span>
                </button>
              </div>

              <div v-if="stockResults.length > 0" class="search-group">
                <div class="search-group-title">库存</div>
                <button
                  v-for="stock in stockResults"
                  :key="`stock-${stock.id}`"
                  type="button"
                  class="search-result"
                  @click="goInventorySearch(stock.productName)"
                >
                  <span class="result-title">{{ stock.productName }}</span>
                  <span class="result-meta">
                    {{ stock.productCode }} · 库存 {{ stock.quantity }}{{ stock.unit }}
                  </span>
                </button>
              </div>

              <div v-if="!hasSearchResults" class="search-empty">
                没有找到匹配结果
              </div>

              <div class="search-actions">
                <button type="button" @click="goProductSearch(globalSearchQuery)">查产品</button>
                <button type="button" @click="goInventorySearch(globalSearchQuery)">查库存</button>
              </div>
            </template>
          </div>
        </div>

        <div class="user-section">
          <div class="user-info" @click.stop="toggleUserMenu">
            <div class="avatar">{{ authStore.admin?.nickname?.charAt(0) || 'A' }}</div>
            <span class="username">{{ authStore.admin?.nickname || authStore.admin?.username }}</span>
            <ChevronDownIcon class="dropdown-arrow" />
          </div>
          <div class="user-menu" v-if="userMenuOpen">
            <button type="button" class="user-menu-item" @click="handleLogout">退出登录</button>
          </div>
        </div>
      </header>

      <main class="content-area" :class="{ 'content-area--dashboard': route.name === 'dashboard' }">
        <RouterView />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { productApi, type Product } from '@/api/product'
import { inventoryApi, type StockItem } from '@/api/inventory'
import {
  HomeIcon,
  CubeIcon,
  TagIcon,
  ShoppingCartIcon,
  TruckIcon,
  ClipboardDocumentCheckIcon,
  ClipboardDocumentListIcon,
  ChartBarIcon,
  CogIcon,
  MagnifyingGlassIcon,
  ChevronDownIcon,
  UserGroupIcon
} from '@heroicons/vue/24/outline'

const authStore = useAuthStore()
const route = useRoute()
const router = useRouter()
const userMenuOpen = ref(false)
const searchBoxRef = ref<HTMLElement | null>(null)
const searchPanelOpen = ref(false)
const globalSearchQuery = ref('')
const globalSearchLoading = ref(false)
const productResults = ref<Product[]>([])
const stockResults = ref<StockItem[]>([])
let searchTimer: number | undefined

const hasSearchResults = computed(() => productResults.value.length > 0 || stockResults.value.length > 0)

const breadcrumb = computed(() => {
  if (route.name === 'inventory') {
    return {
      main: '库存管理',
      sub: '库存总览'
    }
  }

  if (route.name === 'products') {
    return {
      main: '产品管理',
      sub: '产品列表'
    }
  }

  if (route.name === 'purchase') {
    return {
      main: '采购管理',
      sub: '手动采购'
    }
  }

  if (route.name === 'suppliers') {
    return {
      main: '供应商管理',
      sub: '供货规则'
    }
  }

  if (route.name === 'production-config') {
    return {
      main: '生产配置',
      sub: '成品配方'
    }
  }

  if (route.name === 'production') {
    return {
      main: '生产管理',
      sub: '生产计划'
    }
  }

  if (route.name === 'users') {
    return {
      main: '系统设置',
      sub: '用户管理'
    }
  }

  return {
    main: '控制台首页',
    sub: '实时概览'
  }
})

function isActive(name: string) {
  return route.name === name
}

function toggleUserMenu() {
  userMenuOpen.value = !userMenuOpen.value
}

function closeUserMenu() {
  userMenuOpen.value = false
}

function openSearchPanel() {
  searchPanelOpen.value = true
}

function closeSearchPanel() {
  searchPanelOpen.value = false
}

function clearSearchResults() {
  productResults.value = []
  stockResults.value = []
}

async function runGlobalSearch() {
  const keyword = globalSearchQuery.value.trim()
  if (!keyword) {
    clearSearchResults()
    globalSearchLoading.value = false
    return
  }

  globalSearchLoading.value = true
  try {
    const [products, stocks] = await Promise.all([
      productApi.searchProducts(keyword, 0, 5),
      inventoryApi.getAllStocks(0, 5, { query: keyword })
    ])
    productResults.value = products.content
    stockResults.value = stocks.content
  } catch (error) {
    console.error('全局搜索失败:', error)
    clearSearchResults()
  } finally {
    globalSearchLoading.value = false
  }
}

function scheduleGlobalSearch() {
  window.clearTimeout(searchTimer)
  searchTimer = window.setTimeout(() => {
    runGlobalSearch()
  }, 260)
}

function submitGlobalSearch() {
  const keyword = globalSearchQuery.value.trim()
  if (!keyword) {
    return
  }
  goInventorySearch(keyword)
}

function goProductSearch(keyword: string) {
  const query = keyword.trim()
  if (!query) {
    return
  }
  closeSearchPanel()
  router.push({ name: 'products', query: { q: query } })
}

function goInventorySearch(keyword: string) {
  const query = keyword.trim()
  if (!query) {
    return
  }
  closeSearchPanel()
  router.push({ name: 'inventory', query: { q: query } })
}

async function handleLogout() {
  userMenuOpen.value = false
  try {
    await authStore.logout()
    await router.replace({ name: 'login' })
  } catch (error) {
    console.error(error)
  }
}

function handleDocumentClick(event: MouseEvent) {
  const target = event.target as HTMLElement
  if (!target.closest('.user-section')) {
    closeUserMenu()
  }
  if (!target.closest('.search-box')) {
    closeSearchPanel()
  }
}

onMounted(() => {
  document.addEventListener('click', handleDocumentClick)
})

onBeforeUnmount(() => {
  window.clearTimeout(searchTimer)
  document.removeEventListener('click', handleDocumentClick)
})

watch(globalSearchQuery, () => {
  searchPanelOpen.value = true
  scheduleGlobalSearch()
})
</script>

<style scoped>
.dashboard-layout {
  display: flex;
  min-height: 100vh;
  background-color: #F5F7FA;
}

.sidebar {
  width: 240px;
  background: linear-gradient(180deg, #071E35 0%, #0B2744 100%);
  display: flex;
  flex-direction: column;
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
}

.sidebar-header {
  padding: 24px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-icon {
  width: 40px;
  height: 40px;
  background: #1890ff;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 700;
}

.logo-text {
  color: white;
}

.logo-title {
  font-size: 18px;
  font-weight: 600;
  line-height: 1.2;
}

.logo-subtitle {
  font-size: 12px;
  opacity: 0.8;
  line-height: 1.2;
}

.sidebar-nav {
  flex: 1;
  padding: 16px 0;
}

.nav-item {
  display: flex;
  align-items: center;
  height: 52px;
  padding: 0 24px;
  color: rgba(255, 255, 255, 0.75);
  text-decoration: none;
  transition: all 0.2s ease;
}

.nav-item:hover {
  background: rgba(255, 255, 255, 0.1);
  color: white;
}

.nav-item.active {
  background: #1890ff;
  color: white;
}

.nav-icon {
  width: 20px;
  height: 20px;
  margin-right: 12px;
  flex-shrink: 0;
}

.nav-text {
  font-size: 14px;
}

.sidebar-footer {
  padding: 24px;
  display: flex;
  justify-content: center;
}

.decoration {
  font-size: 40px;
  opacity: 0.65;
}

.main-content {
  margin-left: 240px;
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.topbar {
  height: 72px;
  background: white;
  border-bottom: 1px solid #E5E7EB;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
}

.breadcrumb {
  font-size: 14px;
  color: #6B7280;
}

.separator {
  margin: 0 8px;
}

.search-box {
  position: relative;
  width: 380px;
}

.search-icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #9CA3AF;
  width: 20px;
  height: 20px;
}

.search-box input {
  width: 100%;
  height: 42px;
  border: 1px solid #D1D5DB;
  border-radius: 21px;
  padding: 0 16px 0 40px;
  font-size: 14px;
  outline: none;
}

.search-box input:focus {
  border-color: #1890ff;
}

.search-panel {
  position: absolute;
  left: 0;
  right: 0;
  top: calc(100% + 10px);
  z-index: 20;
  max-height: 420px;
  overflow-y: auto;
  border: 1px solid #E5E7EB;
  border-radius: 12px;
  background: #ffffff;
  box-shadow: 0 18px 40px rgba(15, 23, 42, 0.12);
  padding: 10px;
}

.search-group + .search-group {
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid #F3F4F6;
}

.search-group-title {
  padding: 6px 8px;
  color: #64748B;
  font-size: 12px;
  font-weight: 700;
}

.search-result {
  width: 100%;
  display: grid;
  gap: 3px;
  padding: 9px 10px;
  border: none;
  border-radius: 8px;
  background: transparent;
  text-align: left;
  cursor: pointer;
}

.search-result:hover {
  background: #F8FAFC;
}

.result-title {
  color: #0F172A;
  font-size: 14px;
  font-weight: 700;
}

.result-meta {
  color: #64748B;
  font-size: 12px;
}

.search-empty {
  padding: 20px 10px;
  color: #94A3B8;
  text-align: center;
  font-size: 13px;
}

.search-actions {
  display: flex;
  gap: 8px;
  padding-top: 10px;
  margin-top: 8px;
  border-top: 1px solid #F3F4F6;
}

.search-actions button {
  flex: 1;
  min-height: 34px;
  border: 1px solid #CBD5E1;
  border-radius: 8px;
  background: #ffffff;
  color: #2563EB;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
}

.search-actions button:hover {
  background: #EFF6FF;
}

.user-section {
  position: relative;
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 999px;
  transition: background 0.2s ease;
}

.user-info:hover {
  background: rgba(24, 144, 255, 0.08);
}

.user-menu {
  position: absolute;
  right: 0;
  top: calc(100% + 8px);
  min-width: 160px;
  background: white;
  border: 1px solid #E5E7EB;
  border-radius: 12px;
  box-shadow: 0 16px 32px rgba(15, 23, 42, 0.08);
  z-index: 10;
}

.user-menu-item {
  width: 100%;
  background: none;
  border: none;
  padding: 12px 16px;
  text-align: left;
  color: #1F2937;
  font-size: 14px;
  cursor: pointer;
}

.user-menu-item:hover {
  background: #F3F4F6;
}

.avatar {
  width: 32px;
  height: 32px;
  background: #1890ff;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
}

.username {
  font-size: 14px;
  color: #1F2937;
}

.dropdown-arrow {
  width: 16px;
  height: 16px;
  color: #6B7280;
}

.content-area {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}

.content-area--dashboard {
  padding: 8px;
}

@media (max-width: 1200px) {
  .sidebar {
    width: 200px;
  }

  .main-content {
    margin-left: 200px;
  }

  .search-box {
    width: 260px;
  }
}

@media (max-width: 768px) {
  .sidebar {
    width: 72px;
  }

  .sidebar-nav {
    padding: 8px 0;
  }

  .nav-item {
    justify-content: center;
    padding: 0;
  }

  .nav-text {
    display: none;
  }

  .main-content {
    margin-left: 72px;
  }

  .topbar {
    padding: 0 16px;
  }

  .content-area {
    padding: 16px;
  }

  .content-area--dashboard {
    padding: 6px;
  }
}
</style>
