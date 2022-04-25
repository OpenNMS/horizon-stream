import { createRouter, createWebHashHistory } from 'vue-router'
import Dashboard from '@/containers/Dashboard.vue'

const router = createRouter({
  history: createWebHashHistory('/opennms/ui'),
  routes: [
    {
      path: '/',
      name: 'Dashboard',
      component: Dashboard
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/containers/Login.vue')
    },
    {
      path: '/:pathMatch(.*)*', // catch other paths and redirect
      redirect: '/'
    }
  ]
})

export default router
