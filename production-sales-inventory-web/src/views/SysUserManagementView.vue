<template>
  <div class="page">
    <div class="page-header">
      <div>
        <p class="page-eyebrow">系统设置</p>
        <h1>用户管理</h1>
        <p>由系统用户创建账号、重置密码，并控制后台账号是否可登录。</p>
      </div>
      <button v-if="canCreateUser" type="button" class="primary-button" @click="openCreateModal">新增用户</button>
    </div>

    <p v-if="message" class="operation-message">{{ message }}</p>

    <section class="panel filter-panel">
      <input
        v-model="query"
        type="search"
        placeholder="搜索账号或昵称"
        @keyup.enter="loadUsers(true)"
      />
      <button type="button" class="secondary-button" @click="loadUsers(true)">查询</button>
    </section>

    <section class="panel user-table-panel">
      <div class="table-header">
        <div>
          <h2>系统用户列表</h2>
          <p>共 {{ pageState.totalElements }} 个账号，禁用后该账号不能继续登录。</p>
        </div>
      </div>

      <div class="table-wrap">
        <table class="user-table">
          <thead>
            <tr>
              <th>账号</th>
              <th>昵称</th>
              <th>角色</th>
              <th>状态</th>
              <th>最后登录</th>
              <th>创建时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td colspan="7" class="empty-cell">正在加载用户...</td>
            </tr>
            <tr v-else-if="users.length === 0">
              <td colspan="7" class="empty-cell">暂无用户账号</td>
            </tr>
            <tr v-for="user in users" v-else :key="user.id">
              <td>
                <strong>{{ user.username }}</strong>
              </td>
              <td>{{ user.nickname }}</td>
              <td>{{ formatRoles(user.roles) }}</td>
              <td>
                <span class="status-pill" :class="user.status === 'ACTIVE' ? 'status-pill--active' : 'status-pill--disabled'">
                  {{ user.status === 'ACTIVE' ? '正常' : '禁用' }}
                </span>
              </td>
              <td>{{ formatDateTime(user.lastLoginAt) }}</td>
              <td>{{ formatDateTime(user.createdAt) }}</td>
              <td>
                <div class="row-actions">
                  <button v-if="canUpdateUser" type="button" class="text-button" @click="openPasswordModal(user)">重置密码</button>
                  <button
                    v-if="canDisableUser && user.status === 'ACTIVE'"
                    type="button"
                    class="text-button danger-text"
                    :disabled="user.id === authStore.sysUser?.id"
                    @click="disableUser(user)"
                  >
                    禁用
                  </button>
                  <button v-else-if="canUpdateUser" type="button" class="text-button primary-text" @click="activateUser(user)">启用</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="pagination-bar">
        <span>第 {{ pageState.number + 1 }} / {{ Math.max(pageState.totalPages, 1) }} 页</span>
        <div class="pagination-actions">
          <button type="button" :disabled="pageState.number <= 0" @click="changePage(pageState.number - 1)">上一页</button>
          <button type="button" :disabled="pageState.number + 1 >= pageState.totalPages" @click="changePage(pageState.number + 1)">下一页</button>
        </div>
      </div>
    </section>

    <div v-if="createModalOpen" class="modal-backdrop">
      <div class="modal-content">
        <div class="modal-header">
          <h2>新增用户</h2>
          <button type="button" class="icon-button" @click="closeCreateModal">×</button>
        </div>
        <div class="modal-body">
          <label>
            <span>账号 / 用户名</span>
            <input v-model.trim="createForm.username" type="text" placeholder="3-50 位字母、数字或下划线" />
          </label>
          <label>
            <span>昵称</span>
            <input v-model.trim="createForm.nickname" type="text" placeholder="用户显示名称" />
          </label>
          <label>
            <span>初始密码</span>
            <input v-model="createForm.password" type="password" placeholder="至少 8 位，包含字母和数字" />
          </label>
        </div>
        <div class="modal-actions">
          <button type="button" class="secondary-button" @click="closeCreateModal">取消</button>
          <button type="button" class="primary-button" :disabled="submitting" @click="createUser">
            {{ submitting ? '创建中...' : '创建账号' }}
          </button>
        </div>
      </div>
    </div>

    <div v-if="passwordModalOpen && selectedUser" class="modal-backdrop">
      <div class="modal-content">
        <div class="modal-header">
          <h2>重置密码</h2>
          <button type="button" class="icon-button" @click="closePasswordModal">×</button>
        </div>
        <div class="modal-body">
          <p class="modal-help">正在为「{{ selectedUser.nickname }}」重置登录密码，保存后旧密码立即失效。</p>
          <label>
            <span>新密码</span>
            <input v-model="newPassword" type="password" placeholder="至少 8 位，包含字母和数字" />
          </label>
        </div>
        <div class="modal-actions">
          <button type="button" class="secondary-button" @click="closePasswordModal">取消</button>
          <button type="button" class="primary-button" :disabled="submitting" @click="resetPassword">
            {{ submitting ? '保存中...' : '保存新密码' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ApiError } from '@/api/http'
import { sysUserApi } from '@/api/sysUser'
import { useAuthStore } from '@/stores/auth'
import type { SysUserProfile } from '@/types/auth'

const authStore = useAuthStore()
const users = ref<SysUserProfile[]>([])
const loading = ref(false)
const submitting = ref(false)
const createModalOpen = ref(false)
const passwordModalOpen = ref(false)
const selectedUser = ref<SysUserProfile | null>(null)
const newPassword = ref('')
const query = ref('')
const message = ref('')
const pageState = reactive({
  number: 0,
  size: 10,
  totalElements: 0,
  totalPages: 0
})
const createForm = reactive({
  username: '',
  nickname: '',
  password: ''
})
let searchTimer: number | undefined
let messageTimer: number | undefined

const canCreateUser = computed(() => authStore.hasPermission('auth:user:create'))
const canUpdateUser = computed(() => authStore.hasPermission('auth:user:update'))
const canDisableUser = computed(() => authStore.hasPermission('auth:user:disable'))

watch(query, () => {
  window.clearTimeout(searchTimer)
  searchTimer = window.setTimeout(() => loadUsers(true), 300)
})

onMounted(() => {
  loadUsers(true)
})

async function loadUsers(resetPage = false) {
  if (resetPage) {
    pageState.number = 0
  }
  loading.value = true
  try {
    const result = await sysUserApi.getUsers(pageState.number, pageState.size, query.value)
    users.value = result.content
    pageState.number = result.number
    pageState.size = result.size
    pageState.totalElements = result.totalElements
    pageState.totalPages = result.totalPages
  } catch (error) {
    showMessage(getErrorMessage(error, '加载用户失败'))
  } finally {
    loading.value = false
  }
}

function changePage(page: number) {
  pageState.number = page
  loadUsers()
}

function openCreateModal() {
  createForm.username = ''
  createForm.nickname = ''
  createForm.password = ''
  createModalOpen.value = true
}

function closeCreateModal() {
  createModalOpen.value = false
}

async function createUser() {
  const validationMessage = validateUserForm(createForm.username, createForm.nickname, createForm.password)
  if (validationMessage) {
    showMessage(validationMessage)
    return
  }

  submitting.value = true
  try {
    await sysUserApi.createUser({
      username: createForm.username,
      nickname: createForm.nickname,
      password: createForm.password
    })
    showMessage('用户账号已创建。')
    closeCreateModal()
    await loadUsers(true)
  } catch (error) {
    showMessage(getErrorMessage(error, '创建用户失败'))
  } finally {
    submitting.value = false
  }
}

function openPasswordModal(user: SysUserProfile) {
  selectedUser.value = user
  newPassword.value = ''
  passwordModalOpen.value = true
}

function closePasswordModal() {
  passwordModalOpen.value = false
  selectedUser.value = null
  newPassword.value = ''
}

async function resetPassword() {
  if (!selectedUser.value) return
  const validationMessage = validatePassword(newPassword.value)
  if (validationMessage) {
    showMessage(validationMessage)
    return
  }

  submitting.value = true
  try {
    await sysUserApi.resetPassword(selectedUser.value.id, newPassword.value)
    showMessage('密码已重置。')
    closePasswordModal()
    await loadUsers()
  } catch (error) {
    showMessage(getErrorMessage(error, '重置密码失败'))
  } finally {
    submitting.value = false
  }
}

async function activateUser(user: SysUserProfile) {
  submitting.value = true
  try {
    await sysUserApi.activateUser(user.id)
    showMessage('用户已启用。')
    await loadUsers()
  } catch (error) {
    showMessage(getErrorMessage(error, '启用用户失败'))
  } finally {
    submitting.value = false
  }
}

async function disableUser(user: SysUserProfile) {
  const confirmed = window.confirm(`确定禁用用户「${user.nickname}」吗？`)
  if (!confirmed) return

  submitting.value = true
  try {
    await sysUserApi.disableUser(user.id)
    showMessage('用户已禁用。')
    await loadUsers()
  } catch (error) {
    showMessage(getErrorMessage(error, '禁用用户失败'))
  } finally {
    submitting.value = false
  }
}

function validateUserForm(username: string, nickname: string, password: string) {
  if (!/^[A-Za-z0-9_]{3,50}$/.test(username)) return '账号需为 3-50 位字母、数字或下划线。'
  if (!nickname.trim()) return '请填写用户昵称。'
  if (nickname.trim().length > 80) return '用户昵称不能超过 80 个字符。'
  return validatePassword(password)
}

function validatePassword(password: string) {
  if (password.length < 8 || password.length > 72) return '密码长度需为 8-72 位。'
  if (!/[A-Za-z]/.test(password) || !/\d/.test(password)) return '密码需至少包含一个字母和一个数字。'
  return ''
}

function formatRoles(roles: string[]) {
  return roles.length > 0 ? roles.join(' / ') : '-'
}

function formatDateTime(value?: string) {
  if (!value) return '-'
  return new Intl.DateTimeFormat('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }).format(new Date(value))
}

function getErrorMessage(error: unknown, fallback: string) {
  if (error instanceof ApiError) {
    if (error.status === 409) return '该账号已存在，请更换账号。'
    if (error.status === 400) return error.message || '提交信息不符合要求。'
    if (error.status === 403) return error.message || '没有权限执行该操作。'
    return error.message || fallback
  }
  return fallback
}

function showMessage(value: string) {
  message.value = value
  window.clearTimeout(messageTimer)
  messageTimer = window.setTimeout(() => {
    message.value = ''
  }, 2800)
}
</script>

<style scoped>
.page {
  width: 100%;
  overflow-wrap: anywhere;
}

.page * {
  box-sizing: border-box;
}

.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18px;
  margin-bottom: 16px;
}

.page-eyebrow {
  margin: 0 0 6px;
  color: #2563eb;
  font-size: 13px;
  font-weight: 700;
}

.page-header h1 {
  margin: 0;
  color: #0f172a;
  font-size: 20px;
  line-height: 1.2;
}

.page-header p,
.table-header p {
  margin: 6px 0 0;
  color: #64748b;
  font-size: 13px;
  line-height: 1.6;
}

.panel {
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  background: #ffffff;
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.06);
}

