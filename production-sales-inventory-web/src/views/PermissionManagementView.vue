<template>
  <div :class="['page', { 'page--embedded': embedded }]">
    <div v-if="!embedded" class="page-header">
      <div>
        <h1>权限管理</h1>
      </div>
    </div>

    <section class="toolbar">
      <input v-model="query" type="search" placeholder="搜索权限名称或编码" />
    </section>

    <section class="panel">
      <div v-for="group in filteredGroups" :key="group.module" class="permission-group">
        <div class="group-header">
          <h2>{{ group.module }}</h2>
          <span>{{ group.permissions.length }} 项权限</span>
        </div>
        <div class="permission-grid">
          <div v-for="permission in group.permissions" :key="permission.code" class="permission-card">
            <strong>{{ permission.name }}</strong>
            <code>{{ permission.code }}</code>
            <p>{{ permission.description || '暂无说明' }}</p>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { sysUserApi } from '@/api/sysUser'
import type { PermissionGroupResponse } from '@/types/auth'

withDefaults(defineProps<{ embedded?: boolean }>(), { embedded: false })

const query = ref('')
const groups = ref<PermissionGroupResponse[]>([])

onMounted(async () => {
  groups.value = await sysUserApi.getPermissionGroups()
})

const filteredGroups = computed(() => {
  const keyword = query.value.trim().toLowerCase()
  if (!keyword) return groups.value
  return groups.value
    .map((group) => ({
      ...group,
      permissions: group.permissions.filter((permission) =>
        `${permission.name} ${permission.code}`.toLowerCase().includes(keyword)
      )
    }))
    .filter((group) => group.permissions.length > 0)
})
</script>

<style scoped>
.page { width: 100%; }
.page-header { margin-bottom: 16px; }
.page-eyebrow { margin: 0 0 6px; color: #2563eb; font-size: 13px; font-weight: 700; }
.page-header h1 { margin: 0; color: #0f172a; font-size: 20px; }
.page-header p { margin: 6px 0 0; color: #64748b; font-size: 13px; line-height: 1.6; }
.toolbar { margin-bottom: 16px; }
.toolbar input { width: 100%; max-width: 420px; border: 1px solid #cbd5e1; border-radius: 8px; padding: 10px 12px; }
.panel { border: 1px solid #e2e8f0; border-radius: 12px; background: #fff; padding: 16px; display: grid; gap: 18px; }
.group-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.group-header h2 { margin: 0; font-size: 15px; }
.group-header span { color: #64748b; font-size: 12px; }
.permission-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(240px, 1fr)); gap: 12px; }
.permission-card { border: 1px solid #e2e8f0; border-radius: 10px; padding: 12px; background: #f8fafc; }
.permission-card strong { display: block; margin-bottom: 6px; }
.permission-card code { display: inline-block; margin-bottom: 8px; color: #2563eb; }
.permission-card p { margin: 0; color: #64748b; font-size: 12px; line-height: 1.5; }
</style>
