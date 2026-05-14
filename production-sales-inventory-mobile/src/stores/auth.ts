import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { getAuthStatus, login as loginApi, logout as logoutApi, type LoginRequest, type SysUserProfile } from '@/api/auth'
import { AuthExpiredError } from '@/api/http'
import { getStorage, removeStorage, setStorage, TOKEN_STORAGE_KEY, TOKEN_TYPE_STORAGE_KEY, USER_STORAGE_KEY } from '@/utils/storage'

export const useAuthStore = defineStore('auth', () => {
  const accessToken = ref(getStorage<string>(TOKEN_STORAGE_KEY, ''))
  const tokenType = ref(getStorage<string>(TOKEN_TYPE_STORAGE_KEY, 'Bearer'))
  const sysUser = ref<SysUserProfile | null>(getStorage<SysUserProfile | null>(USER_STORAGE_KEY, null))
  const initialized = ref(false)

  const isLoggedIn = computed(() => Boolean(accessToken.value && sysUser.value))
  const displayName = computed(() => sysUser.value?.nickname || sysUser.value?.username || '未登录')

  function persistAuth(tokenResponse: { accessToken: string; tokenType: string; sysUser: SysUserProfile }) {
    accessToken.value = tokenResponse.accessToken
    tokenType.value = tokenResponse.tokenType || 'Bearer'
    sysUser.value = tokenResponse.sysUser

    setStorage(TOKEN_STORAGE_KEY, accessToken.value)
    setStorage(TOKEN_TYPE_STORAGE_KEY, tokenType.value)
    setStorage(USER_STORAGE_KEY, sysUser.value)
  }

  function clearAuth() {
    accessToken.value = ''
    tokenType.value = 'Bearer'
    sysUser.value = null

    removeStorage(TOKEN_STORAGE_KEY)
    removeStorage(TOKEN_TYPE_STORAGE_KEY)
    removeStorage(USER_STORAGE_KEY)
  }

  async function login(payload: LoginRequest) {
    const tokenResponse = await loginApi(payload)
    persistAuth(tokenResponse)
    return tokenResponse
  }

  async function restoreSession() {
    if (!accessToken.value) {
      initialized.value = true
      return false
    }

    try {
      const status = await getAuthStatus()

      if (status.authenticated && status.sysUser) {
        sysUser.value = status.sysUser
        setStorage(USER_STORAGE_KEY, status.sysUser)
        initialized.value = true
        return true
      }

      clearAuth()
      initialized.value = true
      return false
    } catch (error) {
      initialized.value = true

      if (error instanceof AuthExpiredError) {
        clearAuth()
        return false
      }

      // Network/timeout — keep cached session, let subsequent requests trigger
      // a real 401 if the token is actually invalid. Do not log the user out
      // for transient backend hiccups.
      return Boolean(sysUser.value)
    }
  }

  async function logout() {
    try {
      await logoutApi()
    } finally {
      clearAuth()
    }
  }

  return {
    accessToken,
    tokenType,
    sysUser,
    initialized,
    isLoggedIn,
    displayName,
    login,
    restoreSession,
    logout,
    clearAuth
  }
})
