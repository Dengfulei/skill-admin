import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { getCurrentUserApi, loginApi } from '@/api/modules'
import type { AuthenticatedUser } from '@/types'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref<AuthenticatedUser | null>(null)

  const isLoggedIn = computed(() => Boolean(token.value))
  const isManager = computed(() =>
    Boolean(user.value?.systemAdmin || (user.value?.departmentAdminIds?.length ?? 0) > 0)
  )

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
    isManager,
    login,
    fetchCurrentUser,
    logout
  }
})
