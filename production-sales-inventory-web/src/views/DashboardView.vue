<template>
  <div class="dashboard-page">
    <div class="dashboard-shell">
      <section class="top-panels">
        <section class="metric-grid metric-grid--top">
          <article v-for="item in topMetrics" :key="item.title" class="metric-card card-surface">
            <div class="metric-title-row">
              <div class="metric-title">{{ item.title }}</div>
              <div class="metric-mini-icon" :style="{ color: item.accentColor }">{{ item.icon }}</div>
            </div>
            <div class="metric-value-row">
              <div class="metric-value">{{ item.value }}</div>
              <div class="metric-change-chip" :class="item.trendClass">{{ item.change }}</div>
            </div>
            <div class="metric-subtitle">{{ item.subtitle }}</div>
            <div class="metric-progress">
              <i :style="{ width: item.progress, background: item.progressColor }"></i>
            </div>
          </article>
        </section>

        <article class="card-surface panel-card panel-card--quick">
          <div class="panel-header">
            <div>
              <h2>快捷操作</h2>
            </div>
          </div>

          <div class="quick-grid">
            <button v-for="action in quickActions" :key="action.label" type="button" class="quick-action" @click="handleQuickAction(action.action)">
              <span class="quick-action__icon" :class="action.iconClass">
                <component :is="action.icon" />
              </span>
              <span class="quick-action__label">{{ action.label }}</span>
            </button>
          </div>
        </article>
      </section>

      <section class="split-grid split-grid--charts">
        <article class="card-surface section-card section-card--chart">
          <div class="section-card__header section-card__header--tight">
            <div>
              <h2>原料监控</h2>
              <p>原料收发存趋势</p>
            </div>
            <div class="segmented-control">
              <button :class="{ 'is-active': rawMonitorMode === 'amount' }" type="button" @click="rawMonitorMode = 'amount'">金额</button>
              <button :class="{ 'is-active': rawMonitorMode === 'quantity' }" type="button" @click="rawMonitorMode = 'quantity'">业务数量</button>
            </div>
          </div>

          <div class="chart-placeholder chart-placeholder--large">
            <div class="chart-legend chart-legend--top">
              <span><i class="legend-dot legend-dot--blue"></i>{{ rawLegend.inbound }}</span>
              <span><i class="legend-dot legend-dot--orange"></i>{{ rawLegend.outbound }}</span>
              <span><i class="legend-dot legend-dot--green"></i>{{ rawLegend.stock }}</span>
            </div>
            <div class="chart-panel">
              <div class="chart-axis">
                <span v-for="tick in rawAxisTicks" :key="tick">{{ tick }}</span>
              </div>
              <div class="chart-bars">
                <span
                  v-for="bar in displayedRawMaterialBars"
                  :key="bar.label"
                  class="bar-item"
                  @mouseenter="showChartTooltip($event, 'raw', bar)"
                  @mousemove="moveChartTooltip"
                  @mouseleave="hideChartTooltip"
                >
                  <span class="bar-item__column">
                    <i class="bar-item__blue" :style="{ height: bar.inbound ?? '0%' }"></i>
                    <i class="bar-item__orange" :style="{ height: bar.outbound ?? '0%' }"></i>
                    <i class="bar-item__green line-dot" :style="{ bottom: bar.stock ?? '0%' }"></i>
                  </span>
                  <span class="bar-item__label">{{ bar.label }}</span>
                </span>
                <div class="chart-line chart-line--raw"></div>
              </div>
            </div>
          </div>

          <div class="detail-grid">
            <div class="table-card">
              <div class="table-card__title">原料预警排行</div>
              <table>
                <thead>
                  <tr>
                    <th>名称</th>
                    <th>当前库存</th>
                    <th>安全库存</th>
                    <th>可用天数</th>
                    <th>状态</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="row in rawMaterialWarnings" :key="row.name">
                    <td>{{ row.name }}</td>
                    <td>{{ row.current }}</td>
                    <td>{{ row.safety }}</td>
                    <td>{{ row.days }}</td>
                    <td><span class="status-badge" :class="row.statusClass">{{ row.status }}</span></td>
                  </tr>
                </tbody>
              </table>
              <RouterLink to="/inventory" class="table-link">查看全部原料预警</RouterLink>
            </div>

            <div class="summary-stack">
              <div v-for="item in rawSummaryCards" :key="item.label" class="summary-mini-card">
                <span class="summary-mini-card__label">{{ item.label }}</span>
                <strong class="summary-mini-card__value">{{ item.value }}</strong>
                <small class="summary-mini-card__meta">{{ item.meta }}</small>
              </div>
            </div>
          </div>
        </article>

        <article class="card-surface section-card section-card--chart">
          <div class="section-card__header section-card__header--tight">
            <div>
              <h2>成品监控</h2>
              <p>成品产销趋势</p>
            </div>
            <div class="segmented-control">
              <button :class="{ 'is-active': finishedMonitorMode === 'quantity' }" type="button" @click="finishedMonitorMode = 'quantity'">件数</button>
              <button :class="{ 'is-active': finishedMonitorMode === 'amount' }" type="button" @click="finishedMonitorMode = 'amount'">库存金额</button>
            </div>
          </div>

          <div class="chart-placeholder chart-placeholder--large">
            <div class="chart-legend chart-legend--top">
              <span><i class="legend-dot legend-dot--blue"></i>{{ finishedLegend.production }}</span>
              <span><i class="legend-dot legend-dot--orange"></i>{{ finishedLegend.sales }}</span>
              <span><i class="legend-dot legend-dot--green"></i>{{ finishedLegend.stock }}</span>
            </div>
            <div class="chart-panel">
              <div class="chart-axis">
                <span v-for="tick in finishedAxisTicks" :key="tick">{{ tick }}</span>
              </div>
              <div class="chart-bars">
                <span
                  v-for="bar in displayedFinishedProductBars"
                  :key="bar.label"
                  class="bar-item"
                  @mouseenter="showChartTooltip($event, 'finished', bar)"
                  @mousemove="moveChartTooltip"
                  @mouseleave="hideChartTooltip"
                >
                  <span class="bar-item__column">
                    <i class="bar-item__blue" :style="{ height: bar.production ?? '0%' }"></i>
                    <i class="bar-item__orange" :style="{ height: bar.sales ?? '0%' }"></i>
                    <i class="bar-item__green line-dot" :style="{ bottom: bar.stock ?? '0%' }"></i>
                  </span>
                  <span class="bar-item__label">{{ bar.label }}</span>
                </span>
                <div class="chart-line chart-line--finished"></div>
              </div>
            </div>
          </div>

          <div class="detail-grid">
            <div class="ranking-card">
              <div class="table-card__title">热销成品 Top5（按销售出库件数）</div>
              <div class="ranking-list">
                <div v-for="item in hotProducts" :key="item.name" class="ranking-item">
                  <span class="ranking-item__index">{{ item.rank }}</span>
                  <div class="ranking-item__body">
                    <div class="ranking-item__title">{{ item.name }}</div>
                    <div class="ranking-item__track"><i :style="{ width: item.percent }"></i></div>
                  </div>
                  <strong class="ranking-item__value">{{ item.value }}</strong>
                </div>
              </div>
              <RouterLink to="/inventory" class="table-link">查看全部成品排行</RouterLink>
            </div>

            <div class="summary-stack">
              <div v-for="item in finishedSummaryCards" :key="item.label" class="summary-mini-card">
                <span class="summary-mini-card__label">{{ item.label }}</span>
                <strong class="summary-mini-card__value">{{ item.value }}</strong>
                <small class="summary-mini-card__meta">{{ item.meta }}</small>
              </div>
            </div>
          </div>
        </article>
      </section>

      <section class="profit-section card-surface section-card">
        <div class="section-card__header section-card__header--tight">
          <div>
            <h2>收益看板 / 利润分析</h2>
            <p>近 7 天已发货/已完成销售单，按实际成交价计算收入，按成品成本价估算销售成本。</p>
          </div>
          <div class="profit-headline" v-if="profitOverview">
            <span>毛利率</span>
            <strong>{{ profitOverview.grossMargin }}</strong>
          </div>
        </div>

        <div class="profit-metric-grid">
          <article v-for="item in profitMetrics" :key="item.key" class="profit-metric-card">
            <span>{{ item.title }}</span>
            <strong>{{ item.value }}</strong>
            <small>{{ item.subtitle }} · {{ item.change }}</small>
            <i :style="{ width: item.progress, background: item.progressColor }"></i>
          </article>
        </div>

        <div class="profit-grid">
          <div class="profit-chart-card">
            <div class="chart-legend chart-legend--top">
              <span><i class="legend-dot legend-dot--blue"></i>销售收入</span>
              <span><i class="legend-dot legend-dot--orange"></i>销售成本</span>
              <span><i class="legend-dot legend-dot--green"></i>毛利</span>
            </div>
            <div class="chart-panel profit-chart-panel">
              <div class="chart-bars chart-bars--profit">
                <span
                  v-for="bar in profitTrendBars"
                  :key="bar.label"
                  class="bar-item"
                  @mouseenter="showChartTooltip($event, 'profit', bar)"
                  @mousemove="moveChartTooltip"
                  @mouseleave="hideChartTooltip"
                >
                  <span class="bar-item__column">
                    <i class="bar-item__blue" :style="{ height: bar.production ?? '0%' }"></i>
                    <i class="bar-item__orange" :style="{ height: bar.sales ?? '0%' }"></i>
                    <i class="bar-item__green line-dot" :style="{ bottom: bar.stock ?? '0%' }"></i>
                  </span>
                  <span class="bar-item__label">{{ bar.label }}</span>
                </span>
                <div class="chart-line chart-line--finished"></div>
              </div>
            </div>
          </div>

          <div class="ranking-card profit-ranking-card">
            <div class="table-card__title">渠道收入 Top5</div>
            <div class="ranking-list">
              <div v-for="item in profitChannelRanking" :key="item.name" class="ranking-item">
                <span class="ranking-item__index">{{ item.rank }}</span>
                <div class="ranking-item__body">
                  <div class="ranking-item__title">{{ item.name }}</div>
                  <div class="ranking-item__track"><i :style="{ width: item.percent }"></i></div>
                </div>
                <strong class="ranking-item__value">{{ item.value }}</strong>
              </div>
              <div v-if="profitChannelRanking.length === 0" class="profit-empty">暂无已发货销售数据</div>
            </div>
          </div>

          <div class="summary-stack profit-summary-stack">
            <div v-for="item in profitSummaries" :key="item.label" class="summary-mini-card">
              <span class="summary-mini-card__label">{{ item.label }}</span>
              <strong class="summary-mini-card__value">{{ item.value }}</strong>
              <small class="summary-mini-card__meta">{{ item.meta }}</small>
            </div>
          </div>
        </div>
      </section>

      <div
        v-if="chartTooltip.visible"
        class="floating-chart-tooltip"
        :class="[`is-${chartTooltip.placement}`, `is-align-${chartTooltip.align}`]"
        :style="{ left: `${chartTooltip.x}px`, top: `${chartTooltip.y}px` }"
      >
        <strong>{{ chartTooltip.title }}</strong>
        <em v-for="row in chartTooltip.rows" :key="row.label">
          <i class="legend-dot" :class="row.colorClass"></i>
          <span>{{ row.label }}</span>
          <b>{{ row.value }}</b>
        </em>
      </div>

      <section class="split-grid split-grid--bottom">
        <article class="card-surface section-card">
          <div class="section-card__header section-card__header--tight">
            <div>
              <h2>原料分类库存占比</h2>
              <p>按库存金额</p>
            </div>
          </div>

          <div class="category-list">
            <div v-for="item in rawCategoryShares" :key="item.name" class="category-row">
              <div class="category-row__main">
                <span class="category-row__dot" :style="{ background: item.color }"></span>
                <span class="category-row__name">{{ item.name }}</span>
                <span class="category-row__ratio">{{ item.ratio }}</span>
              </div>
              <div class="category-row__track"><i :style="{ width: item.percent, background: item.color }"></i></div>
              <div class="category-row__footer">
                <span>库存金额</span>
                <strong>{{ item.value }}</strong>
              </div>
              <div class="category-row__meta">
                <span>{{ item.productCount }} 个 SKU</span>
                <span>库存 {{ formatDashboardNumber(item.quantity) }}</span>
              </div>
            </div>
          </div>

          <div class="category-summary-line">
            <span>合计库存金额</span>
            <strong>{{ rawCategoryTotalAmount }}</strong>
            <RouterLink to="/inventory" class="table-link">查看全部分类</RouterLink>
          </div>
        </article>

        <article class="card-surface section-card">
          <div class="section-card__header section-card__header--tight">
            <div>
              <h2>成品分类库存占比</h2>
              <p>按库存金额</p>
            </div>
          </div>

          <div class="category-list">
            <div v-for="item in finishedCategoryShares" :key="item.name" class="category-row">
              <div class="category-row__main">
                <span class="category-row__dot" :style="{ background: item.color }"></span>
                <span class="category-row__name">{{ item.name }}</span>
                <span class="category-row__ratio">{{ item.ratio }}</span>
              </div>
              <div class="category-row__track"><i :style="{ width: item.percent, background: item.color }"></i></div>
              <div class="category-row__footer">
                <span>库存金额</span>
                <strong>{{ item.value }}</strong>
              </div>
              <div class="category-row__meta">
                <span>{{ item.productCount }} 个 SKU</span>
                <span>库存 {{ formatDashboardNumber(item.quantity) }}</span>
              </div>
            </div>
          </div>

          <div class="category-summary-line">
            <span>合计库存金额</span>
            <strong>{{ finishedCategoryTotalAmount }}</strong>
            <RouterLink to="/inventory" class="table-link">查看全部分类</RouterLink>
          </div>
        </article>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { inventoryApi, type DashboardChartBar, type InventoryDashboard } from '@/api/inventory'
