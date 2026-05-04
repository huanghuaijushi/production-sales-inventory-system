<template>
  <div class="line-chart-wrap">
    <div v-if="loading" class="chart-state">加载中...</div>
    <div
      v-else
      ref="chartCanvasRef"
      class="chart-canvas"
      @pointermove="handlePointerMove"
      @pointerleave="clearActivePoint"
      @pointerdown="handlePointerMove"
    >
      <svg class="line-chart" viewBox="0 0 420 190" preserveAspectRatio="none">
        <rect x="0" y="0" width="420" height="190" fill="#ffffff" />

        <g class="grid-lines">
          <line
            v-for="tick in yTicks"
            :key="tick.id"
            :x1="chart.left"
            :y1="tick.y"
            :x2="chart.right"
            :y2="tick.y"
          />
        </g>

        <line class="axis-line" :x1="chart.left" :y1="chart.bottom" :x2="chart.right" :y2="chart.bottom" />
        <line class="axis-line" :x1="chart.left" :y1="chart.top" :x2="chart.left" :y2="chart.bottom" />

        <polyline class="trend-line trend-line--in" :points="inboundPoints" />
        <polyline class="trend-line trend-line--out" :points="outboundPoints" />

        <line
          v-if="activeX !== null"
          class="hover-guide"
          :x1="activeX"
          :y1="chart.top"
          :x2="activeX"
          :y2="chart.bottom"
        />

        <g v-if="showPoints">
          <circle
            v-for="point in inboundPointList"
            :key="`in-${point.x}-${point.y}`"
            class="trend-point trend-point--in"
            :cx="point.x"
            :cy="point.y"
            r="3"
          />
          <circle
            v-for="point in outboundPointList"
            :key="`out-${point.x}-${point.y}`"
            class="trend-point trend-point--out"
            :cx="point.x"
            :cy="point.y"
            r="3"
          />
        </g>

        <g v-if="activeInboundPoint && activeOutboundPoint">
          <circle
            class="active-point active-point--in"
            :cx="activeInboundPoint.x"
            :cy="activeInboundPoint.y"
            r="5"
          />
          <circle
            class="active-point active-point--out"
            :cx="activeOutboundPoint.x"
            :cy="activeOutboundPoint.y"
            r="5"
          />
        </g>
      </svg>

      <div class="trend-legend">
        <span class="legend-item">
          <span class="legend-swatch legend-swatch--in"></span>
          入库
        </span>
        <span class="legend-item">
          <span class="legend-swatch legend-swatch--out"></span>
          出库
        </span>
      </div>

      <div class="y-axis-labels">
        <span
          v-for="tick in yTicks"
          :key="`value-${tick.id}`"
          :style="{ top: toSvgPercent(tick.y, chart.viewHeight) }"
        >
          {{ tick.label }}
        </span>
      </div>

      <div class="x-axis-labels">
        <span
          v-for="label in xLabels"
          :key="label.x"
          :style="{ left: toSvgPercent(label.x, chart.viewWidth) }"
        >
          {{ label.text }}
        </span>
      </div>

      <div
        v-if="activeTooltip"
        class="trend-tooltip"
        :class="{ 'trend-tooltip--left': activeTooltip.x > chart.viewWidth * 0.72 }"
        :style="{ left: toSvgPercent(activeTooltip.x, chart.viewWidth), top: toSvgPercent(activeTooltip.y, chart.viewHeight) }"
      >
        <div class="tooltip-date">{{ activeTooltip.item.date }}</div>
        <div class="tooltip-row">
          <span><span class="tooltip-dot tooltip-dot--in"></span>入库</span>
          <strong>{{ formatQuantity(activeTooltip.item.inboundQuantity) }}</strong>
        </div>
        <div class="tooltip-row">
          <span><span class="tooltip-dot tooltip-dot--out"></span>出库</span>
          <strong>{{ formatQuantity(activeTooltip.item.outboundQuantity) }}</strong>
        </div>
        <div class="tooltip-row tooltip-row--net">
          <span>净变化</span>
          <strong :class="getNetChangeClass(activeTooltip.item.netChangeQuantity)">
            {{ formatSignedQuantity(activeTooltip.item.netChangeQuantity) }}
          </strong>
        </div>
      </div>

      <p v-if="!hasBusinessData" class="empty-text">暂无出入库记录</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { type StockTrendItem } from '@/api/inventory'

