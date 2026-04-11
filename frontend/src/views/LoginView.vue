<template>
  <div class="login-shell">
    <div class="login-panel page-card">
      <section class="intro-panel">
        <span class="eyebrow">Skill Governance Workspace</span>
        <h1>Skill / MCP 配置数据库化与权限管控</h1>
        <p class="intro-copy">
          面向企业团队的统一资源控制台，支持公共级、部门级、个人级技能分配，以及运行时自动鉴权。
        </p>
        <div class="feature-list">
          <div>
            <strong>统一存储</strong>
            <span>Skill 与 MCP 配置数据库化管理</span>
          </div>
          <div>
            <strong>权限清晰</strong>
            <span>管理员、部门管理员、普通用户边界明确</span>
          </div>
          <div>
            <strong>演示完整</strong>
            <span>申请、审批、鉴权链路一站式验证</span>
          </div>
        </div>
      </section>
      <section class="form-panel">
        <div class="form-heading">
          <h2>进入控制台</h2>
          <p>请使用演示账号登录，查看不同角色下的可见资源与管理边界。</p>
        </div>
        <el-form
          :model="form"
          label-position="top"
          data-testid="login-form"
          @submit.prevent="handleLogin"
        >
          <el-form-item label="用户名">
            <el-input
              v-model="form.username"
              placeholder="admin / sales_admin / tech_admin / dengfulei / alice"
              name="username"
              aria-label="登录用户名"
              data-testid="login-username"
            />
          </el-form-item>
          <el-form-item label="密码">
            <el-input
              v-model="form.password"
              show-password
              placeholder="默认 123456"
              name="password"
              aria-label="登录密码"
              data-testid="login-password"
            />
          </el-form-item>
          <el-button
            type="primary"
            size="large"
            class="submit-btn"
            data-testid="login-submit"
            @click="handleLogin"
          >
            登录系统
          </el-button>
        </el-form>
        <div class="demo-box">
          <p>演示账号</p>
          <ul>
            <li><code>admin</code><span>系统管理员</span></li>
            <li><code>sales_admin</code><span>销售部管理员</span></li>
            <li><code>tech_admin</code><span>技术部管理员</span></li>
            <li><code>sales_user</code><span>销售部员工</span></li>
            <li><code>hr_user</code><span>人事部员工</span></li>
            <li><code>dengfulei</code><span>技术部员工</span></li>
            <li><code>alice</code><span>个人用户</span></li>
          </ul>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const form = reactive({
  username: 'admin',
  password: '123456'
})

async function handleLogin() {
  await authStore.login(form.username, form.password)
  ElMessage.success('登录成功')
  router.push('/')
}
</script>

<style scoped>
.login-shell {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 28px;
  background:
    radial-gradient(circle at top left, rgba(21, 94, 239, 0.16), transparent 35%),
    radial-gradient(circle at bottom right, rgba(15, 118, 110, 0.14), transparent 28%);
}

.login-panel {
  width: min(1080px, 100%);
  padding: 18px;
  display: grid;
  grid-template-columns: minmax(0, 1.1fr) minmax(360px, 420px);
  gap: 18px;
}

.intro-panel,
.form-panel {
  border-radius: 20px;
}

.intro-panel {
  padding: 34px;
  background:
    linear-gradient(135deg, rgba(15, 23, 42, 0.96), rgba(15, 62, 168, 0.92)),
    radial-gradient(circle at top left, rgba(255, 255, 255, 0.12), transparent 36%);
  color: #f8fafc;
}

.eyebrow {
  display: inline-block;
  color: rgba(191, 219, 254, 0.9);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.14em;
  text-transform: uppercase;
}

.intro-panel h1 {
  margin: 16px 0 14px;
  font-size: clamp(34px, 4vw, 52px);
  line-height: 1.04;
  letter-spacing: -0.045em;
}

.intro-copy {
  max-width: 520px;
  margin: 0;
  color: rgba(226, 232, 240, 0.84);
  font-size: 16px;
  line-height: 1.8;
}

.feature-list {
  display: grid;
  gap: 14px;
  margin-top: 28px;
}

.feature-list div {
  display: grid;
  gap: 4px;
  padding-top: 14px;
  border-top: 1px solid rgba(255, 255, 255, 0.12);
}

.feature-list strong {
  font-size: 15px;
}

.feature-list span {
  color: rgba(226, 232, 240, 0.76);
  font-size: 14px;
}

.form-panel {
  padding: 28px;
  background: rgba(255, 255, 255, 0.92);
}

.form-heading {
  margin-bottom: 18px;
}

.form-heading h2 {
  margin: 0 0 6px;
  color: #0f172a;
  font-size: 28px;
  line-height: 1.15;
  letter-spacing: -0.03em;
}

.form-heading p {
  margin: 0;
  color: #5e6b81;
  font-size: 14px;
  line-height: 1.7;
}

.submit-btn {
  width: 100%;
  margin-top: 6px;
}

.demo-box {
  margin-top: 22px;
  padding: 16px 18px;
  border-radius: 16px;
  background: #f7f9fc;
  border: 1px solid rgba(15, 23, 42, 0.08);
}

.demo-box p {
  margin: 0 0 12px;
  color: #23314a;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

.demo-box ul {
  display: grid;
  gap: 10px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.demo-box li {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  color: #4a5870;
  font-size: 14px;
}

.demo-box code {
  padding: 4px 8px;
  border-radius: 999px;
  background: #eef4ff;
  color: #20459d;
  font-size: 12px;
  font-family: "JetBrains Mono", "SFMono-Regular", monospace;
}

@media (max-width: 900px) {
  .login-panel {
    grid-template-columns: 1fr;
    width: min(680px, 100%);
  }

  .intro-panel,
  .form-panel {
    padding: 24px;
  }
}
</style>