import {
  ShoppingCartIcon,
  ArchiveBoxArrowDownIcon,
  CheckBadgeIcon,
  TruckIcon,
  ClipboardDocumentCheckIcon,
  ExclamationTriangleIcon
} from '@heroicons/vue/24/solid'

type QuickActionType = 'purchase' | 'materialIssue' | 'productionInbound' | 'salesOutbound' | 'inventoryCheck' | 'stockLoss'
type ChartTooltipRow = { label: string; value: string; colorClass: string }
type ChartTooltipPayload = { title: string; rows: ChartTooltipRow[] }
type ChartType = 'raw' | 'finished' | 'profit'

const router = useRouter()
const rawMonitorMode = ref<'amount' | 'quantity'>('amount')
const finishedMonitorMode = ref<'quantity' | 'amount'>('quantity')
const dashboardData = ref<InventoryDashboard | null>(null)
const overviewLoading = ref(false)
const chartTooltip = reactive({
  visible: false,
  x: 0,
  y: 0,
  title: '',
  rows: [] as ChartTooltipRow[],
  placement: 'right' as 'right' | 'left',
  align: 'middle' as 'middle' | 'top'
})

const overviewMetrics = computed(() => {
  const overview = dashboardData.value?.todayBusinessOverview
  return [
    {
      title: '今日入库',
      value: overviewLoading.value ? '加载中' : `${formatNumber(overview?.inboundRecordCount)} / ${formatNumber(overview?.inboundQuantity)}`,
      subtitle: '单数 / 数量',
      change: overviewLoading.value ? '--' : '实时',
      themeClass: 'theme-blue',
      trendClass: 'trend-up'
    },
    {
      title: '今日出库',
      value: overviewLoading.value ? '加载中' : `${formatNumber(overview?.outboundRecordCount)} / ${formatNumber(overview?.outboundQuantity)}`,
      subtitle: '单数 / 数量',
      change: overviewLoading.value ? '--' : '实时',
      themeClass: 'theme-orange',
      trendClass: 'trend-up'
    },
    {
      title: '生产损耗',
      value: overviewLoading.value ? '加载中' : `${formatNumber(overview?.lossRecordCount)} / ${formatNumber(overview?.lossQuantity)}`,
      subtitle: '笔数 / 数量',
      change: overviewLoading.value ? '--' : '实时',
      themeClass: 'theme-green',
      trendClass: 'trend-up'
    },
    {
      title: '待发货订单',
      value: overviewLoading.value ? '加载中' : `${formatNumber(overview?.pendingSalesOrderCount)}单`,
      subtitle: '销售待处理',
      change: overviewLoading.value ? '--' : '实时',
      themeClass: 'theme-purple',
      trendClass: 'trend-up'
    }
  ]
})

