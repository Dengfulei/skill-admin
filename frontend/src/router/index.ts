import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/LoginView.vue')
  },
  {
    path: '/',
    component: () => import('@/layout/MainLayout.vue'),
    redirect: '/user/resources',
    children: [
      {
        path: 'user/resources',
        name: 'user-resources',
        component: () => import('@/views/user/MyResourcesView.vue')
      },
      {
        path: 'user/applications',
        name: 'user-applications',
        component: () => import('@/views/user/DepartmentApplyView.vue')
      },
      {
        path: 'user/manage-resources',
        name: 'user-manage-resources',
        component: () => import('@/views/admin/ResourceManageView.vue')
      },
      {
        path: 'admin/resources',
        name: 'admin-resources',
        component: () => import('@/views/admin/ResourceManageView.vue')
      },
      {
        path: 'admin/applications',
        name: 'admin-applications',
        component: () => import('@/views/admin/ApplicationReviewView.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to) => {
  const authStore = useAuthStore()
  if (to.path === '/login') {
    if (authStore.isLoggedIn) {
      return '/'
    }
    return true
  }
  if (!authStore.isLoggedIn) {
    return '/login'
  }
  if (!authStore.user) {
    await authStore.fetchCurrentUser()
  }
  if (to.path === '/admin/resources' && !authStore.isManager) {
    return '/user/manage-resources'
  }
  if (to.path.startsWith('/admin') && !authStore.isManager) {
    return '/user/resources'
  }
  return true
})

export default router
