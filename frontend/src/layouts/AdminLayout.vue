<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const router = useRouter()
const route = useRoute()

const usuario = ref(null)

function cargarUsuario() {
  try {
    const raw = localStorage.getItem('usuario_sgbu')
    if (raw) {
      usuario.value = JSON.parse(raw)
    } else {
      usuario.value = null
    }
  } catch {
    usuario.value = null
  }
}

onMounted(() => {
  cargarUsuario()
})

const operatorName = computed(() => {
  if (!usuario.value) return '—'
  return usuario.value.nombreCompleto || usuario.value.email || '—'
})

const operatorRole = computed(() => {
  if (!usuario.value) return '—'
  return usuario.value.rol || '—'
})

const modules = [
  { id: 'resumen', label: 'Resumen', to: '/dashboard', exact: true },
  { id: 'inventario', label: 'Inventario', cus: 'CUS-05', to: '/dashboard/inventario' },
  { id: 'prestamos', label: 'Préstamos', cus: 'CUS-02', to: '/dashboard/prestamos' },
  { id: 'devoluciones', label: 'Devoluciones', cus: 'CUS-03', to: '/dashboard/devoluciones' },
  { id: 'renovaciones', label: 'Renovaciones', cus: 'CUS-04', to: '/dashboard/renovaciones' },
]

function isActive(item) {
  if (item.exact) {
    return route.path === item.to
  }
  return route.path.startsWith(item.to)
}

function logout() {
  localStorage.removeItem('usuario_sgbu')
  router.push('/dashboard/login')
}
</script>

<template>
  <div class="min-h-screen bg-neutral-50 text-neutral-900 lg:flex">
    <aside class="flex w-full flex-col justify-between border-b border-neutral-200 bg-neutral-50/50 lg:sticky lg:top-0 lg:h-screen lg:w-64 lg:shrink-0 lg:border-b-0 lg:border-r">
      <div>
        <div class="border-b border-neutral-200 px-5 py-5">
          <p class="font-mono text-[11px] font-medium uppercase tracking-widest text-neutral-900">
            Biblioteca / Control
          </p>
          <p class="mt-2 flex items-center gap-2 font-mono text-[10px] uppercase tracking-wider text-neutral-400">
            <span class="h-1.5 w-1.5 border border-neutral-900" aria-hidden="true"></span>
            Módulo operativo
          </p>
        </div>

        <nav aria-label="Módulos administrativos" class="flex gap-1 overflow-x-auto p-2 lg:flex-col">
          <RouterLink
            v-for="module in modules"
            :key="module.id"
            :to="module.to"
            class="flex min-w-max items-center gap-2 border-l-2 px-3 py-2 text-left text-sm lg:min-w-0"
            :class="isActive(module)
              ? 'border-l-neutral-900 bg-neutral-200/60 font-medium text-neutral-900'
              : 'border-l-transparent text-neutral-500 hover:bg-neutral-100 hover:text-neutral-900'"
            :aria-current="isActive(module) ? 'page' : undefined"
          >
            <span
              class="h-1.5 w-1.5 shrink-0 border border-current"
              aria-hidden="true"
            ></span>
            <span>{{ module.label }}</span>
            <span v-if="module.cus" class="ml-auto hidden font-mono text-[9px] text-neutral-400 lg:inline">
              {{ module.cus }}
            </span>
          </RouterLink>
        </nav>
      </div>

      <div class="border-t border-neutral-200 p-4">
        <p class="font-mono text-[10px] uppercase tracking-wider text-neutral-400">
          Operador
        </p>
        <p class="mt-1 text-xs font-medium text-neutral-900">{{ operatorName }}</p>
        <p class="mt-1 font-mono text-[10px] uppercase tracking-wider text-neutral-400">
          Rol: {{ operatorRole }}
        </p>
        <button
          type="button"
          class="mt-4 w-full border border-neutral-200 bg-white px-3 py-2 text-left font-mono text-[10px] uppercase tracking-wider text-neutral-500 transition-colors duration-150 hover:border-neutral-400 hover:text-neutral-900 focus-visible:outline focus-visible:outline-1 focus-visible:outline-offset-2 focus-visible:outline-neutral-900"
          @click="logout"
        >
          Cerrar sesión
        </button>
      </div>
    </aside>

    <main class="min-w-0 flex-1">
      <RouterView />
    </main>
  </div>
</template>