const quickActions: Array<{ label: string; icon: typeof ShoppingCartIcon; iconClass: string; action: QuickActionType }> = [
  { label: '采购入库', icon: ShoppingCartIcon, iconClass: 'is-blue', action: 'purchase' },
  { label: '生产领料', icon: ArchiveBoxArrowDownIcon, iconClass: 'is-orange', action: 'materialIssue' },
  { label: '成品入库', icon: CheckBadgeIcon, iconClass: 'is-green', action: 'productionInbound' },
  { label: '销售出库', icon: TruckIcon, iconClass: 'is-purple', action: 'salesOutbound' },
  { label: '盘点', icon: ClipboardDocumentCheckIcon, iconClass: 'is-cyan', action: 'inventoryCheck' },
  { label: '报损', icon: ExclamationTriangleIcon, iconClass: 'is-red', action: 'stockLoss' }
]

const topMetrics = computed(() => dashboardData.value?.topMetrics ?? [])

const rawMaterialBars = computed(() => dashboardData.value?.rawMaterialBars ?? [])
const rawMaterialWarnings = computed(() => dashboardData.value?.rawMaterialWarnings ?? [])
const finishedProductBars = computed(() => dashboardData.value?.finishedProductBars ?? [])
const displayedRawMaterialBars = computed(() => rawMonitorMode.value === 'amount'
  ? rawMaterialBars.value
  : normalizeRawMaterialQuantityBars(rawMaterialBars.value)
)
const displayedFinishedProductBars = computed(() => finishedMonitorMode.value === 'quantity'
  ? finishedProductBars.value
  : normalizeFinishedProductAmountBars(finishedProductBars.value)
)
const rawAxisTicks = computed(() => rawMonitorMode.value === 'amount'
  ? buildAmountAxisTicks(rawMaterialBars.value)
  : buildRawQuantityAxisTicks(rawMaterialBars.value)
)
const finishedAxisTicks = computed(() => finishedMonitorMode.value === 'quantity'
  ? buildCountAxisTicks(finishedProductBars.value)
  : buildFinishedAmountAxisTicks(finishedProductBars.value)
)
const rawLegend = computed(() => rawMonitorMode.value === 'amount'
  ? { inbound: '采购金额（元）', outbound: '领料成本（元）', stock: '库存金额（元）' }
  : { inbound: '采购入库（数量）', outbound: '生产领料（数量）', stock: '当前库存（数量）' }
)
const finishedLegend = computed(() => finishedMonitorMode.value === 'quantity'
  ? { production: '生产入库（件）', sales: '销售出库（件）', stock: '成品库存（件）' }
  : { production: '生产入库金额（元）', sales: '销售出库金额（元）', stock: '成品库存金额（元）' }
)
const hotProducts = computed(() => dashboardData.value?.hotProducts ?? [])
const rawCategoryShares = computed(() => dashboardData.value?.rawCategoryShares ?? [])
const finishedCategoryShares = computed(() => dashboardData.value?.finishedCategoryShares ?? [])
const rawCategoryTotalAmount = computed(() => amountSummary(rawCategoryShares.value))
const finishedCategoryTotalAmount = computed(() => amountSummary(finishedCategoryShares.value))
const rawSummaryCards = computed(() => dashboardData.value?.rawSummaryCards ?? [])
const finishedSummaryCards = computed(() => dashboardData.value?.finishedSummaryCards ?? [])
const profitOverview = computed(() => dashboardData.value?.profitOverview)
const profitMetrics = computed(() => profitOverview.value?.metrics ?? [])
const profitTrendBars = computed(() => profitOverview.value?.trendBars ?? [])
const profitChannelRanking = computed(() => profitOverview.value?.channelRanking ?? [])
const profitSummaries = computed(() => profitOverview.value?.summaries ?? [])

