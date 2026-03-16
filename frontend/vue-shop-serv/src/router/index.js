import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/GoodsView.vue'
import PurchasesView from '../views/PurchasesView.vue'
import SalesView from '../views/SalesView.vue'
import EmployeesView from '../views/EmployeesView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView
    },
    {
      path: '/purchases',
      name: 'purchases',
      component: PurchasesView
    },
    {
      path: '/sales',
      name: 'sales',
      component: SalesView
    },
    {
      path: '/employees',
      name: 'employees',
      component: EmployeesView
    }
  ]
})

export default router
