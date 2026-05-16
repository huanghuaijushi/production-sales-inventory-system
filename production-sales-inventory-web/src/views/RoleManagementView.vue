<template>
  <div :class="['page', { 'page--embedded': embedded }]">
    <div v-if="!embedded" class="page-header">
      <div>
        <h1>角色管理</h1>
      </div>
      <button type="button" class="primary-button" @click="openCreateRole">新增角色</button>
    </div>

    <section class="toolbar">
      <div class="toolbar-search">
        <input v-model="query" type="search" placeholder="搜索角色名称或编码" @keyup.enter="loadRoles" />
        <button type="button" @click="loadRoles">查询</button>
      </div>
      <button v-if="embedded" type="button" class="primary-button" @click="openCreateRole">新增角色</button>
    </section>

    <section class="grid">
      <div class="panel">
        <div class="panel-header">
          <h2>角色列表</h2>
        </div>
        <div class="list">
          <button
            v-for="role in filteredRoles"
            :key="role.id"
            type="button"
            class="role-item"
            :class="{ active: selectedRole?.id === role.id }"
            @click="selectRole(role)"
          >
            <strong>{{ role.name }}</strong>
            <span>{{ role.code }}</span>
          </button>
        </div>
      </div>

      <div class="panel detail-panel">
        <template v-if="selectedRole">
          <div class="panel-header">
            <div>
              <h2>{{ selectedRole.name }}</h2>
              <p>{{ selectedRole.description || '暂无描述' }}</p>
            </div>
            <div class="actions">
              <button type="button" class="secondary-button" @click="openEditRole">编辑角色</button>
              <button type="button" class="primary-button" @click="saveRolePermissions">保存权限</button>
            </div>
          </div>

          <div class="permission-groups">
            <div v-for="group in groupedPermissions" :key="group.module" class="permission-group">
              <h3>{{ group.module }}</h3>
              <div class="permission-list">
                <label v-for="permission in group.permissions" :key="permission.code" class="permission-item">
                  <input v-model="selectedPermissionCodes" type="checkbox" :value="permission.code" />
                  <span>
                    <strong>{{ permission.name }}</strong>
                    <small>{{ permission.code }}</small>
                  </span>
                </label>
              </div>
            </div>
          </div>
        </template>
        <div v-else class="empty-state">请选择一个角色进行编辑</div>
      </div>
    </section>

    <div v-if="editorOpen" class="modal-backdrop">
      <div class="modal-content">
        <div class="modal-header">
          <h2>{{ editingRoleId ? '编辑角色' : '新增角色' }}</h2>
          <button type="button" class="icon-button" @click="closeEditor">×</button>
        </div>
        <div class="modal-body">
          <label>
            <span>角色编码</span>
            <input v-model.trim="editorForm.code" type="text" placeholder="例如 ADMIN / SUPER_ADMIN" />
          </label>
          <label>
            <span>角色名称</span>
            <input v-model.trim="editorForm.name" type="text" placeholder="角色名称" />
          </label>
          <label>
            <span>描述</span>
            <textarea v-model.trim="editorForm.description" rows="3" placeholder="角色描述"></textarea>
          </label>
          <label>
            <span>排序</span>
            <input v-model.number="editorForm.sortOrder" type="number" min="0" />
          </label>
          <label class="checkbox-line">
            <input v-model="editorForm.enabled" type="checkbox" />
            <span>启用</span>
          </label>
          <div class="permission-group-picker">
            <span>权限</span>
            <div class="permission-list compact">
              <label v-for="permission in allPermissions" :key="permission.code" class="permission-item">
                <input v-model="editorForm.permissionCodes" type="checkbox" :value="permission.code" />
                <span>
                  <strong>{{ permission.name }}</strong>
                  <small>{{ permission.code }}</small>
                </span>
              </label>
            </div>
          </div>
        </div>
        <div class="modal-actions">
          <button type="button" class="secondary-button" @click="closeEditor">取消</button>
          <button type="button" class="primary-button" @click="saveRole">保存</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { sysUserApi } from '@/api/sysUser'
import type { PermissionGroupResponse, PermissionOption, RoleDetailResponse, RoleOption } from '@/types/auth'

withDefaults(defineProps<{ embedded?: boolean }>(), { embedded: false })

const query = ref('')
const roles = ref<RoleOption[]>([])
const selectedRole = ref<RoleOption | null>(null)
const selectedPermissionCodes = ref<string[]>([])
const editorOpen = ref(false)
const editingRoleId = ref<number | null>(null)
const groupedPermissionsData = ref<PermissionGroupResponse[]>([])
const allPermissions = ref<PermissionOption[]>([])
const editorForm = reactive({
  code: '',
  name: '',
  description: '',
  enabled: true,
  sortOrder: 0,
  permissionCodes: [] as string[]
})

const filteredRoles = computed(() => {
  const keyword = query.value.trim().toLowerCase()
  if (!keyword) return roles.value
  return roles.value.filter((role) => `${role.name} ${role.code}`.toLowerCase().includes(keyword))
})

const groupedPermissions = computed<PermissionGroupResponse[]>(() => groupedPermissionsData.value)

onMounted(async () => {
  await Promise.all([loadRoles(), loadPermissions()])
})