function getRawTooltipRows(bar: DashboardChartBar): ChartTooltipRow[] {
  if (rawMonitorMode.value === 'amount') {
    return [
      { label: rawLegend.value.inbound, value: bar.inboundValue ?? '--', colorClass: 'legend-dot--blue' },
      { label: rawLegend.value.outbound, value: bar.outboundValue ?? '--', colorClass: 'legend-dot--orange' },
      { label: rawLegend.value.stock, value: bar.stockValue ?? '--', colorClass: 'legend-dot--green' }
    ]
  }
  return [
    { label: rawLegend.value.inbound, value: bar.inboundQuantityValue ?? '--', colorClass: 'legend-dot--blue' },
    { label: rawLegend.value.outbound, value: bar.outboundQuantityValue ?? '--', colorClass: 'legend-dot--orange' },
    { label: rawLegend.value.stock, value: bar.stockQuantityValue ?? '--', colorClass: 'legend-dot--green' }
  ]
}

function getFinishedTooltipRows(bar: DashboardChartBar): ChartTooltipRow[] {
  if (finishedMonitorMode.value === 'quantity') {
    return [
      { label: finishedLegend.value.production, value: bar.productionValue ?? '--', colorClass: 'legend-dot--blue' },
      { label: finishedLegend.value.sales, value: bar.salesValue ?? '--', colorClass: 'legend-dot--orange' },
      { label: finishedLegend.value.stock, value: bar.stockValue ?? '--', colorClass: 'legend-dot--green' }
    ]
  }
  return [
    { label: finishedLegend.value.production, value: bar.productionAmountValue ?? '--', colorClass: 'legend-dot--blue' },
    { label: finishedLegend.value.sales, value: bar.salesAmountValue ?? '--', colorClass: 'legend-dot--orange' },
    { label: finishedLegend.value.stock, value: bar.stockAmountValue ?? '--', colorClass: 'legend-dot--green' }
  ]
}

function getProfitTooltipRows(bar: DashboardChartBar): ChartTooltipRow[] {
  return [
    { label: '销售收入', value: bar.productionValue ?? '--', colorClass: 'legend-dot--blue' },
    { label: '销售成本', value: bar.salesValue ?? '--', colorClass: 'legend-dot--orange' },
    { label: '毛利', value: bar.stockValue ?? '--', colorClass: 'legend-dot--green' }
  ]
}

function showChartTooltip(event: MouseEvent, chartType: ChartType, bar: DashboardChartBar) {
  const payload: ChartTooltipPayload = chartType === 'raw'
    ? { title: bar.label, rows: getRawTooltipRows(bar) }
    : chartType === 'finished'
      ? { title: bar.label, rows: getFinishedTooltipRows(bar) }
      : { title: bar.label, rows: getProfitTooltipRows(bar) }

  chartTooltip.visible = true
  chartTooltip.title = payload.title
  chartTooltip.rows = payload.rows
  updateChartTooltipPosition(event)
}

function moveChartTooltip(event: MouseEvent) {
  if (!chartTooltip.visible) return
  updateChartTooltipPosition(event)
}

