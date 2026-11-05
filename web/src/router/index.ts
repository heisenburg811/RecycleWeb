import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'home', component: () => import('@/views/Home.vue') },
    { path: '/search', name: 'search', component: () => import('@/views/Search.vue') },
    { path: '/item/:id', name: 'detail', component: () => import('@/views/Detail.vue') },
    { path: '/publish', name: 'publish', component: () => import('@/views/Publish.vue') },
    { path: '/order/confirm/:id', name: 'orderConfirm', component: () => import('@/views/OrderConfirm.vue') },
    { path: '/orders', name: 'orders', component: () => import('@/views/Orders.vue') }
  ],
  scrollBehavior: () => ({ top: 0 })
})

export default router