const props = defineProps<{
  items: StockTrendItem[]
  loading?: boolean
}>()

const chart = {
  viewWidth: 420,
  viewHeight: 190,
  left: 38,
  right: 400,
  top: 28,
  bottom: 148
}

const chartWidth = chart.right - chart.left
const chartHeight = chart.bottom - chart.top

const chartCanvasRef = ref<HTMLElement | null>(null)
const activeIndex = ref<number | null>(null)
const normalizedItems = computed(() => props.items ?? [])

const maxValue = computed(() => {
  const max = normalizedItems.value.reduce((result, item) => {
    return Math.max(result, item.inboundQuantity, item.outboundQuantity)
  }, 0)
  return Math.max(1, max)
})

const hasBusinessData = computed(() => {
  return normalizedItems.value.some(item => item.inboundQuantity > 0 || item.outboundQuantity > 0)
})

const inboundPointList = computed(() => {
  return normalizedItems.value.map((item, index) => ({
    x: getPointX(index),
    y: getPointY(item.inboundQuantity)
  }))
})

const outboundPointList = computed(() => {
  return normalizedItems.value.map((item, index) => ({
    x: getPointX(index),
    y: getPointY(item.outboundQuantity)
  }))
})

const inboundPoints = computed(() => formatPoints(inboundPointList.value))
const outboundPoints = computed(() => formatPoints(outboundPointList.value))
const showPoints = computed(() => normalizedItems.value.length <= 30)
const activeItem = computed(() => {
  if (activeIndex.value === null) {
    return null
  }

  return normalizedItems.value[activeIndex.value] ?? null
})

const activeInboundPoint = computed(() => {
  if (activeIndex.value === null) {
    return null
  }

  return inboundPointList.value[activeIndex.value] ?? null
})

const activeOutboundPoint = computed(() => {
  if (activeIndex.value === null) {
    return null
  }

  return outboundPointList.value[activeIndex.value] ?? null
})

const activeX = computed(() => activeInboundPoint.value?.x ?? null)
const activeTooltip = computed(() => {
  const item = activeItem.value
  const inPoint = activeInboundPoint.value
  const outPoint = activeOutboundPoint.value
  if (!item || !inPoint || !outPoint) {
    return null
  }

  return {
    item,
    x: inPoint.x,
    y: Math.max(chart.top + 12, Math.min(inPoint.y, outPoint.y) - 18)
  }
})

const yTicks = computed(() => {
  const half = Math.ceil(maxValue.value / 2)
  return [maxValue.value, half, 0].map((value, index) => ({
    id: `${index}-${value}`,
    label: String(value),
    y: getPointY(value)
  }))
})

const xLabels = computed(() => {
  const items = normalizedItems.value
  if (items.length === 0) {
    return []
  }

  const maxLabelCount = items.length <= 7 ? items.length : 5
  if (maxLabelCount === 1) {
    return [{ text: items[0]?.label ?? '', x: getPointX(0), index: 0 }]
  }

  const step = Math.max(1, Math.ceil((items.length - 1) / (maxLabelCount - 1)))
  return items
    .map((item, index) => ({ text: item.label, x: getPointX(index), index }))
    .filter(item => item.index === 0 || item.index === items.length - 1 || item.index % step === 0)
})

function getPointX(index: number) {
  const itemCount = normalizedItems.value.length
  if (itemCount <= 1) {
    return chart.left
  }

  return chart.left + (chartWidth / (itemCount - 1)) * index
}

function getPointY(value: number) {
  return chart.bottom - (value / maxValue.value) * chartHeight
}

function formatPoints(points: Array<{ x: number; y: number }>) {
  return points.map(point => `${point.x.toFixed(1)},${point.y.toFixed(1)}`).join(' ')
}

function toSvgPercent(value: number, total: number) {
  return `${(value / total) * 100}%`
}

function handlePointerMove(event: PointerEvent) {
  const canvas = chartCanvasRef.value
  const itemCount = normalizedItems.value.length
  if (!canvas || itemCount === 0) {
    return
  }

  const rect = canvas.getBoundingClientRect()
  const relativeX = ((event.clientX - rect.left) / rect.width) * chart.viewWidth
  const clampedX = Math.max(chart.left, Math.min(chart.right, relativeX))

  if (itemCount === 1) {
    activeIndex.value = 0
    return
  }

  const pointStep = chartWidth / (itemCount - 1)
  activeIndex.value = Math.max(0, Math.min(itemCount - 1, Math.round((clampedX - chart.left) / pointStep)))
}