async function loadRoles() {
  roles.value = await sysUserApi.getRoles()
  if (!selectedRole.value && roles.value.length > 0) {
    const firstRole = roles.value[0]
    if (firstRole) {
      selectRole(firstRole)
    }
  }
}

async function loadPermissions() {
  groupedPermissionsData.value = await sysUserApi.getPermissionGroups()
  allPermissions.value = groupedPermissionsData.value.flatMap((group) => group.permissions)
}

function selectRole(role: RoleOption) {
  selectedRole.value = role
  loadRoleDetail(role.id)
}

async function loadRoleDetail(roleId: number) {
  const detail: RoleDetailResponse = await sysUserApi.getRole(roleId)
  selectedPermissionCodes.value = [...detail.permissionCodes]
}

function openCreateRole() {
  editingRoleId.value = null
  editorForm.code = ''
  editorForm.name = ''
  editorForm.description = ''
  editorForm.enabled = true
  editorForm.sortOrder = 0
  editorForm.permissionCodes = []
  editorOpen.value = true
}

async function openEditRole() {
  if (!selectedRole.value) return
  const detail = await sysUserApi.getRole(selectedRole.value.id)
  editingRoleId.value = detail.id
  editorForm.code = detail.code
  editorForm.name = detail.name
  editorForm.description = detail.description || ''
  editorForm.enabled = detail.enabled
  editorForm.sortOrder = detail.sortOrder || 0
  editorForm.permissionCodes = [...detail.permissionCodes]
  editorOpen.value = true
}

function closeEditor() {
  editorOpen.value = false
}

async function saveRolePermissions() {
  if (!selectedRole.value) return
  await sysUserApi.updateRolePermissions(selectedRole.value.id, selectedPermissionCodes.value)
  await loadRoles()
  await loadRoleDetail(selectedRole.value.id)
}

async function saveRole() {
  const payload = {
    code: editorForm.code,
    name: editorForm.name,
    description: editorForm.description,
    enabled: editorForm.enabled,
    sortOrder: editorForm.sortOrder,
    permissionCodes: editorForm.permissionCodes
  }
  if (editingRoleId.value) {
    await sysUserApi.updateRole(editingRoleId.value, payload)
  } else {
    await sysUserApi.createRole(payload)
  }
  editorOpen.value = false
  await loadRoles()
}
</script>

<style scoped>
.page { width: 100%; }
.page-header { display: flex; justify-content: space-between; align-items: flex-start; gap: 12px; margin-bottom: 16px; }
.page-eyebrow { margin: 0 0 6px; color: #2563eb; font-size: 13px; font-weight: 700; }
.page-header h1 { margin: 0; color: #0f172a; font-size: 20px; }
.page-header p { margin: 6px 0 0; color: #64748b; font-size: 13px; line-height: 1.6; }
.toolbar { display: flex; justify-content: space-between; gap: 12px; margin-bottom: 16px; }
.toolbar-search { display: flex; gap: 10px; flex: 1; }
.toolbar-search input { flex: 1; border: 1px solid #cbd5e1; border-radius: 8px; padding: 10px 12px; }
.toolbar-search button, .primary-button, .secondary-button { border-radius: 8px; padding: 10px 14px; font-weight: 700; cursor: pointer; }
.primary-button { border: 0; background: #2563eb; color: #fff; }
.secondary-button { border: 1px solid #dbe3ef; background: #fff; color: #334155; }
.grid { display: grid; grid-template-columns: 320px 1fr; gap: 16px; }
.panel { border: 1px solid #e2e8f0; border-radius: 12px; background: #fff; padding: 16px; }
.panel-header { display: flex; align-items: flex-start; justify-content: space-between; gap: 12px; margin-bottom: 12px; }
.list { display: grid; gap: 8px; }
.role-item { border: 1px solid #e2e8f0; background: #f8fafc; border-radius: 10px; padding: 12px; text-align: left; cursor: pointer; display: grid; gap: 4px; }
.role-item.active { border-color: #2563eb; background: #eff6ff; }
.role-item span { color: #64748b; font-size: 12px; }
.permission-groups { display: grid; gap: 14px; }
.permission-group h3 { margin: 0 0 8px; font-size: 14px; }
.permission-list { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 10px; }
.permission-item { display: flex; gap: 10px; align-items: flex-start; border: 1px solid #e2e8f0; border-radius: 10px; padding: 10px; }
.permission-item strong { display: block; }
.permission-item small { color: #64748b; }
.permission-item.compact { grid-template-columns: repeat(auto-fill, minmax(260px, 1fr)); }
.empty-state { color: #64748b; }
.actions { display: flex; gap: 10px; }
.modal-backdrop { position: fixed; inset: 0; background: rgba(15, 23, 42, 0.45); display: flex; align-items: center; justify-content: center; padding: 24px; }
.modal-content { width: min(900px, 100%); max-height: 90vh; overflow: auto; background: #fff; border-radius: 16px; padding: 20px; }
.modal-header, .modal-actions { display: flex; align-items: center; justify-content: space-between; gap: 12px; }
.modal-body { display: grid; gap: 12px; margin: 16px 0; }
.modal-body label { display: grid; gap: 6px; }
.modal-body input, .modal-body textarea { border: 1px solid #cbd5e1; border-radius: 8px; padding: 10px 12px; }
.checkbox-line { display: flex !important; align-items: center; gap: 8px; }
</style>
