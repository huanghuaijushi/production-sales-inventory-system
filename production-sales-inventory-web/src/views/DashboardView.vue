<template>
  <div class="home-dashboard">
    <!-- Block 1 · 本月销售 -->
    <section class="home-card home-card--hero" :class="{ 'is-loading': loading }">
      <header class="home-card__header">
        <h2>本月销售</h2>
        <RouterLink to="/sales" class="home-card__link">查看销售明细 →</RouterLink>
      </header>

      <div class="hero-number">
        <strong>{{ salesHero.value }}</strong>
        <span v-if="salesHero.unit" class="hero-number__unit">{{ salesHero.unit }}</span>
      </div>

      <div class="hero-meta">
        <span v-if="growthLabel" :class="['hero-meta__growth', growthClass]">
          {{ growthLabel }}
          <small>vs 上月</small>
        </span>
        <span v-else class="hero-meta__growth hero-meta__growth--empty">
          上月无销售数据
        </span>
        <span class="hero-meta__today">
          今天进账 <strong>{{ todayLabel }}</strong>
        </span>
      </div>
    </section>

    <!-- Block 2 · 欠我钱 / 现金跑道（占位） -->
    <section class="home-card home-card--placeholder">
      <header class="home-card__header">
        <h2>欠我钱 · 现金跑道</h2>
        <span class="home-card__pill">开发中</span>
      </header>

      <div class="placeholder-body">
        <p class="placeholder-body__lead">
          应收账款 / 现金流模块计划在下一期上线。
        </p>
        <ul class="placeholder-body__list">
          <li>应收金额 · 已收款 · 未回款</li>
          <li>30 / 60 / 90+ 天账龄分布</li>
          <li>现金在手 · 未来 4 周大额收支 · 现金跑道天数</li>
        </ul>
        <p class="placeholder-body__note">
          上线前请暂时通过线下表格管理回款和现金流。
        </p>
      </div>
    </section>

    <!-- Block 3 + 4 · 库存红黄绿 / 今日待办 -->
    <section class="home-row">
      <article class="home-card">
        <header class="home-card__header">
          <h2>库存红黄绿</h2>
          <RouterLink to="/inventory" class="home-card__link">查看库存 →</RouterLink>
        </header>

        <div class="alert-summary">
          <span class="alert-summary__pill is-red">🔴 红 {{ inventoryAlerts.redCount }}</span>
          <span class="alert-summary__pill is-yellow">🟡 黄 {{ inventoryAlerts.yellowCount }}</span>
          <span class="alert-summary__pill is-green">🟢 绿 {{ inventoryAlerts.greenCount }}</span>
        </div>

        <div v-if="inventoryAlerts.redAlerts.length > 0" class="alert-group">
          <h3 class="alert-group__title">临断货 / 低于安全库存</h3>
          <ul class="alert-list">
            <li v-for="item in inventoryAlerts.redAlerts" :key="`red-${item.productId}`">
              <span class="alert-list__name">{{ item.productName }}</span>
              <span class="alert-list__detail">
                {{ formatType(item.productType) }} · 当前 {{ item.current }} / 安全 {{ item.safety }}
              </span>
            </li>
          </ul>
          <p v-if="inventoryAlerts.redCount > inventoryAlerts.redAlerts.length" class="alert-list__more">
            还有 {{ inventoryAlerts.redCount - inventoryAlerts.redAlerts.length }} 个未显示
          </p>
        </div>

        <div v-if="inventoryAlerts.yellowAlerts.length > 0" class="alert-group">
          <h3 class="alert-group__title">成品临期（{{ EXPIRY_DAYS }} 天内）</h3>
          <ul class="alert-list">
            <li v-for="item in inventoryAlerts.yellowAlerts" :key="`yellow-${item.productId}-${item.batchNo}`">
              <span class="alert-list__name">{{ item.productName }}</span>
              <span class="alert-list__detail">
                批次 {{ item.batchNo }} · 还有 {{ item.daysToExpiry }} 天
              </span>
            </li>
          </ul>
          <p v-if="inventoryAlerts.yellowCount > inventoryAlerts.yellowAlerts.length" class="alert-list__more">
            还有 {{ inventoryAlerts.yellowCount - inventoryAlerts.yellowAlerts.length }} 个未显示
          </p>
        </div>

        <p
          v-if="inventoryAlerts.redAlerts.length === 0 && inventoryAlerts.yellowAlerts.length === 0"
          class="alert-empty"
        >
          没有库存预警，状态正常。
        </p>
      </article>

      <article class="home-card">
        <header class="home-card__header">
          <h2>今日待办</h2>
        </header>

        <ul class="todo-list">
          <li>
            <span class="todo-list__label">待发货</span>
            <span class="todo-list__value">{{ todos.pendingShipments }} 单</span>
            <RouterLink to="/sales" class="todo-list__action">打开销售 →</RouterLink>
          </li>
          <li>
            <span class="todo-list__label">待入库</span>
            <span class="todo-list__value">{{ todos.pendingInbounds }} 单</span>
            <RouterLink to="/purchase" class="todo-list__action">打开采购 →</RouterLink>
          </li>
          <li class="todo-list__item--muted">
            <span class="todo-list__label">待审批</span>
            <span class="todo-list__value">{{ todos.pendingApprovals }}</span>
            <span class="todo-list__action todo-list__action--ghost">审批流即将上线</span>
          </li>
        </ul>
      </article>
    </section>

    <p v-if="errorMessage" class="home-error">{{ errorMessage }}</p>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { dashboardApi, type HomeDashboard, type InventoryAlertsBlock, type SalesMonthBlock, type TodayTodosBlock } from '@/api/dashboard'