function updateChartTooltipPosition(event: MouseEvent) {
  const tooltipWidth = 240
  const tooltipHeight = Math.max(118, 52 + chartTooltip.rows.length * 28)
  const gap = 18
  const viewportPadding = 12
  const canPlaceRight = event.clientX + gap + tooltipWidth <= window.innerWidth - viewportPadding
  const x = canPlaceRight
    ? event.clientX + gap
    : Math.max(viewportPadding, event.clientX - gap - tooltipWidth)
  const maxY = window.innerHeight - viewportPadding - tooltipHeight
  const middleY = event.clientY - tooltipHeight / 2
  const y = Math.max(viewportPadding, Math.min(maxY, middleY))

  chartTooltip.x = x
  chartTooltip.y = y
  chartTooltip.placement = canPlaceRight ? 'right' : 'left'
  chartTooltip.align = y === viewportPadding ? 'top' : 'middle'
}

function hideChartTooltip() {
  chartTooltip.visible = false
}

function formatNumber(value: number | null | undefined) {
  return value === null || value === undefined
    ? '--'
    : new Intl.NumberFormat('zh-CN').format(value)
}

function formatDashboardNumber(value: number | null | undefined) {
  return formatNumber(value)
}

function parseCurrencyAmount(value: string | null | undefined) {
  if (!value) return 0
  const parsed = Number(value.replace(/[¥,\s]/g, ''))
  return Number.isFinite(parsed) ? parsed : 0
}

function amountSummary(items: { value: string }[]) {
  const total = items.reduce((sum, item) => sum + parseCurrencyAmount(item.value), 0)
  return `¥${new Intl.NumberFormat('zh-CN').format(Math.round(total))}`
}

function parseDisplayNumber(value: string | null | undefined) {
  if (!value) return 0
  const normalized = value.replace(/[¥,件kgKG个袋盒包\s]/g, '')
  const parsed = Number(normalized)
  return Number.isFinite(parsed) ? parsed : 0
}

function toPercent(value: number, maxValue: number) {
  if (maxValue <= 0 || value <= 0) return '0%'
  return `${Math.max(4, Math.min(100, Math.round((value / maxValue) * 100)))}%`
}

function normalizeRawMaterialQuantityBars(bars: typeof rawMaterialBars.value) {
  const values = bars.flatMap(bar => [bar.inboundQuantityValue, bar.outboundQuantityValue, bar.stockQuantityValue].map(parseDisplayNumber))
  const maxValue = Math.max(0, ...values)
  return bars.map(bar => ({
    ...bar,
    inbound: toPercent(parseDisplayNumber(bar.inboundQuantityValue), maxValue),
    outbound: toPercent(parseDisplayNumber(bar.outboundQuantityValue), maxValue),
    stock: toPercent(parseDisplayNumber(bar.stockQuantityValue), maxValue)
  }))
}

function normalizeFinishedProductAmountBars(bars: typeof finishedProductBars.value) {
  const values = bars.flatMap(bar => [bar.productionAmountValue, bar.salesAmountValue, bar.stockAmountValue].map(parseDisplayNumber))
  const maxValue = Math.max(0, ...values)
  return bars.map(bar => ({
    ...bar,
    production: toPercent(parseDisplayNumber(bar.productionAmountValue), maxValue),
    sales: toPercent(parseDisplayNumber(bar.salesAmountValue), maxValue),
    stock: toPercent(parseDisplayNumber(bar.stockAmountValue), maxValue)
  }))
}

function buildAmountAxisTicks(bars: Array<{ inboundValue: string | null; outboundValue: string | null; stockValue: string | null }>) {
  const maxValue = Math.max(
    0,
    ...bars.flatMap(bar => [bar.inboundValue, bar.outboundValue, bar.stockValue].map(parseDisplayNumber))
  )
  const axisMax = getFriendlyAmountAxisMax(maxValue)
  return [axisMax, axisMax * 0.75, axisMax * 0.5, axisMax * 0.25, 0].map(formatAxisAmount)
}

function buildRawQuantityAxisTicks(bars: Array<{ inboundQuantityValue: string | null; outboundQuantityValue: string | null; stockQuantityValue: string | null }>) {
  const maxValue = Math.max(
    0,
    ...bars.flatMap(bar => [bar.inboundQuantityValue, bar.outboundQuantityValue, bar.stockQuantityValue].map(parseDisplayNumber))
  )
  const axisMax = getFriendlyQuantityAxisMax(maxValue)
  return [axisMax, axisMax * 0.75, axisMax * 0.5, axisMax * 0.25, 0].map(value => formatNumber(Math.round(value)))
}

function buildCountAxisTicks(bars: Array<{ productionValue: string | null; salesValue: string | null; stockValue: string | null }>) {
  const maxValue = Math.max(
    0,
    ...bars.flatMap(bar => [bar.productionValue, bar.salesValue, bar.stockValue].map(parseDisplayNumber))
  )
  const axisMax = getFriendlyQuantityAxisMax(maxValue)
  return [axisMax, axisMax * 0.75, axisMax * 0.5, axisMax * 0.25, 0].map(value => formatNumber(Math.round(value)))
}

function buildFinishedAmountAxisTicks(bars: Array<{ productionAmountValue: string | null; salesAmountValue: string | null; stockAmountValue: string | null }>) {
  const maxValue = Math.max(
    0,
    ...bars.flatMap(bar => [bar.productionAmountValue, bar.salesAmountValue, bar.stockAmountValue].map(parseDisplayNumber))
  )
  const axisMax = getFriendlyAmountAxisMax(maxValue)
  return [axisMax, axisMax * 0.75, axisMax * 0.5, axisMax * 0.25, 0].map(formatAxisAmount)
}

function getFriendlyAmountAxisMax(value: number) {
  const steps = [1000, 3000, 5000, 10000, 30000, 50000, 100000, 300000, 500000, 1000000]
  return steps.find(step => value <= step) ?? Math.ceil(value / 100000) * 100000
}

function getFriendlyQuantityAxisMax(value: number) {
  const steps = [10, 20, 50, 100, 200, 500, 1000, 2000, 5000, 10000]
  return steps.find(step => value <= step) ?? Math.ceil(value / 1000) * 1000
}

