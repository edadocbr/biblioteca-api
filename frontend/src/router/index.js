import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import LoginView from '../views/Login.vue'
import RegisterView from '../views/Register.vue'
import AdminLogin from '../views/admin/Login.vue'
import AdminDashboardView from '../views/admin/AdminDashboardView.vue'
import AdminLayout from '../layouts/AdminLayout.vue'
import InventarioView from '../views/admin/InventarioView.vue'
import PrestamosView from '../views/admin/PrestamosView.vue'
import DevolucionesView from '../views/admin/DevolucionesView.vue'
import RenovacionesView from '../views/admin/RenovacionesView.vue'

const routes = [
  {
    path: '/',
    name: 'home',
    component: Home
  },
  {
    path: '/login',
    name: 'login',
    component: LoginView
  },
  {
    path: '/register',
    name: 'register',
    component: RegisterView
  },
  {
    path: '/dashboard/login',
    name: 'admin-login',
    component: AdminLogin
  },
  {
    path: '/dashboard',
    component: AdminLayout,
    meta: { requiresAuth: true, role: 'BIBLIOTECARIO' },
    children: [
      { path: '', name: 'admin-resumen', component: AdminDashboardView },
      { path: 'inventario', name: 'admin-inventario', component: InventarioView },
      { path: 'prestamos', name: 'admin-prestamos', component: PrestamosView },
      { path: 'devoluciones', name: 'admin-devoluciones', component: DevolucionesView },
      { path: 'renovaciones', name: 'admin-renovaciones', component: RenovacionesView }
    ]
  },
  {
    path: '/admin',
    name: 'admin',
    component: AdminDashboardView
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  if (to.meta.requiresAuth) {
    try {
      const raw = localStorage.getItem('usuario_sgbu')
      if (!raw) {
        return { name: 'admin-login' }
      }
      const usuario = JSON.parse(raw)
      if (!usuario || usuario.rol !== 'BIBLIOTECARIO') {
        return { name: 'admin-login' }
      }
    } catch {
      return { name: 'admin-login' }
    }
  }

  if (to.path.startsWith('/admin')) {
    try {
      const raw = localStorage.getItem('usuario_sgbu')
      if (!raw) {
        return { path: '/login' }
      }
      const usuario = JSON.parse(raw)
      if (!usuario || usuario.rol !== 'BIBLIOTECARIO') {
        return { path: '/login' }
      }
    } catch {
      return { path: '/login' }
    }
  }
})

export default router
