import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { getReviewApplicationsApi } from '@/api/modules'
import type { AccessRequestItem } from '@/types'

function getPendingCount(applications: AccessRequestItem[]) {
  return applications.filter((item) => item.status === 'PENDING').length
}

export const useApplicationStore = defineStore('application', () => {
  const pendingReviewCount = ref(0)

  const hasPendingReviews = computed(() => pendingReviewCount.value > 0)

  async function syncPendingReviewCount(applications?: AccessRequestItem[]) {
    if (applications) {
      pendingReviewCount.value = getPendingCount(applications)
      return pendingReviewCount.value
    }
    const reviewApplications = await getReviewApplicationsApi()
    pendingReviewCount.value = getPendingCount(reviewApplications)
    return pendingReviewCount.value
  }

  function resetPendingReviewCount() {
    pendingReviewCount.value = 0
  }

  return {
    pendingReviewCount,
    hasPendingReviews,
    syncPendingReviewCount,
    resetPendingReviewCount
  }
})