function formatAxisAmount(value: number) {
  if (value >= 10000) {
    return `${Number((value / 10000).toFixed(1)).toLocaleString('zh-CN')}万`
  }
  return formatNumber(Math.round(value))
}

function handleQuickAction(action: QuickActionType) {
  const routeMap: Record<QuickActionType, { name: string; query?: Record<string, string> }> = {
    purchase: { name: 'purchase', query: { action: 'create' } },
    materialIssue: { name: 'production', query: { action: 'material-issue' } },
    productionInbound: { name: 'production', query: { action: 'inbound' } },
    salesOutbound: { name: 'sales' },
    inventoryCheck: { name: 'inventory', query: { action: 'check' } },
    stockLoss: { name: 'inventory', query: { action: 'loss' } }
  }
  router.push(routeMap[action])
}

async function loadDashboard() {
  overviewLoading.value = true
  try {
    dashboardData.value = await inventoryApi.getDashboard()
  } catch (error) {
    console.error('加载首页看板失败:', error)
  } finally {
    overviewLoading.value = false
  }
}

onMounted(() => {
  loadDashboard()
})
</script>

<style scoped>
.dashboard-page {
  width: 100%;
}

.dashboard-shell {
  width: 100%;
  max-width: none;
  min-height: auto;
  margin: 0;
  padding: 12px;
  display: grid;
  gap: 10px;
  background: transparent;
}

.card-surface {
  background: #fff;
  border: 1px solid #e6ecf5;
  border-radius: 16px;
  box-shadow: 0 6px 18px rgba(15, 23, 42, 0.04);
}

