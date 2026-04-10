<template>
  <el-container class="app-shell">
    <el-aside width="256px" class="aside">
      <div class="brand-block">
        <span class="brand-kicker">Codex Console</span>
        <h2>Skill/MCP Admin</h2>
        <p>数据库化技能中心与三级权限控制工作台</p>
      </div>
      <el-menu :default-active="route.path" router class="menu">
        <el-menu-item index="/user/resources">
          <span>我的可用技能</span>
        </el-menu-item>
        <el-menu-item v-if="authStore.canApplyDepartmentResources" index="/user/applications">
          <span>部门技能申请</span>
        </el-menu-item>
        <el-menu-item index="/user/manage-resources">
          <span>个人资源管理</span>
        </el-menu-item>
        <el-menu-item v-if="authStore.canManageSharedResources" index="/admin/resources">
          <span>共享资源管理</span>
        </el-menu-item>
        <el-menu-item v-if="authStore.canReviewApplications" index="/admin/applications">
          <div class="menu-item-content">
            <span>申请审批</span>
            <span v-if="applicationStore.hasPendingReviews" class="menu-item-count">
              {{ applicationStore.pendingReviewCount }}
            </span>
          </div>
        </el-menu-item>
      </el-menu>
      <div class="aside-note">
        <h4>当前模式</h4>
        <p>公共级、部门级、个人级资源统一在数据库中维护与授权。</p>
      </div>
    </el-aside>
    <el-container class="content-shell">
      <el-header class="header">
        <div class="header-copy">
          <span class="header-kicker">Workspace</span>
          <h3>{{ pageTitle }}</h3>
          <p>{{ subtitle }}</p>
        </div>
        <div class="user-info page-card">
          <div class="identity-copy">
            <strong>{{ authStore.user?.displayName }}</strong>
            <span>{{ authStore.user?.username }}</span>
          </div>
          <el-tag type="danger" v-if="authStore.user?.systemAdmin"
            >系统管理员</el-tag
          >
          <el-tag
            type="warning"
            v-else-if="authStore.user?.departmentAdminIds?.length"
            >部门管理员</el-tag
          >
          <el-tag type="success" v-else>普通用户</el-tag>
          <el-button link type="primary" @click="handleLogout"
            >退出登录</el-button
          >
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useAuthStore } from "@/stores/auth";
import { useApplicationStore } from "@/stores/application";

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();
const applicationStore = useApplicationStore();

const titleMap: Record<string, [string, string]> = {
  "/user/resources": [
    "我的可用技能",
    "展示当前用户可访问的公共、部门与个人 Skill/MCP 资源",
  ],
  "/user/applications": ["部门技能申请", "提交部门级技能申请并跟踪审批状态"],
  "/user/manage-resources": [
    "个人资源管理",
    "仅维护当前用户的个人 Skill/MCP 资源，并保持仅本人可用",
  ],
  "/admin/resources": [
    "共享资源管理",
    "维护公共级与部门级 Skill / MCP 资源，并执行共享授权与启停控制",
  ],
  "/admin/applications": ["申请审批", "部门管理员处理本人负责部门的技能申请"],
};

const pageTitle = computed(() => titleMap[route.path]?.[0] ?? "Skill Admin");
const subtitle = computed(() => titleMap[route.path]?.[1] ?? "");

function handleLogout() {
  applicationStore.resetPendingReviewCount();
  authStore.logout();
  router.push("/login");
}

watch(
  () => authStore.canReviewApplications,
  async (canReview) => {
    if (!canReview) {
      applicationStore.resetPendingReviewCount();
      return;
    }
    await applicationStore.syncPendingReviewCount();
  },
  { immediate: true },
);
</script>

<style scoped>
.app-shell {
  min-height: 100vh;
}

.aside {
  display: flex;
  flex-direction: column;
  gap: 18px;
  padding: 22px 16px;
  background: linear-gradient(180deg, #0f172a, #111827 72%);
  color: #f8fafc;
  border-right: 1px solid rgba(255, 255, 255, 0.08);
}

.brand-block {
  padding: 10px 12px 8px;
}

.brand-kicker,
.header-kicker {
  display: inline-block;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.brand-kicker {
  color: rgba(148, 163, 184, 0.92);
}

.brand-block h2 {
  margin: 10px 0 8px;
  font-size: 28px;
  line-height: 1.1;
  letter-spacing: -0.03em;
}

.brand-block p {
  margin: 0;
  font-size: 14px;
  line-height: 1.7;
  color: rgba(226, 232, 240, 0.78);
}

.menu {
  border-right: none;
  background: transparent;
}

.menu :deep(.el-menu-item) {
  height: 46px;
  border-radius: 12px;
  margin-bottom: 6px;
  color: rgba(226, 232, 240, 0.84);
}

.menu-item-content {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.menu-item-count {
  min-width: 22px;
  height: 22px;
  padding: 0 7px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 999px;
  background: rgba(248, 113, 113, 0.18);
  color: #fca5a5;
  font-size: 12px;
  font-weight: 700;
  line-height: 1;
}

.menu :deep(.el-menu-item:hover),
.menu :deep(.el-menu-item.is-active) {
  color: #ffffff;
  background: rgba(255, 255, 255, 0.08);
}

.menu :deep(.el-menu-item:hover) .menu-item-count,
.menu :deep(.el-menu-item.is-active) .menu-item-count {
  background: rgba(248, 113, 113, 0.24);
  color: #fecaca;
}

.aside-note {
  margin-top: auto;
  padding: 16px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.08);
}

.aside-note h4 {
  margin: 0 0 6px;
  font-size: 13px;
}

.aside-note p {
  margin: 0;
  font-size: 13px;
  line-height: 1.7;
  color: rgba(226, 232, 240, 0.72);
}

.content-shell {
  min-width: 0;
}

.header {
  height: auto;
  min-height: 108px;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 24px 28px 14px;
  background: transparent;
}

.header-copy {
  min-width: 0;
}

.header-kicker {
  color: #5b6a84;
}

.header-copy h3 {
  margin: 8px 0 6px;
  color: #0f172a;
  font-size: 28px;
  line-height: 1.15;
  letter-spacing: -0.03em;
}

.header-copy p {
  margin: 0;
  color: #5e6b81;
  font-size: 14px;
}

.main {
  padding: 0 28px 28px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  padding: 14px 16px;
  border-radius: 18px;
}

.identity-copy {
  display: grid;
  gap: 2px;
  margin-right: 2px;
}

.identity-copy strong {
  color: #0f172a;
  font-size: 14px;
  line-height: 1.2;
}

.identity-copy span {
  color: #66758c;
  font-size: 12px;
}

@media (max-width: 960px) {
  .app-shell {
    flex-direction: column;
  }

  .aside {
    width: 100% !important;
  }

  .header {
    height: auto;
    min-height: unset;
    padding: 20px 18px 12px;
  }

  .main {
    padding: 0 18px 18px;
  }
}
</style>