.filter-panel {
  display: grid;
  grid-template-columns: minmax(260px, 1fr) auto;
  gap: 12px;
  margin-bottom: 16px;
  padding: 14px 16px;
}

input {
  width: 100%;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  padding: 9px 11px;
  color: #0f172a;
  font: inherit;
  font-size: 14px;
  outline: none;
}

input:focus {
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.12);
}

.primary-button,
.secondary-button {
  border-radius: 8px;
  padding: 9px 14px;
  font-weight: 700;
  cursor: pointer;
}

.primary-button {
  border: 0;
  background: #2563eb;
  color: #ffffff;
}

.secondary-button {
  border: 1px solid #dbe3ef;
  background: #ffffff;
  color: #334155;
}

.primary-button:disabled,
.text-button:disabled {
  cursor: not-allowed;
  opacity: 0.55;
}

.user-table-panel {
  overflow: hidden;
}

.table-header {
  padding: 14px 16px;
  border-bottom: 1px solid #edf2f7;
}

.table-header h2 {
  margin: 0;
  color: #0f172a;
  font-size: 16px;
}

.table-wrap {
  overflow-x: auto;
}

.user-table {
  width: 100%;
  min-width: 900px;
  border-collapse: collapse;
}

.user-table th,
.user-table td {
  border-bottom: 1px solid #edf2f7;
  padding: 12px 14px;
  color: #334155;
  font-size: 13px;
  text-align: left;
  vertical-align: middle;
}