function clearActivePoint() {
  activeIndex.value = null
}

function formatQuantity(value: number) {
  return new Intl.NumberFormat('zh-CN').format(value)
}

function formatSignedQuantity(value: number) {
  if (value === 0) {
    return '0'
  }

  return `${value > 0 ? '+' : ''}${formatQuantity(value)}`
}

function getNetChangeClass(value: number) {
  return {
    'is-positive': value > 0,
    'is-negative': value < 0
  }
}
</script>

<style scoped>
.line-chart-wrap {
  position: relative;
  width: 100%;
  height: 100%;
}

.chart-canvas {
  position: relative;
  width: 100%;
  height: 100%;
  touch-action: none;
}

.line-chart {
  display: block;
  width: 100%;
  height: 100%;
}

.chart-state {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  color: #64748b;
  font-size: 13px;
}

.grid-lines line {
  stroke: #eef2f7;
  stroke-width: 1;
}

.axis-line {
  stroke: #cbd5e1;
  stroke-width: 1;
}

.trend-line {
  fill: none;
  stroke-width: 2.5;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.trend-line--in {
  stroke: #2563eb;
}

.trend-line--out {
  stroke: #16a34a;
}

.trend-point {
  stroke: #ffffff;
  stroke-width: 1.5;
}

.trend-point--in {
  fill: #2563eb;
}

.trend-point--out {
  fill: #16a34a;
}

.hover-guide {
  stroke: #94a3b8;
  stroke-dasharray: 4 4;
  stroke-width: 1;
}

.active-point {
  stroke: #ffffff;
  stroke-width: 2;
  filter: drop-shadow(0 2px 4px rgba(15, 23, 42, 0.2));
}

.active-point--in {
  fill: #2563eb;
}

.active-point--out {
  fill: #16a34a;
}

.trend-legend {
  position: absolute;
  top: 0;
  right: 24px;
  display: flex;
  align-items: center;
  gap: 28px;
  color: #475569;
  font-size: 13px;
  font-weight: 600;
  line-height: 1;
  pointer-events: none;
}

.legend-item {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  white-space: nowrap;
}

.legend-swatch {
  width: 16px;
  height: 10px;
  border-radius: 3px;
}

.legend-swatch--in {
  background: #2563eb;
}

.legend-swatch--out {
  background: #16a34a;
}

.y-axis-labels,
.x-axis-labels {
  position: absolute;
  inset: 0;
  color: #94a3b8;
  font-size: 12px;
  font-weight: 500;
  line-height: 1;
  pointer-events: none;
}

.y-axis-labels span {
  position: absolute;
  left: 0;
  min-width: 30px;
  text-align: right;
  transform: translateY(-50%);
}

.x-axis-labels span {
  position: absolute;
  top: 89.5%;
  transform: translateX(-50%);
  white-space: nowrap;
}

.trend-tooltip {
  position: absolute;
  z-index: 3;
  min-width: 132px;
  padding: 10px 12px;
  border: 1px solid rgba(203, 213, 225, 0.8);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.14);
  color: #1e293b;
  font-size: 12px;
  line-height: 1.2;
  pointer-events: none;
  transform: translate(12px, -50%);
}

.trend-tooltip--left {
  transform: translate(calc(-100% - 12px), -50%);
}

.tooltip-date {
  margin-bottom: 8px;
  color: #334155;
  font-size: 12px;
  font-weight: 700;
}

.tooltip-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  color: #64748b;
}

.tooltip-row + .tooltip-row {
  margin-top: 6px;
}

.tooltip-row span {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.tooltip-row strong {
  color: #0f172a;
  font-weight: 700;
}

.tooltip-row--net {
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid #e2e8f0;
}

.tooltip-dot {
  width: 7px;
  height: 7px;
  border-radius: 999px;
}

.tooltip-dot--in {
  background: #2563eb;
}

.tooltip-dot--out {
  background: #16a34a;
}

.tooltip-row strong.is-positive {
  color: #2563eb;
}

.tooltip-row strong.is-negative {
  color: #dc2626;
}

.empty-text {
  position: absolute;
  top: 45%;
  left: 50%;
  margin: 0;
  color: #94a3b8;
  font-size: 13px;
  transform: translate(-50%, -50%);
  pointer-events: none;
}
</style>
