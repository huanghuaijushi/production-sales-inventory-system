import { createRouter, createWebHistory } from 'vue-router'

import { useAuthStore } from '@/stores/auth'
import AuthenticatedLayout from '@/layouts/AuthenticatedLayout.vue'
import DashboardView from '@/views/DashboardView.vue'
import InventoryView from '@/views/InventoryView.vue'
import ProductManagementView from '@/views/ProductManagementView.vue'
import PurchaseManagementView from '@/views/PurchaseManagementView.vue'
import SupplierManagementView from '@/views/SupplierManagementView.vue'
import ProductionConfigView from '@/views/ProductionConfigView.vue'
import LoginView from '@/views/LoginView.vue'
import RegisterView from '@/views/RegisterView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/dashboard'
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView,
      meta: {
        guestOnly: true
      }
    },
    {
      path: '/register',
      name: 'register',
      component: RegisterView,
      meta: {
        guestOnly: true
      }
    },
    {
      path: '/',
      component: AuthenticatedLayout,
      meta: {
        requiresAuth: true
      },
      children: [
        {
          path: 'dashboard',
          name: 'dashboard',
          component: DashboardView
        },
        {
          path: 'inventory',
          name: 'inventory',
          component: InventoryView
        },
        {
          path: 'products',
          name: 'products',
          component: ProductManagementView
        },
        {
          path: 'purchase',
          name: 'purchase',
          component: PurchaseManagementView
        },
        {
          path: 'suppliers',
          name: 'suppliers',
          component: SupplierManagementView
        },
        {
          path: 'production-config',
          name: 'production-config',
          component: ProductionConfigView
        }
      ]
    }
  ]
})

router.beforeEach(async (to) => {
  const authStore = useAuthStore()

  if (!authStore.initialized) {
    await authStore.checkSession()
  }

  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    return {
      name: 'login',
      query: {
        redirect: to.fullPath
      }
    }
  }

  if (to.meta.guestOnly && authStore.isAuthenticated) {
    return { name: 'dashboard' }
  }

  return true
})

export default router
