<template>
  <div class="system-settings-page">
    <div class="page-header">
      <div>
        <h1>系统设置</h1>
      </div>
    </div>

    <nav v-if="visibleTabs.length > 1" class="tab-bar">
      <button
        v-for="tab in visibleTabs"
        :key="tab.key"
        type="button"
        :class="{ active: activeTab === tab.key }"
        @click="activeTab = tab.key"
      >
        {{ tab.label }}
      </button>
    </nav>

    <SysUserManagementView v-if="activeTab === 'users'" :embedded="true" />
    <RoleManagementView v-else-if="activeTab === 'roles'" :embedded="true" />
    <PermissionManagementView v-else-if="activeTab === 'permissions'" :embedded="true" />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import SysUserManagementView from '@/views/SysUserManagementView.vue'
import RoleManagementView from '@/views/RoleManagementView.vue'
import PermissionManagementView from '@/views/PermissionManagementView.vue'

type TabKey = 'users' | 'roles' | 'permissions'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const tabs: Array<{ key: TabKey; label: string; permission: string }> = [
  { key: 'users', label: '用户', permission: 'auth:user:view' },
  { key: 'roles', label: '角色', permission: 'auth:role:view' },
  { key: 'permissions', label: '权限', permission: 'auth:permission:view' }
]

const visibleTabs = computed(() => tabs.filter((tab) => authStore.hasPermission(tab.permission)))

const fallbackTab = computed<TabKey>(() => visibleTabs.value[0]?.key ?? 'users')

const activeTab = computed<TabKey>({
  get: () => {
    const tab = route.query.tab as string | undefined
    const allowed = visibleTabs.value.find((t) => t.key === tab)
    return allowed?.key ?? fallbackTab.value
  },
  set: (val) => {
    router.replace({ query: { ...route.query, tab: val } })
  }
})
</script>

<style scoped>
.system-settings-page {
  display: flex;
  flex-direction: column;
  gap: 14px;
  color: #0f172a;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.page-header h1 {
  margin: 0;
  color: #0f172a;
  font-size: 22px;
}

.tab-bar {
  display: flex;
  gap: 8px;
  padding: 6px;
  border: 1px solid #e5edf7;
  border-radius: 14px;
  background: #fff;
  box-shadow: 0 6px 18px rgba(15, 23, 42, 0.04);
  align-self: flex-start;
}

.tab-bar button {
  height: 38px;
  padding: 0 16px;
  border: none;
  border-radius: 10px;
  background: transparent;
  color: #64748b;
  cursor: pointer;
  font-weight: 900;
}

.tab-bar button.active {
  background: #2563eb;
  color: #fff;
  box-shadow: 0 8px 18px rgba(37, 99, 235, 0.18);
}
</style>
