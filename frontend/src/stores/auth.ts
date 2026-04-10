import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { getCurrentUserApi, loginApi } from '@/api/modules'
import type { AuthenticatedUser } from '@/types'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref<AuthenticatedUser | null>(null)

  const isLoggedIn = computed(() => Boolean(token.value))
  const isSystemAdmin = computed(() => Boolean(user.value?.systemAdmin))
  const hasDepartmentAdminRole = computed(() => Boolean((user.value?.departmentAdminIds?.length ?? 0) > 0))
  const canApplyDepartmentResources = computed(() =>
    Boolean((user.value?.departmentIds?.length ?? 0) > 0 && !isSystemAdmin.value && !hasDepartmentAdminRole.value)
  )
  const canManageSharedResources = computed(() => Boolean(isSystemAdmin.value || hasDepartmentAdminRole.value))
  const canReviewApplications = computed(() => Boolean(hasDepartmentAdminRole.value))

  async function login(username: string, password: string) {
    const data = await loginApi({ username, password })
    token.value = data.token
    user.value = data.user
    localStorage.setItem('token', data.token)
  }

  async function fetchCurrentUser() {
    if (!token.value) return
    user.value = await getCurrentUserApi()
  }

  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
  }

  return {
    token,
    user,
    isLoggedIn,
    isSystemAdmin,
    hasDepartmentAdminRole,
    canApplyDepartmentResources,
    canManageSharedResources,
    canReviewApplications,
    login,
    fetchCurrentUser,
    logout
  }
})
