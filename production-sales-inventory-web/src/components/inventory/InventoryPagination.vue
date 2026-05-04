<template>
  <section class="inventory-pagination-card">
    <div class="pagination-summary">
      <p>第 {{ page + 1 }} 页 / 共 {{ totalPages }} 页</p>
      <p>当前显示 {{ displayStart }} - {{ displayEnd }} 条，共 {{ total }} 条库存记录</p>
    </div>

    <div class="pagination-controls">
      <button
        type="button"
        class="page-btn"
        :disabled="page <= 0"
        @click="changePage(page - 1)"
      >
        上一页
      </button>

      <button
        v-for="pageNumber in pageNumbers"
        :key="pageNumber"
        type="button"
        class="page-btn"
        :class="{ active: pageNumber === page + 1 }"
        @click="changePage(pageNumber - 1)"
      >
        {{ pageNumber }}
      </button>

      <button
        type="button"
        class="page-btn"
        :disabled="page >= totalPages - 1"
        @click="changePage(page + 1)"
      >
        下一页
      </button>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps({
  total: {
    type: Number,
    required: true
  },
  page: {
    type: Number,
    required: true
  },
  pageSize: {
    type: Number,
    required: true
  }
})

const emit = defineEmits<{
  (e: 'update:page', page: number): void
}>()

const totalPages = computed(() => Math.max(1, Math.ceil(props.total / props.pageSize)))

const pageNumbers = computed(() => {
  const pages: number[] = []
  const maxButtons = 5
  const base = Math.max(1, props.page + 1 - 2)
  const end = Math.min(totalPages.value, base + maxButtons - 1)

  for (let i = base; i <= end; i += 1) {
    pages.push(i)
  }

  return pages
})

const displayStart = computed(() => {
  return props.total === 0 ? 0 : props.page * props.pageSize + 1
})

const displayEnd = computed(() => {
  return Math.min(props.total, (props.page + 1) * props.pageSize)
})

function changePage(nextPage: number) {
  if (nextPage < 0 || nextPage >= totalPages.value) {
    return
  }
  emit('update:page', nextPage)
}
</script>

<style scoped>
.inventory-pagination-card {
  margin-top: 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 14px;
  padding: 14px 16px;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  background: #ffffff;
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.06);
}

.pagination-summary p {
  margin: 0;
  color: #475569;
  font-size: 13px;
}

.pagination-controls {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.page-btn {
  min-width: 76px;
  padding: 7px 11px;
  border-radius: 8px;
  border: 1px solid #cbd5e1;
  background: #ffffff;
  color: #0f172a;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.page-btn:hover:not(:disabled) {
  background: #f8fafc;
}

.page-btn.active {
  background: #2563eb;
  border-color: #2563eb;
  color: #ffffff;
}

.page-btn:disabled {
  color: #94a3b8;
  cursor: not-allowed;
  background: #f8fafc;
}
</style>
