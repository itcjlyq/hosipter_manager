<template>
  <el-container class="admin-shell">
    <el-aside width="220px" class="aside">
      <div class="brand">
        <span class="brand-dot" />
        <span class="brand-text">整形 · 管理后台</span>
      </div>
      <el-menu
        :default-active="active"
        router
        class="menu"
        background-color="transparent"
        text-color="#334155"
        active-text-color="#0d9488"
      >
        <el-menu-item index="/">
          <el-icon><Odometer /></el-icon>
          <span>概览</span>
        </el-menu-item>
        <el-menu-item index="/doctors">
          <el-icon><UserFilled /></el-icon>
          <span>医生管理</span>
        </el-menu-item>
        <el-menu-item index="/patients">
          <el-icon><Avatar /></el-icon>
          <span>患者管理</span>
        </el-menu-item>
        <el-menu-item index="/binds">
          <el-icon><Link /></el-icon>
          <span>医患绑定</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="header-title">{{ title }}</div>
        <div class="header-actions">
          <el-tag type="success" effect="light" round>管理员</el-tag>
          <el-button type="primary" plain round @click="logout">退出登录</el-button>
        </div>
      </el-header>
      <el-main class="main">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { Odometer, UserFilled, Avatar, Link } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const active = computed(() => route.path)
const title = computed(() => route.meta.title || '管理后台')

function logout() {
  auth.clear()
  router.push({ name: 'login' })
}
</script>

<style scoped lang="scss">
.aside {
  background: linear-gradient(180deg, #ffffff 0%, #f0fdfa 100%);
  border-right: 1px solid rgba(20, 184, 166, 0.12);
  box-shadow: 8px 0 24px rgba(15, 23, 42, 0.04);
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 20px 18px 16px;
  font-weight: 800;
  color: #0f172a;
  letter-spacing: 0.5px;
}

.brand-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: linear-gradient(135deg, #5eead4, #14b8a6);
  box-shadow: 0 0 0 6px rgba(20, 184, 166, 0.15);
}

.brand-text {
  font-size: 15px;
}

.menu {
  border-right: none;
  padding: 8px 8px 16px;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(8px);
  border-bottom: 1px solid rgba(148, 163, 184, 0.2);
}

.header-title {
  font-size: 18px;
  font-weight: 800;
  color: #0f172a;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.main {
  padding: 20px 22px 28px;
  min-height: calc(100vh - 60px);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.15s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