const EXPIRY_DAYS = 30

const loading = ref(false)
const errorMessage = ref('')
const data = ref<HomeDashboard | null>(null)

const salesMonth = computed<SalesMonthBlock>(() => data.value?.salesMonth ?? {
  currentMonthAmount: 0,
  lastMonthAmount: 0,
  growthPercent: null,
  todayAmount: 0,
  currency: 'CNY'
})

const inventoryAlerts = computed<InventoryAlertsBlock>(() => data.value?.inventoryAlerts ?? {
  redCount: 0,
  yellowCount: 0,
  greenCount: 0,
  redAlerts: [],
  yellowAlerts: []
})

const todos = computed<TodayTodosBlock>(() => data.value?.todayTodos ?? {
  pendingShipments: 0,
  pendingInbounds: 0,
  pendingApprovals: 0
})

const salesHero = computed(() => formatAmount(salesMonth.value.currentMonthAmount))
const todayLabel = computed(() => formatCompactAmount(salesMonth.value.todayAmount))

const growthLabel = computed(() => {
  const p = salesMonth.value.growthPercent
  if (p === null || p === undefined) return ''
  const sign = p > 0 ? '+' : ''
  return `📈 ${sign}${p}%`
})

const growthClass = computed(() => {
  const p = salesMonth.value.growthPercent
  if (p === null || p === undefined) return ''
  if (p > 0) return 'is-up'
  if (p < 0) return 'is-down'
  return ''
})

function formatType(t: string): string {
  switch (t) {
    case 'FINISHED_PRODUCT': return '成品'
    case 'RAW_MATERIAL': return '原料'
    case 'PACKAGING_MATERIAL': return '包材'
    case 'SEMI_FINISHED_PRODUCT': return '半成品'
    default: return t
  }
}

function formatAmount(value: number): { value: string; unit: string } {
  if (value >= 10000) {
    return { value: (value / 10000).toFixed(1).replace(/\.0$/, ''), unit: '万元' }
  }
  return { value: `¥ ${value.toLocaleString('zh-CN', { maximumFractionDigits: 0 })}`, unit: '' }
}

function formatCompactAmount(value: number): string {
  if (value >= 10000) {
    return `¥ ${(value / 10000).toFixed(1).replace(/\.0$/, '')} 万`
  }
  return `¥ ${value.toLocaleString('zh-CN', { maximumFractionDigits: 0 })}`
}

