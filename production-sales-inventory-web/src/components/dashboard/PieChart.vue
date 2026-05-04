<template>
  <div
    ref="containerRef"
    class="pie-chart-container"
    @pointerleave="clearActiveItem"
  >
    <svg class="pie-chart" viewBox="0 0 200 200" role="img" aria-label="库存分布">
      <g transform="translate(100,100)">
        <circle v-if="segments.length === 0" r="74" fill="none" stroke="#E5E7EB" stroke-width="22" />
        <path
          v-for="segment in segments"
          :key="segment.label"
          :d="segment.path"
          :fill="segment.color"
          class="pie-segment"
          :class="{ 'is-active': activeLabel === segment.label }"
          tabindex="0"
          @pointerenter="handleItemPointerMove(segment.label, $event)"
          @pointermove="handleItemPointerMove(segment.label, $event)"
          @focus="handleKeyboardFocus(segment.label)"
          @blur="clearActiveItem"
        />
      </g>

      <text x="100" y="95" text-anchor="middle" font-size="16" font-weight="600" fill="#1F2937">
        {{ formatNumber(totalQuantity) }}
      </text>
      <text x="100" y="112" text-anchor="middle" font-size="12" fill="#6B7280">总库存</text>
    </svg>

    <div class="legend">
      <div v-if="legendItems.length === 0" class="empty-state">暂无库存分布数据</div>
      <div
        v-for="item in legendItems"
        v-else
        :key="item.label"
        class="legend-item"
        :class="{ 'is-active': activeLabel === item.label }"
        tabindex="0"
        @pointerenter="handleItemPointerMove(item.label, $event)"
        @pointermove="handleItemPointerMove(item.label, $event)"
        @focus="handleKeyboardFocus(item.label)"
        @blur="clearActiveItem"
      >
        <div class="legend-color" :style="{ background: item.color }"></div>
        <span class="legend-text">{{ item.label }}</span>
        <span class="legend-percent">{{ item.percentText }}</span>
        <span class="legend-count">{{ formatNumber(item.quantity) }}</span>
      </div>
    </div>

    <div
      v-if="activeItem"
      class="distribution-tooltip"
      :class="{ 'distribution-tooltip--left': tooltipPlacement === 'left' }"
      :style="{ left: `${tooltipPosition.x}px`, top: `${tooltipPosition.y}px` }"
    >
      <div class="tooltip-title">
        <span class="tooltip-swatch" :style="{ background: activeItem.color }"></span>
        {{ activeItem.label }}
      </div>
      <div class="tooltip-row">
        <span>库存数量</span>
        <strong>{{ formatNumber(activeItem.quantity) }}</strong>
      </div>
      <div class="tooltip-row">
        <span>商品数</span>
        <strong>{{ formatNumber(activeItem.productCount) }}</strong>
      </div>
      <div class="tooltip-row">
        <span>占比</span>
        <strong>{{ activeItem.percentText }}</strong>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { type InventoryDistributionItem } from '@/api/inventory'

const props = withDefaults(defineProps<{
  items?: InventoryDistributionItem[]
}>(), {
  items: () => []
})

const colors = ['#2563EB', '#16A34A', '#F59E0B', '#7C3AED', '#0F766E', '#E11D48', '#64748B']
const outerRadius = 80
const innerRadius = 55
const containerRef = ref<HTMLElement | null>(null)
const activeLabel = ref<string | null>(null)
const tooltipPosition = ref({ x: 0, y: 0 })
const tooltipPlacement = ref<'right' | 'left'>('right')

const totalQuantity = computed(() => {
  return props.items.reduce((sum, item) => sum + Math.max(item.quantity, 0), 0)
})

const totalProducts = computed(() => {
  return props.items.reduce((sum, item) => sum + item.productCount, 0)
})

const legendItems = computed(() => {
  const total = totalQuantity.value > 0 ? totalQuantity.value : totalProducts.value
  if (total <= 0) {
    return []
  }

  return props.items
    .filter((item) => totalQuantity.value > 0 ? item.quantity > 0 : item.productCount > 0)
    .map((item, index) => {
      const value = totalQuantity.value > 0 ? item.quantity : item.productCount
      return {
        label: item.category,
        quantity: item.quantity,
        productCount: item.productCount,
        color: colors[index % colors.length],
        percent: Math.round((value / total) * 100),
        percentText: formatPercent((value / total) * 100)
      }
    })
})

const segments = computed(() => {
  const total = totalQuantity.value > 0 ? totalQuantity.value : totalProducts.value
  if (total <= 0) {
    return []
  }

  let startAngle = -90
  return legendItems.value.map((item) => {
    const value = totalQuantity.value > 0 ? item.quantity : item.productCount
    const angle = (value / total) * 360
    const endAngle = startAngle + angle
    const path = describeArc(startAngle, endAngle)
    startAngle = endAngle
    return {
      label: item.label,
      color: item.color,
      quantity: item.quantity,
      productCount: item.productCount,
      percentText: item.percentText,
      path
    }
  })
})

