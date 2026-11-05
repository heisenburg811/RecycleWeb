import { createRouter, createWebHistory } from 'vue-router'
import { TOKEN_KEY } from '@/api/request'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'home', component: () => import('@/views/Home.vue') },
    { path: '/search', name: 'search', component: () => import('@/views/Search.vue') },
    { path: '/item/:id', name: 'detail', component: () => import('@/views/Detail.vue') },
    { path: '/login', name: 'login', component: () => import('@/views/Login.vue') },
    { path: '/me', name: 'me', component: () => import('@/views/Me.vue') },
    {
      path: '/publish',
      name: 'publish',
      component: () => import('@/views/Publish.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/order/confirm/:id',
      name: 'orderConfirm',
      component: () => import('@/views/OrderConfirm.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/orders',
      name: 'orders',
      component: () => import('@/views/Orders.vue'),
      meta: { requiresAuth: true }
    }
  ],
  scrollBehavior: () => ({ top: 0 })
})

router.beforeEach((to) => {
  if (to.meta.requiresAuth && !localStorage.getItem(TOKEN_KEY)) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  return true
})

export default router