async function loadData() {
  loading.value = true
  errorMessage.value = ''
  try {
    data.value = await dashboardApi.getHome()
  } catch (error) {
    console.error('加载首页数据失败', error)
    errorMessage.value = '加载失败，请稍后重试。'
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<style scoped>
.home-dashboard {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 8px 4px;
}

.home-card {
  background: #ffffff;
  border: 1px solid #E5E7EB;
  border-radius: 16px;
  padding: 24px 28px;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);
}

.home-card.is-loading {
  opacity: 0.7;
}

.home-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.home-card__header h2 {
  font-size: 15px;
  font-weight: 600;
  color: #475569;
  margin: 0;
  letter-spacing: 0.5px;
}

.home-card__link {
  font-size: 13px;
  color: #1890ff;
  text-decoration: none;
  font-weight: 600;
}

.home-card__link:hover {
  text-decoration: underline;
}

.home-card__pill {
  background: #FEF3C7;
  color: #92400E;
  font-size: 12px;
  padding: 3px 10px;
  border-radius: 999px;
  font-weight: 600;
}

/* Hero block */
.home-card--hero {
  background: linear-gradient(135deg, #0B2744 0%, #0F3D6E 100%);
  border-color: transparent;
  color: #ffffff;
}

.home-card--hero .home-card__header h2 {
  color: rgba(255, 255, 255, 0.8);
}

.home-card--hero .home-card__link {
  color: rgba(255, 255, 255, 0.9);
}

.hero-number {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 14px;
}

.hero-number strong {
  font-size: 56px;
  font-weight: 700;
  line-height: 1.05;
  letter-spacing: -1px;
}

.hero-number__unit {
  font-size: 22px;
  font-weight: 500;
  opacity: 0.9;
}

.hero-meta {
  display: flex;
  align-items: center;
  gap: 24px;
  flex-wrap: wrap;
  font-size: 14px;
}

.hero-meta__growth {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: rgba(255, 255, 255, 0.12);
  padding: 6px 12px;
  border-radius: 999px;
  font-weight: 600;
}

.hero-meta__growth small {
  opacity: 0.7;
  font-weight: 400;
}

.hero-meta__growth.is-up {
  background: rgba(34, 197, 94, 0.25);
  color: #BBF7D0;
}

.hero-meta__growth.is-down {
  background: rgba(239, 68, 68, 0.25);
  color: #FECACA;
}

.hero-meta__growth--empty {
  opacity: 0.65;
}

.hero-meta__today {
  opacity: 0.85;
}

.hero-meta__today strong {
  margin-left: 6px;
  font-weight: 700;
}

/* Placeholder block */
.home-card--placeholder {
  background: #F8FAFC;
  border-style: dashed;
}

.placeholder-body__lead {
  font-size: 15px;
  color: #334155;
  font-weight: 600;
  margin: 0 0 12px;
}

.placeholder-body__list {
  list-style: none;
  padding: 0;
  margin: 0 0 12px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  color: #64748B;
  font-size: 13px;
}

.placeholder-body__list li::before {
  content: '· ';
  color: #94A3B8;
  margin-right: 6px;
}

.placeholder-body__note {
  font-size: 12px;
  color: #94A3B8;
  margin: 0;
}

/* Row of two cards */
.home-row {
  display: grid;
  grid-template-columns: minmax(0, 1.4fr) minmax(0, 1fr);
  gap: 20px;
}

@media (max-width: 1024px) {
  .home-row {
    grid-template-columns: 1fr;
  }
}

/* Inventory alerts */
.alert-summary {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.alert-summary__pill {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 14px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 600;
}

.alert-summary__pill.is-red {
  background: #FEE2E2;
  color: #991B1B;
}

.alert-summary__pill.is-yellow {
  background: #FEF3C7;
  color: #92400E;
}

.alert-summary__pill.is-green {
  background: #DCFCE7;
  color: #166534;
}

.alert-group {
  margin-bottom: 14px;
}

.alert-group__title {
  font-size: 13px;
  font-weight: 600;
  color: #475569;
  margin: 0 0 8px;
}

.alert-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.alert-list li {
  display: flex;
  align-items: baseline;
  gap: 12px;
  padding: 8px 12px;
  background: #F8FAFC;
  border-radius: 8px;
  font-size: 13px;
}

.alert-list__name {
  font-weight: 600;
  color: #1E293B;
}

.alert-list__detail {
  color: #64748B;
  font-size: 12px;
}

.alert-list__more {
  margin: 6px 0 0;
  font-size: 12px;
  color: #94A3B8;
}

.alert-empty {
  margin: 0;
  font-size: 13px;
  color: #16A34A;
  background: #DCFCE7;
  padding: 10px 14px;
  border-radius: 8px;
}

/* Today todos */
.todo-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.todo-list li {
  display: grid;
  grid-template-columns: 80px 1fr auto;
  align-items: center;
  gap: 12px;
  padding: 12px 4px;
  border-bottom: 1px solid #F1F5F9;
}

.todo-list li:last-child {
  border-bottom: none;
}

.todo-list__label {
  font-size: 13px;
  color: #64748B;
}

.todo-list__value {
  font-size: 18px;
  font-weight: 700;
  color: #1E293B;
}

.todo-list__action {
  font-size: 13px;
  color: #1890ff;
  text-decoration: none;
  font-weight: 600;
}

.todo-list__action:hover {
  text-decoration: underline;
}

.todo-list__action--ghost {
  color: #94A3B8;
  font-weight: 400;
  cursor: default;
}

.todo-list__action--ghost:hover {
  text-decoration: none;
}

.todo-list__item--muted .todo-list__value {
  color: #94A3B8;
}

.home-error {
  margin: 0;
  padding: 10px 14px;
  background: #FEE2E2;
  color: #991B1B;
  border-radius: 8px;
  font-size: 13px;
}
</style>