.user-table th {
  background: #f8fafc;
  color: #475569;
  font-weight: 700;
}

.user-table th:last-child,
.user-table td:last-child {
  text-align: center;
}

.user-table strong {
  color: #0f172a;
}

.status-pill {
  display: inline-flex;
  align-items: center;
  border-radius: 999px;
  padding: 4px 10px;
  font-size: 12px;
  font-weight: 700;
}

.status-pill--active {
  background: #ecfdf5;
  color: #047857;
}

.status-pill--disabled {
  background: #f1f5f9;
  color: #64748b;
}

.row-actions,
.modal-actions {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  flex-wrap: wrap;
}

.pagination-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 10px;
  flex-wrap: wrap;
}

.text-button {
  border: 0;
  padding: 0;
  background: transparent;
  color: #475569;
  font-weight: 700;
  cursor: pointer;
  white-space: nowrap;
}

.primary-text {
  color: #2563eb;
}

.danger-text {
  color: #dc2626;
}

.empty-cell {
  color: #94a3b8;
  text-align: center;
}

.pagination-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 16px;
  color: #64748b;
  font-size: 13px;
}

.pagination-actions button {
  border: 1px solid #dbe3ef;
  border-radius: 6px;
  padding: 6px 10px;
  background: #ffffff;
  cursor: pointer;
}

