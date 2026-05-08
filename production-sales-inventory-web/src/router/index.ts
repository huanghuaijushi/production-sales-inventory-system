import { createRouter, createWebHistory } from 'vue-router'

import { useAuthStore } from '@/stores/auth'
import AuthenticatedLayout from '@/layouts/AuthenticatedLayout.vue'
import DashboardView from '@/views/DashboardView.vue'
import InventoryView from '@/views/InventoryView.vue'
import ProductManagementView from '@/views/ProductManagementView.vue'
import PurchaseManagementView from '@/views/PurchaseManagementView.vue'
import SupplierManagementView from '@/views/SupplierManagementView.vue'
import ProductionPlanView from '@/views/ProductionPlanView.vue'
import ProductionConfigView from '@/views/ProductionConfigView.vue'
import SalesManagementView from '@/views/SalesManagementView.vue'
import SysUserManagementView from '@/views/SysUserManagementView.vue'
import RoleManagementView from '@/views/RoleManagementView.vue'
import PermissionManagementView from '@/views/PermissionManagementView.vue'
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
          path: 'production',
          name: 'production',
          component: ProductionPlanView
        },
        {
          path: 'production-config',
          name: 'production-config',
          component: ProductionConfigView
        },
        {
          path: 'sales',
          name: 'sales',
          component: SalesManagementView
        },
        {
          path: 'sys-users',
          name: 'sys-users',
          component: SysUserManagementView
        },
        {
          path: 'roles',
          name: 'roles',
          component: RoleManagementView
        },
        {
          path: 'permissions',
          name: 'permissions',
          component: PermissionManagementView
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
