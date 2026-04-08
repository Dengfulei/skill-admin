<template>
  <el-container style="min-height: 100vh">
    <el-aside width="240px" class="aside">
      <div class="brand">
        <h2>Skill Admin</h2>
        <p>三级权限配置中心</p>
      </div>
      <el-menu :default-active="route.path" router class="menu">
        <el-menu-item index="/user/resources">我的可用技能</el-menu-item>
        <el-menu-item index="/user/applications">部门技能申请</el-menu-item>
        <el-menu-item v-if="authStore.isManager" index="/admin/resources">资源管理</el-menu-item>
        <el-menu-item v-if="authStore.isManager" index="/admin/applications">申请审批</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div>
          <h3>{{ pageTitle }}</h3>
          <p>{{ subtitle }}</p>
        </div>
        <div class="user-info">
          <el-tag type="success">{{ authStore.user?.displayName }}</el-tag>
          <el-tag v-if="authStore.user?.systemAdmin" type="danger">系统管理员</el-tag>
          <el-tag v-else-if="authStore.user?.departmentAdminIds?.length" type="warning">部门管理员</el-tag>
          <el-button link type="primary" @click="handleLogout">退出登录</el-button>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const titleMap: Record<string, [string, string]> = {
  '/user/resources': ['我的可用技能', '展示当前用户可访问的公共、部门与个人 Skill/MCP 资源'],
  '/user/applications': ['部门技能申请', '提交部门级技能申请并跟踪审批状态'],
  '/admin/resources': ['资源管理', '维护 Skill/MCP 资源、按三级权限分配与启停控制'],
  '/admin/applications': ['申请审批', '部门管理员或系统管理员处理部门技能申请']
}

const pageTitle = computed(() => titleMap[route.path]?.[0] ?? 'Skill Admin')
const subtitle = computed(() => titleMap[route.path]?.[1] ?? '')

function handleLogout() {
  authStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.aside {
  background: linear-gradient(180deg, #0f172a, #111827);
  color: #fff;
  padding: 20px 14px;
}

.brand {
  padding: 12px 12px 20px;
}

.brand h2 {
  margin: 0;
  font-size: 24px;
}

.brand p {
  margin: 8px 0 0;
  color: rgba(255, 255, 255, 0.7);
  font-size: 13px;
}

.menu {
  border-right: none;
  background: transparent;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 22px 28px 12px;
  background: transparent;
}

.header h3 {
  margin: 0;
  font-size: 24px;
}

.header p {
  margin: 6px 0 0;
  color: #64748b;
}

.main {
  padding: 0 28px 28px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
</style>