.dashboard-hero {
  padding: 14px 16px;
  background: linear-gradient(180deg, #ffffff 0%, #f8fbff 100%);
}

.hero-badge-row {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  min-height: 28px;
  padding: 0 10px;
  border-radius: 999px;
  background: #eef2f7;
  color: #475569;
  font-size: 12px;
  font-weight: 700;
}

.hero-badge--primary {
  background: #e8f1ff;
  color: #2563eb;
}

.eyebrow {
  margin: 0 0 6px;
  font-size: 13px;
  color: #64748b;
}

.page-title {
  margin: 0;
  font-size: 30px;
  line-height: 1.15;
  color: #0f172a;
}

.page-description {
  margin: 6px 0 0;
  max-width: 860px;
  color: #64748b;
  line-height: 1.55;
}

.top-panels,
.metric-grid,
.split-grid {
  display: grid;
  gap: 10px;
}

.top-panels {
  grid-template-columns: 1fr;
  align-items: start;
}

.panel-card,
.section-card,
.metric-card {
  padding: 12px;
}

.panel-header,
.section-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.panel-header h2,
.section-card h2 {
  margin: 0;
  font-size: 18px;
  line-height: 1.2;
  color: #1e293b;
}

.section-card__header p {
  margin: 6px 0 0;
  font-size: 13px;
  color: #64748b;
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.overview-card {
  min-width: 0;
  min-height: 112px;
  padding: 14px 12px;
  border-radius: 12px;
  border: 1px solid #edf2f7;
  display: grid;
  align-content: start;
  overflow: hidden;
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.35);
}

.theme-blue {
  background: linear-gradient(180deg, #f4f8ff 0%, #ffffff 100%);
  border-color: #d7e7ff;
}

.theme-orange {
  background: linear-gradient(180deg, #fff8ef 0%, #ffffff 100%);
  border-color: #fde7c5;
}

.theme-green {
  background: linear-gradient(180deg, #f1fff7 0%, #ffffff 100%);
  border-color: #d3f3df;
}

.theme-purple {
  background: linear-gradient(180deg, #faf5ff 0%, #ffffff 100%);
  border-color: #ead9ff;
}

.overview-card__title {
  font-size: 12px;
  font-weight: 700;
  color: #475569;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.overview-card__value {
  margin-top: 8px;
  font-size: clamp(20px, 2vw, 28px);
  line-height: 1.12;
  font-weight: 700;
  color: #0f172a;
  letter-spacing: -0.04em;
  white-space: normal;
  overflow-wrap: anywhere;
}

.overview-card__subtitle {
  margin-top: 7px;
  font-size: 11px;
  color: #64748b;
}

.overview-card__change {
  margin-top: 9px;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  color: #64748b;
}

.metric-grid {
  grid-template-columns: repeat(6, minmax(0, 1fr));
}

.metric-grid--top {
  order: 2;
}

.metric-card {
  min-width: 0;
  display: grid;
  gap: 10px;
  min-height: 116px;
  padding: 16px 16px 14px;
  overflow: hidden;
}

.metric-title-row,
.metric-value-row {
  min-width: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.metric-mini-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 24px;
  width: 24px;
  height: 24px;
  border-radius: 999px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  font-size: 12px;
  font-weight: 800;
}

.metric-title {
  min-width: 0;
  color: #64748b;
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.metric-value {
  min-width: 0;
  flex: 1;
  font-size: clamp(22px, 2vw, 30px);
  font-weight: 700;
  color: #0f172a;
  line-height: 1.08;
  letter-spacing: -0.035em;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.metric-subtitle {
  color: #94a3b8;
  font-size: 11px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.metric-change-chip,
.trend-up,
.trend-down {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 auto;
  max-width: 58px;
  min-height: 22px;
  padding: 0 6px;
  border-radius: 999px;
  font-size: 10px;
  line-height: 1;
  font-weight: 700;
  white-space: nowrap;
}

.metric-progress {
  height: 6px;
  background: #eef2f7;
  border-radius: 999px;
  overflow: hidden;
}

.metric-progress i {
  display: block;
  height: 100%;
  border-radius: 999px;
}

.trend-up {
  color: #16a34a;
  background: #f0fdf4;
}

.trend-down {
  color: #ef4444;
  background: #fef2f2;
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 10px;
}

.quick-action {
  padding: 12px 10px;
  background: #fff;
  border: 1px solid #edf2f7;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.2s ease;
}

.quick-action:hover {
  border-color: #dbe5f1;
  box-shadow: 0 6px 18px rgba(37, 99, 235, 0.08);
}

.quick-action__icon {
  flex: 0 0 auto;
  width: 34px;
  height: 34px;
  border-radius: 12px;
  display: grid;
  place-items: center;
  background: #f8fafc;
}

.quick-action__icon :deep(svg) {
  width: 18px;
  height: 18px;
}

.quick-action__label {
  min-width: 0;
  font-size: 12px;
  color: #334155;
  font-weight: 600;
  white-space: nowrap;
}

.is-blue {
  color: #2563eb;
}

.is-orange {
  color: #f59e0b;
}

.is-green {
  color: #22c55e;
}

.is-purple {
  color: #7c3aed;
}

.is-cyan {
  color: #06b6d4;
}

.is-red {
  color: #ef4444;
}

.split-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.section-card--chart {
  min-height: 640px;
}

.section-card__header--tight {
  margin-bottom: 12px;
}

.segmented-control {
  display: inline-flex;
  padding: 2px;
  border: 1px solid #dce7f3;
  border-radius: 10px;
  background: #f8fbff;
}

.segmented-control button {
  height: 30px;
  padding: 0 12px;
  border: none;
  border-radius: 8px;
  background: transparent;
  color: #64748b;
  font-size: 12px;
  font-weight: 700;
  transition: all 0.22s ease;
}

.segmented-control button:hover {
  color: #334155;
}

.segmented-control .is-active {
  background: linear-gradient(180deg, #ffffff 0%, #f6f9ff 100%);
  color: #2563eb;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.12), 0 1px 2px rgba(15, 23, 42, 0.08);
}

.chart-placeholder {
  position: relative;
  border: 1px solid #edf2f7;
  border-radius: 12px;
  background: linear-gradient(180deg, #ffffff 0%, #fbfdff 100%);
  padding: 12px 14px 14px;
}

.chart-placeholder--large {
  height: 280px;
  margin-bottom: 16px;
}

.chart-legend {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  margin-bottom: 10px;
  color: #64748b;
  font-size: 12px;
}

.chart-legend span {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.legend-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  display: inline-block;
}

.legend-dot--blue {
  background: #2563eb;
}

.legend-dot--orange {
  background: #f97316;
}

.legend-dot--green {
  background: #22c55e;
}

.chart-panel {
  display: grid;
  grid-template-columns: 48px 1fr;
  gap: 10px;
  height: calc(100% - 32px);
}

.chart-axis {
  display: grid;
  align-content: space-between;
  justify-items: end;
  padding: 4px 0 22px;
  color: #94a3b8;
  font-size: 11px;
}

.chart-bars {
  position: relative;
  height: 100%;
  display: flex;
  align-items: end;
  justify-content: space-between;
  gap: 10px;
  padding-top: 4px;
  background-image: linear-gradient(to top, rgba(226, 232, 240, 0.75) 1px, transparent 1px);
  background-size: 100% 25%;
}

.detail-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(220px, 0.8fr);
  gap: 14px;
  align-items: start;
}

.summary-stack {
  display: grid;
  gap: 10px;
}

.summary-mini-card {
  padding: 14px 14px 12px;
  border: 1px solid #edf2f7;
  border-radius: 12px;
  background: linear-gradient(180deg, #fbfdff 0%, #ffffff 100%);
}

.summary-mini-card__label {
  display: block;
  font-size: 12px;
  color: #64748b;
}

.summary-mini-card__value {
  display: block;
  margin-top: 8px;
  font-size: 22px;
  color: #0f172a;
}

.summary-mini-card__meta {
  display: block;
  margin-top: 6px;
  font-size: 12px;
  color: #94a3b8;
}

.bar-item {
  position: relative;
  z-index: 2;
  display: grid;
  justify-items: center;
  gap: 8px;
  flex: 1;
}

.bar-item__column {
  position: relative;
  display: flex;
  align-items: end;
  gap: 6px;
  height: 188px;
}

.floating-chart-tooltip {
  position: fixed;
  min-width: 210px;
  max-width: 240px;
  padding: 12px 14px;
  border-radius: 14px;
  background: rgba(15, 23, 42, 0.96);
  color: #fff;
  box-shadow: 0 18px 42px rgba(15, 23, 42, 0.28);
  display: grid;
  gap: 8px;
  pointer-events: none;
  z-index: 9999;
  backdrop-filter: blur(10px);
}

.floating-chart-tooltip.is-right {
  transform: none;
}

.floating-chart-tooltip.is-left {
  transform: none;
}

.floating-chart-tooltip strong {
  font-size: 14px;
  font-weight: 800;
  color: #f8fafc;
}

.floating-chart-tooltip em {
  display: grid;
  grid-template-columns: auto 1fr auto;
  align-items: center;
  gap: 7px;
  font-style: normal;
  font-size: 12px;
  color: #e2e8f0;
  white-space: nowrap;
}

.floating-chart-tooltip b {
  color: #fff;
  font-weight: 800;
}

.bar-item:hover .bar-item__column > i {
  filter: brightness(1.08);
  box-shadow: 0 8px 18px rgba(37, 99, 235, 0.16);
}

.bar-item:hover .bar-item__label {
  color: #2563eb;
  font-weight: 800;
}

.bar-item__column > i {
  display: block;
  width: 12px;
  border-radius: 999px 999px 0 0;
  transition: height 0.28s ease, bottom 0.28s ease, transform 0.28s ease, box-shadow 0.28s ease;
}

.bar-item__blue {
  background: #2f7cf6;
}

.bar-item__orange {
  background: #f59e0b;
}

.bar-item__green.line-dot {
  position: absolute;
  left: 50%;
  width: 10px;
  height: 10px;
  margin-left: 9px;
  transform: translateX(-50%);
  border-radius: 50%;
  background: #22c55e;
  box-shadow: 0 0 0 3px rgba(34, 197, 94, 0.12);
}

.bar-item__green.line-dot:hover {
  transform: translateX(-50%) scale(1.2);
  box-shadow: 0 0 0 5px rgba(34, 197, 94, 0.16);
}

.chart-line {
  position: absolute;
  left: 6%;
  right: 6%;
  top: 62px;
  bottom: 44px;
  z-index: 1;
  border-top: 3px solid #22c55e;
  opacity: 0.65;
}

.chart-line--raw {
  clip-path: polygon(0 52%, 14% 48%, 28% 20%, 42% 38%, 56% 40%, 70% 50%, 84% 48%, 100% 46%, 100% 54%, 84% 56%, 70% 58%, 56% 48%, 42% 46%, 28% 28%, 14% 56%, 0 60%);
}

.chart-line--finished {
  clip-path: polygon(0 58%, 14% 52%, 28% 78%, 42% 44%, 56% 68%, 70% 38%, 84% 26%, 100% 62%, 100% 70%, 84% 34%, 70% 46%, 56% 74%, 42% 50%, 28% 84%, 14% 58%, 0 64%);
}

.bar-item__label {
  font-size: 12px;
  color: #64748b;
}

.table-card,
.ranking-card {
  display: grid;
  gap: 10px;
}

.profit-section {
  display: grid;
  gap: 16px;
}

.profit-headline {
  display: grid;
  justify-items: end;
  gap: 4px;
}

.profit-headline span {
  font-size: 12px;
  color: #64748b;
}

.profit-headline strong {
  font-size: 24px;
  color: #16a34a;
}

.profit-metric-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.profit-metric-card {
  padding: 14px;
  border: 1px solid #e8eef8;
  border-radius: 14px;
  background: linear-gradient(180deg, #fbfdff 0%, #ffffff 100%);
  display: grid;
  gap: 8px;
}

.profit-metric-card span {
  font-size: 12px;
  color: #64748b;
}

.profit-metric-card strong {
  font-size: 22px;
  color: #0f172a;
}

.profit-metric-card small {
  color: #64748b;
  font-size: 12px;
}

.profit-metric-card i {
  display: block;
  width: 0;
  height: 6px;
  border-radius: 999px;
}

.profit-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.4fr) minmax(320px, 1fr) minmax(240px, 300px);
  gap: 14px;
}

.profit-chart-card,
.profit-ranking-card {
  padding: 14px;
  border: 1px solid #edf2f7;
  border-radius: 14px;
  background: #fff;
}

.profit-chart-panel {
  min-height: 260px;
}

.profit-summary-stack {
  gap: 12px;
}

.profit-empty {
  color: #94a3b8;
  font-size: 13px;
  padding: 20px 0;
}

.table-card__title {
  font-size: 15px;
  font-weight: 700;
  color: #1e293b;
}

.table-card table {
  width: 100%;
  border-collapse: collapse;
}

.table-card th,
.table-card td {
  padding: 10px 0;
  border-bottom: 1px solid #eef2f7;
  text-align: left;
  font-size: 13px;
}

.status-badge {
  padding: 3px 8px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
}

.is-danger {
  color: #dc2626;
  background: #fef2f2;
}

.is-warn {
  color: #d97706;
  background: #fff7ed;
}

.ranking-list,
.category-list {
  display: grid;
  gap: 12px;
}

.ranking-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.ranking-item__index {
  width: 18px;
  font-size: 14px;
  color: #f59e0b;
  font-weight: 800;
}

.ranking-item__body {
  flex: 1;
}

.ranking-item__title {
  margin-bottom: 6px;
  color: #1e293b;
  font-size: 13px;
}

.ranking-item__track,
.category-row__track {
  height: 7px;
  background: #eef2f7;
  border-radius: 999px;
  overflow: hidden;
}

.ranking-item__track i,
.category-row__track i {
  display: block;
  height: 100%;
  border-radius: 999px;
}

.ranking-item__track i {
  background: linear-gradient(90deg, #2f7cf6 0%, #2563eb 100%);
}

.ranking-item__value {
  min-width: 72px;
  text-align: right;
  color: #475569;
  font-size: 13px;
}

.category-row {
  padding: 12px 14px;
  border: 1px solid #edf2f7;
  border-radius: 12px;
  background: linear-gradient(180deg, #fbfdff 0%, #ffffff 100%);
  display: grid;
  gap: 10px;
}

.category-row__main,
.category-row__footer,
.category-summary-line {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.category-row__main {
  justify-content: flex-start;
}

.category-row__dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
}

.category-row__name {
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
}

.category-row__ratio {
  margin-left: auto;
  font-size: 12px;
  color: #64748b;
}

.category-row__footer span,
.category-row__meta span {
  font-size: 12px;
  color: #94a3b8;
}

.category-row__meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.category-row__footer strong,
.category-summary-line strong {
  font-size: 14px;
  color: #0f172a;
}

.category-summary-line {
  margin-top: 14px;
  padding-top: 14px;
  border-top: 1px solid #eef2f7;
  color: #64748b;
  font-size: 13px;
}

.table-link {
  justify-self: end;
  color: #2563eb;
  text-decoration: none;
  font-size: 13px;
  font-weight: 700;
}

.table-link--bottom {
  margin-top: 6px;
}

@media (max-width: 1500px) {
  .top-panels,
  .split-grid {
    grid-template-columns: 1fr;
  }

  .overview-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 1400px) {
  .metric-grid {
    grid-template-columns: repeat(6, minmax(0, 1fr));
  }
}

@media (max-width: 1024px) {
  .overview-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .metric-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .quick-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .detail-grid,
  .profit-grid {
    grid-template-columns: 1fr;
  }

  .profit-metric-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .overview-grid,
  .metric-grid,
  .quick-grid,
  .profit-metric-grid {
    grid-template-columns: 1fr;
  }

  .panel-header h2,
  .section-card h2 {
    font-size: 20px;
  }

  .page-title {
    font-size: 24px;
  }

  .bar-item__column {
    gap: 4px;
  }

  .bar-item__column > i {
    width: 10px;
  }
}
</style>
