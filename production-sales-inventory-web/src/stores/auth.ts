import { defineStore } from 'pinia'

import { getAuthStatusApi, loginApi, logoutApi, registerApi } from '@/api/auth'
import type { AdminProfile, LoginRequest, RegisterRequest } from '@/types/auth'
import { clearAuthToken, readAuthToken, readRememberLogin, saveAuthToken } from '@/utils/token-storage'

interface AuthState {
  token: string | null
  admin: AdminProfile | null
  remembered: boolean
  initialized: boolean
  loading: boolean
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    token: readAuthToken(),
    admin: null,
    remembered: readRememberLogin(),
    initialized: false,
    loading: false
  }),

  getters: {
    isAuthenticated: (state) => Boolean(state.token && state.admin)
  },

  actions: {
    async login(payload: LoginRequest, remember: boolean) {
      this.loading = true
      try {
        const result = await loginApi(payload)
        this.token = result.accessToken
        this.admin = result.admin
        this.remembered = remember
        saveAuthToken(result.accessToken, remember)
      } finally {
        this.loading = false
      }
    },

    async register(payload: RegisterRequest) {
      this.loading = true
      try {
        return await registerApi(payload)
      } finally {
        this.loading = false
      }
    },

    async checkSession() {
      if (this.initialized) {
        return this.isAuthenticated
      }

      if (!this.token) {
        this.initialized = true
        return false
      }

      try {
        const status = await getAuthStatusApi()
        if (status.authenticated && status.admin) {
          this.admin = status.admin
          return true
        }

        this.clearSession()
        return false
      } catch {
        this.clearSession()
        return false
      } finally {
        this.initialized = true
      }
    },

    async logout() {
      try {
        if (this.token) {
          await logoutApi()
        }
      } finally {
        this.clearSession()
      }
    },

    clearSession() {
      this.token = null
      this.admin = null
      clearAuthToken()
    }
  }
})