.operation-message {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 80;
  border: 1px solid #bfdbfe;
  border-radius: 8px;
  padding: 12px 16px;
  background: #eff6ff;
  color: #1d4ed8;
  box-shadow: 0 16px 36px rgba(37, 99, 235, 0.14);
}

.modal-backdrop {
  position: fixed;
  inset: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: rgba(15, 23, 42, 0.48);
}

.modal-content {
  display: flex;
  flex-direction: column;
  width: min(560px, 100%);
  max-height: 90vh;
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 24px 60px rgba(15, 23, 42, 0.22);
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 22px;
  border-bottom: 1px solid #e2e8f0;
}

.modal-header h2 {
  margin: 0;
  color: #0f172a;
  font-size: 20px;
}

.icon-button {
  width: 36px;
  height: 36px;
  border: 0;
  border-radius: 8px;
  background: #f1f5f9;
  color: #334155;
  font-size: 22px;
  cursor: pointer;
}

.modal-body {
  display: grid;
  gap: 14px;
  overflow-y: auto;
  padding: 20px 22px;
}

.modal-help {
  margin: 0;
  color: #64748b;
  font-size: 13px;
  line-height: 1.6;
}

label span {
  display: block;
  margin-bottom: 6px;
  color: #334155;
  font-size: 13px;
  font-weight: 700;
}

.modal-actions {
  padding: 16px 22px;
  border-top: 1px solid #e2e8f0;
}

@media (max-width: 760px) {
  .page-header,
  .pagination-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-panel {
    grid-template-columns: 1fr;
  }

  .row-actions,
  .pagination-actions,
  .modal-actions {
    justify-content: flex-start;
  }
}
</style>
