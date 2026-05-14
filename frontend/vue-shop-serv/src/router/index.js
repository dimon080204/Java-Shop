import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/GoodsView.vue'
import PurchasesView from '../views/PurchasesView.vue'
import SalesView from '../views/SalesView.vue'
import EmployeesView from '../views/EmployeesView.vue'
import AnalyticsView from '../views/AnalyticsView.vue'
import LoginView from '../views/LoginView.vue'

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
    },
    {
      path: '/analytics',
      name: 'analytics',
      component: AnalyticsView
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView
    }
  ]
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token');

  // 1. Check if the route requires authentication
  const isAuthRequired = to.matched.some(record => record.meta.requiresAuth);

  if (isAuthRequired && !token) {
    // If authentication is required but no token is found — redirect to login
    next({ name: 'Login' });
  } else if (to.name === 'Login' && token) {
    // If the user is already logged in but tries to access the login page — redirect to inventory
    next({ name: 'Inventory' });
  } else {
    // In all other cases — allow navigation
    next();
  }
});

export default router
