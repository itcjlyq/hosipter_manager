import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/Login.vue'),
    meta: { public: true, title: '登录' }
  },
  {
    path: '/',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { title: '管理后台' },
    children: [
      {
        path: '',
        name: 'dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '概览' }
      },
      {
        path: 'doctors',
        name: 'doctors',
        component: () => import('@/views/Doctors.vue'),
        meta: { title: '医生管理' }
      },
      {
        path: 'patients',
        name: 'patients',
        component: () => import('@/views/Patients.vue'),
        meta: { title: '患者管理' }
      },
      {
        path: 'binds',
        name: 'binds',
        component: () => import('@/views/Binds.vue'),
        meta: { title: '医患绑定' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const auth = useAuthStore()
  if (!to.meta.public && !auth.isLoggedIn) {
    next({ name: 'login', query: { redirect: to.fullPath } })
    return
  }
  if (to.name === 'login' && auth.isLoggedIn) {
    next({ name: 'dashboard' })
    return
  }
  document.title = to.meta.title ? `${to.meta.title} · 医院管理后台` : '医院管理后台'
  next()
})

export default router