const activeItem = computed(() => {
  return legendItems.value.find(item => item.label === activeLabel.value) ?? null
})

function describeArc(startAngle: number, endAngle: number) {
  const startOuter = polarToCartesian(outerRadius, endAngle)
  const endOuter = polarToCartesian(outerRadius, startAngle)
  const startInner = polarToCartesian(innerRadius, startAngle)
  const endInner = polarToCartesian(innerRadius, endAngle)
  const largeArcFlag = endAngle - startAngle > 180 ? 1 : 0

  return [
    `M ${startOuter.x} ${startOuter.y}`,
    `A ${outerRadius} ${outerRadius} 0 ${largeArcFlag} 0 ${endOuter.x} ${endOuter.y}`,
    `L ${startInner.x} ${startInner.y}`,
    `A ${innerRadius} ${innerRadius} 0 ${largeArcFlag} 1 ${endInner.x} ${endInner.y}`,
    'Z'
  ].join(' ')
}

function polarToCartesian(radius: number, angleInDegrees: number) {
  const angleInRadians = (angleInDegrees * Math.PI) / 180
  return {
    x: Number((radius * Math.cos(angleInRadians)).toFixed(3)),
    y: Number((radius * Math.sin(angleInRadians)).toFixed(3))
  }
}

function formatNumber(value: number) {
  return new Intl.NumberFormat('zh-CN').format(value)
}

function formatPercent(value: number) {
  if (value > 0 && value < 1) {
    return '<1%'
  }

  return `${Number.isInteger(value) ? value.toFixed(0) : value.toFixed(1)}%`
}

function handleItemPointerMove(label: string, event: PointerEvent) {
  activeLabel.value = label
  updateTooltipPosition(event.clientX, event.clientY)
}

function handleKeyboardFocus(label: string) {
  activeLabel.value = label
  const container = containerRef.value
  if (!container) {
    return
  }

  tooltipPosition.value = {
    x: container.clientWidth / 2,
    y: container.clientHeight / 2
  }
  tooltipPlacement.value = 'right'
}

function clearActiveItem() {
  activeLabel.value = null
}

function updateTooltipPosition(clientX: number, clientY: number) {
  const container = containerRef.value
  if (!container) {
    return
  }

  const rect = container.getBoundingClientRect()
  const x = clientX - rect.left
  const y = clientY - rect.top
  tooltipPosition.value = {
    x: Math.max(8, Math.min(rect.width - 8, x)),
    y: Math.max(12, Math.min(rect.height - 12, y))
  }
  tooltipPlacement.value = x > rect.width * 0.68 ? 'left' : 'right'
}
</script>

<style scoped>
.pie-chart-container {
  position: relative;
  display: flex;
  height: 100%;
  gap: 20px;
}

.pie-chart {
  width: 120px;
  height: 120px;
  flex-shrink: 0;
}

.pie-segment {
  cursor: pointer;
  transition: opacity 0.18s ease, transform 0.18s ease, filter 0.18s ease;
  transform-box: fill-box;
  transform-origin: center;
  outline: none;
}

.pie-segment:hover,
.pie-segment:focus-visible,
.pie-segment.is-active {
  opacity: 0.92;
  filter: drop-shadow(0 4px 8px rgba(15, 23, 42, 0.18));
  transform: scale(1.035);
}

.legend {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  min-height: 24px;
  padding: 2px 4px;
  border-radius: 6px;
  outline: none;
  cursor: pointer;
  transition: background 0.18s ease;
}

.legend-item:hover,
.legend-item:focus-visible,
.legend-item.is-active {
  background: #F8FAFC;
}

.legend-color {
  width: 12px;
  height: 12px;
  border-radius: 2px;
}

.legend-text {
  flex: 1;
  color: #6B7280;
}

.legend-percent {
  color: #1F2937;
  font-weight: 500;
}

.legend-count {
  color: #9CA3AF;
}

.empty-state {
  color: #9CA3AF;
  font-size: 13px;
}

.distribution-tooltip {
  position: absolute;
  z-index: 4;
  min-width: 136px;
  padding: 10px 12px;
  border: 1px solid rgba(203, 213, 225, 0.82);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.14);
  color: #1E293B;
  font-size: 12px;
  line-height: 1.2;
  pointer-events: none;
  transform: translate(12px, -50%);
}

.distribution-tooltip--left {
  transform: translate(calc(-100% - 12px), -50%);
}

.tooltip-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  color: #334155;
  font-weight: 700;
}

.tooltip-swatch {
  width: 9px;
  height: 9px;
  border-radius: 999px;
}

.tooltip-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  color: #64748B;
}

.tooltip-row + .tooltip-row {
  margin-top: 6px;
}

.tooltip-row strong {
  color: #0F172A;
  font-weight: 700;
}
</style>
