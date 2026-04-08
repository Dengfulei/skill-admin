<template>
  <div class="login-shell">
    <div class="login-panel page-card">
      <div class="hero">
        <el-tag type="primary" effect="dark">V03 赛题实现</el-tag>
        <h1>Skill / MCP 配置数据库化与权限管控</h1>
        <p>基于 Spring Boot 3、MySQL 8、Vue3、Element Plus 的三级权限配置中心演示系统。</p>
      </div>
      <el-form :model="form" @submit.prevent="handleLogin">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="admin / sales_admin / sales_user / hr_user / alice" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" show-password placeholder="默认 123456" />
        </el-form-item>
        <el-button type="primary" size="large" style="width: 100%" @click="handleLogin">
          登录系统
        </el-button>
      </el-form>
      <div class="demo-box">
        <p>演示账号</p>
        <ul>
          <li>`admin`：系统管理员</li>
          <li>`sales_admin`：销售部管理员</li>
          <li>`sales_user`：销售部员工</li>
          <li>`hr_user`：人事部员工</li>
          <li>`alice`：个人用户</li>
        </ul>
      </div>
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
  padding: 24px;
  background:
    radial-gradient(circle at top left, rgba(37, 99, 235, 0.18), transparent 36%),
    radial-gradient(circle at bottom right, rgba(15, 118, 110, 0.18), transparent 32%);
}

.login-panel {
  width: min(920px, 100%);
  padding: 32px;
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  gap: 28px;
}

.hero h1 {
  margin: 14px 0 12px;
  font-size: 34px;
}

.hero p {
  color: #475569;
  line-height: 1.7;
}

.demo-box {
  margin-top: 18px;
  padding: 16px;
  border-radius: 14px;
  background: #f8fafc;
}

.demo-box ul {
  margin: 8px 0 0;
  padding-left: 18px;
  color: #475569;
}

@media (max-width: 900px) {
  .login-panel {
    grid-template-columns: 1fr;
  }
}
</style>
