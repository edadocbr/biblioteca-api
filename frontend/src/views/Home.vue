<script setup>
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import InlineAlert from '../components/ui/InlineAlert.vue'
import Toast from '../components/ui/Toast.vue'

const router = useRouter()

const categorias = ['Todos', 'Arquitectura', 'Diseño', 'Filosofía', 'Ensayo']
const categoriaSeleccionada = ref('Todos')
const busqueda = ref('')
const libroSeleccionado = ref(null)
const cargando = ref(true)

const usuario = ref(null)
const menuAbierto = ref(false)
const notificacion = ref(null) // { message, variant }

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

function toggleMenu() {
  menuAbierto.value = !menuAbierto.value
}

function cerrarSesion() {
  localStorage.removeItem('usuario_sgbu')
  usuario.value = null
  menuAbierto.value = false
}

function onClickFuera(e) {
  const el = document.getElementById('user-menu')
  if (el && !el.contains(e.target)) {
    menuAbierto.value = false
  }
}

function showNotificacion(message, variant = 'error') {
  notificacion.value = { message, variant }
  setTimeout(() => { notificacion.value = null }, 5000)
}

const PORTADA_DEFAULT = 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=800&auto=format&fit=crop&q=80'

const libros = ref([])

onUnmounted(() => {
  window.removeEventListener('storage', cargarUsuario)
  document.removeEventListener('click', onClickFuera)
})

onMounted(async () => {
  cargarUsuario()
  window.addEventListener('storage', cargarUsuario)
  document.addEventListener('click', onClickFuera)
  try {
    const params = usuario.value ? `?idLector=${usuario.value.idUsuario}` : ''
    const res = await fetch(`http://localhost:8080/api/catalogo/libros${params}`)
    if (!res.ok) throw new Error('Error al consultar el catálogo')

    const data = await res.json()

    libros.value = data.map((item, index) => ({
      id: item.isbn,
      isbn: item.isbn,
      titulo: item.titulo,
      autor: item.autor,
      anio: item.anioPublicacion,
      categoria: categorias[(index % 4) + 1],
      stockTotal: item.stockTotal,
      stockDisponible: item.stockDisponible,
      estadoSolicitud: item.estadoSolicitud || null,
      estado: item.stockDisponible > 0 ? 'Disponible' : 'Sin stock',
      sinopsis: `Obra publicada por la editorial ${item.editorial}. Ejemplares registrados en el sistema: ${item.stockDisponible} unidad(es) disponible(s).`,
      portada: PORTADA_DEFAULT
    }))
  } catch (error) {
    console.error('Error cargando libros desde la base de datos:', error)
    showNotificacion('No se pudo cargar el catálogo de libros.')
  } finally {
    cargando.value = false
  }
})

const librosFiltrados = computed(() => {
  const termino = busqueda.value.trim().toLowerCase()
  return libros.value.filter((libro) => {
    const coincideCategoria = categoriaSeleccionada.value === 'Todos' || libro.categoria === categoriaSeleccionada.value
    if (!coincideCategoria) return false
    if (!termino) return true
    return (
      libro.titulo.toLowerCase().includes(termino) ||
      libro.autor.toLowerCase().includes(termino) ||
      libro.isbn.toLowerCase().includes(termino)
    )
  })
})

const totalDisponibles = computed(() => libros.value.filter((l) => l.stockDisponible > 0).length)

function estadoBoton(libro) {
  if (usuario.value && usuario.value.rol === 'BIBLIOTECARIO') {
    return { label: 'Modo Administrador', disabled: true, variant: 'disabled' }
  }
  if (libro.estadoSolicitud === 'LISTA_PARA_RECOJO') {
    return { label: 'Listo para recojo (En ventanilla)', disabled: true, variant: 'disabled' }
  }
  if (libro.estadoSolicitud === 'EN_ESPERA') {
    return { label: 'En lista de espera', disabled: true, variant: 'disabled' }
  }
  if (libro.stockDisponible > 0) {
    return { label: 'Solicitar préstamo', disabled: false, variant: 'active' }
  }
  return { label: 'Reservar ejemplar', disabled: false, variant: 'active' }
}

function abrirDetalle(libro) {
  libroSeleccionado.value = libro
}

function cerrarDetalle() {
  libroSeleccionado.value = null
}

async function solicitarReserva(libro, evt) {
  evt.stopPropagation()
  notificacion.value = null

  const raw = localStorage.getItem('usuario_sgbu')
  if (!raw) {
    router.push({ path: '/login', query: { redirect: '/' } })
    return
  }

  let sesion
  try {
    sesion = JSON.parse(raw)
  } catch {
    router.push({ path: '/login', query: { redirect: '/' } })
    return
  }

  if (sesion && sesion.rol === 'BIBLIOTECARIO') {
    showNotificacion('Los bibliotecarios no pueden realizar solicitudes de reserva o préstamo.')
    return
  }

  const idLector = sesion.idUsuario
  if (!idLector) {
    showNotificacion('Sesión inválida. Inicia sesión de nuevo.')
    return
  }

  try {
    const res = await fetch('http://localhost:8080/api/reservas/solicitar', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ idLibro: libro.id, idLector })
    })

    if (res.ok) {
      const data = await res.json()
      const idx = libros.value.findIndex((l) => l.id === libro.id)
      if (idx !== -1) {
        libros.value[idx].estadoSolicitud = data.estado
      }
      if (libroSeleccionado.value && libroSeleccionado.value.id === libro.id) {
        libroSeleccionado.value.estadoSolicitud = data.estado
      }
      showNotificacion(data.mensaje || 'Solicitud registrada correctamente.', 'success')
    } else {
      const txt = await res.text().catch(() => '')
      try {
        const errObj = JSON.parse(txt)
        showNotificacion(errObj.message || errObj.error || 'No se pudo procesar la solicitud.')
      } catch (_) {
        showNotificacion(txt || 'No se pudo procesar la solicitud.')
      }
    }
  } catch (e) {
    showNotificacion('Error de conexión con el servidor.')
  }
}
</script>

<template>
  <div class="min-h-screen bg-white text-neutral-900">
    <header class="border-b border-neutral-200">
      <div class="max-w-7xl mx-auto px-6 py-8">
        <div class="flex flex-col gap-6 md:flex-row md:items-end md:justify-between">
          <div class="flex flex-col gap-1">
            <h1 class="text-lg tracking-tight font-medium">BIBLIOTECA UNIVERSITARIA</h1>
            <p class="text-xs uppercase tracking-widest font-mono text-neutral-500">
              Catálogo de acceso público
            </p>
          </div>
          <div class="flex flex-col gap-4 md:flex-row md:items-center md:gap-6">
            <div class="w-full md:w-80">
              <input
                v-model="busqueda"
                type="search"
                placeholder="Buscar por título, autor o ISBN"
                class="w-full border-b border-neutral-200 bg-transparent px-0 py-2 text-sm focus:outline-none focus:border-neutral-900 transition-colors duration-150 ease-out"
              />
            </div>
            <p class="text-xs uppercase tracking-widest font-mono text-neutral-500">
              {{ totalDisponibles }} disponibles
            </p>
            <div v-if="!usuario" class="whitespace-nowrap">
              <RouterLink
                to="/login"
                class="text-xs uppercase tracking-widest font-mono text-neutral-500 hover:text-neutral-900 transition-colors duration-150 underline-offset-4 hover:underline focus-visible:outline focus-visible:outline-1 focus-visible:outline-offset-4 focus-visible:outline-neutral-900"
              >
                Iniciar sesión
              </RouterLink>
            </div>
            <div v-else id="user-menu" class="relative whitespace-nowrap">
              <button
                type="button"
                class="flex items-center gap-2 border border-neutral-200 px-2.5 py-1.5 text-neutral-900 hover:bg-neutral-50 focus-visible:outline focus-visible:outline-1 focus-visible:outline-offset-2 focus-visible:outline-neutral-900"
                @click.stop="toggleMenu"
              >
                <span class="inline-flex h-5 w-5 items-center justify-center border border-neutral-900">
                  <svg width="10" height="10" viewBox="0 0 12 12" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <circle cx="6" cy="4" r="2" stroke="currentColor" stroke-width="1"/>
                    <path d="M3 10c0-1.657 1.343-3 3-3s3 1.343 3 3" stroke="currentColor" stroke-width="1"/>
                  </svg>
                </span>
                <span class="text-xs uppercase tracking-widest font-mono">{{ usuario.nombreCompleto.split(' ')[0] }}</span>
              </button>
              <Transition name="menu-fade">
                <div
                  v-if="menuAbierto"
                  class="absolute right-0 top-full mt-2 w-72 border border-neutral-200 bg-white shadow-sm"
                >
                  <div class="p-3">
                    <p class="text-xs font-medium text-neutral-900">{{ usuario.nombreCompleto }}</p>
                    <p class="mt-1 font-mono text-xs uppercase text-neutral-500">
                      {{ usuario.codigoEstudiante || '—' }} · Límite reservas: {{ usuario.limiteReservas ?? 3 }}
                    </p>
                  </div>
                  <div class="border-t border-neutral-200 p-2">
                    <button
                      type="button"
                      class="w-full text-left px-2 py-1.5 text-xs uppercase tracking-widest font-mono text-neutral-900 hover:text-neutral-500"
                      @click="cerrarSesion"
                    >
                      Cerrar sesión
                    </button>
                  </div>
                </div>
              </Transition>
            </div>
          </div>
        </div>
        <nav class="mt-8">
          <ul class="flex flex-wrap items-center gap-x-6 gap-y-2">
            <li v-for="categoria in categorias" :key="categoria">
              <button
                type="button"
                @click="categoriaSeleccionada = categoria"
                class="text-sm transition-colors duration-150 ease-out"
                :class="categoriaSeleccionada === categoria ? 'text-neutral-900 underline underline-offset-4 decoration-neutral-900' : 'text-neutral-500 hover:text-neutral-900'"
              >
                {{ categoria }}
              </button>
            </li>
          </ul>
        </nav>
      </div>
    </header>

    <main>
      <div class="max-w-7xl mx-auto px-6 py-12">
        <InlineAlert
          v-if="notificacion"
          :variant="notificacion.variant"
          dismissible
          @dismiss="notificacion = null"
        >
          {{ notificacion.message }}
        </InlineAlert>

        <div class="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-5 gap-6">
          <article
            v-for="libro in librosFiltrados"
            :key="libro.id"
            class="group cursor-pointer"
            @click="abrirDetalle(libro)"
          >
            <div class="aspect-[3/4] border border-neutral-200 overflow-hidden bg-neutral-50 transition-colors duration-150 ease-out group-hover:border-neutral-900">
              <img
                :src="libro.portada"
                :alt="libro.titulo"
                class="h-full w-full object-cover transition-opacity duration-150 ease-out group-hover:opacity-90"
                loading="lazy"
              />
            </div>
            <div class="mt-3 flex flex-col gap-1">
              <h2 class="text-sm font-medium leading-snug tracking-tight">
                {{ libro.titulo }}
              </h2>
              <p class="text-xs text-neutral-500">
                {{ libro.autor }}
              </p>
              <div class="mt-2 flex items-center justify-between">
                <p class="text-xs uppercase tracking-widest font-mono text-neutral-500">
                  {{ libro.anio }}
                </p>
                <span
                  class="text-xs uppercase tracking-widest font-mono"
                  :class="libro.estado === 'Disponible' ? 'text-neutral-900' : 'text-neutral-500'"
                >
                  {{ libro.estado }}
                </span>
              </div>
              <button
                type="button"
                :disabled="estadoBoton(libro).disabled"
                @click="solicitarReserva(libro, $event)"
                class="w-full mt-2 border px-2.5 py-1.5 font-mono text-[10px] uppercase tracking-wider transition-colors"
                :class="
                  estadoBoton(libro).variant === 'disabled'
                    ? 'border-neutral-200 bg-neutral-100 text-neutral-400 cursor-not-allowed'
                    : 'border-neutral-900 bg-neutral-900 text-white hover:bg-neutral-800'
                "
              >
                {{ estadoBoton(libro).label }}
              </button>
            </div>
          </article>
        </div>
        <div v-if="librosFiltrados.length === 0" class="mt-12">
          <p class="text-sm text-neutral-500">
            No se encontraron resultados.
          </p>
        </div>
      </div>
    </main>

    <Transition
      enter-active-class="transition-opacity duration-150 ease-out"
      enter-from-class="opacity-0"
      enter-to-class="opacity-100"
      leave-active-class="transition-opacity duration-150 ease-out"
      leave-from-class="opacity-100"
      leave-to-class="opacity-0"
    >
      <div
        v-if="libroSeleccionado"
        class="fixed inset-0 bg-neutral-900/10"
        @click="cerrarDetalle"
      ></div>
    </Transition>

    <Transition
      enter-active-class="transition-transform duration-150 ease-out"
      enter-from-class="translate-x-full"
      enter-to-class="translate-x-0"
      leave-active-class="transition-transform duration-150 ease-out"
      leave-from-class="translate-x-0"
      leave-to-class="translate-x-full"
    >
      <aside
        v-if="libroSeleccionado"
        class="fixed right-0 top-0 h-full w-full max-w-md border-l border-neutral-200 bg-white"
      >
        <div class="flex h-full flex-col">
          <div class="flex items-center justify-between border-b border-neutral-200 px-6 py-4">
            <p class="text-xs uppercase tracking-widest font-mono text-neutral-500">
              Ficha
            </p>
            <button
              type="button"
              @click="cerrarDetalle"
              class="text-sm text-neutral-500 transition-colors duration-150 ease-out hover:text-neutral-900"
            >
              Cerrar
            </button>
          </div>
          <div class="flex-1 overflow-y-auto px-6 py-8">
            <div class="aspect-[3/4] w-full max-w-xs border border-neutral-200 overflow-hidden bg-neutral-50">
              <img
                :src="libroSeleccionado.portada"
                :alt="libroSeleccionado.titulo"
                class="h-full w-full object-cover"
              />
            </div>
            <div class="mt-6 flex flex-col gap-2">
              <h2 class="text-lg tracking-tight font-medium">
                {{ libroSeleccionado.titulo }}
              </h2>
              <p class="text-sm text-neutral-500">
                {{ libroSeleccionado.autor }}
              </p>
            </div>
            <div class="mt-8">
              <dl class="border-t border-neutral-200">
                <div class="grid grid-cols-2 gap-4 border-b border-neutral-200 py-3">
                  <dt class="text-xs uppercase tracking-widest font-mono text-neutral-500">ISBN</dt>
                  <dd class="text-xs font-mono text-neutral-900 text-right">
                    {{ libroSeleccionado.isbn }}
                  </dd>
                </div>
                <div class="grid grid-cols-2 gap-4 border-b border-neutral-200 py-3">
                  <dt class="text-xs uppercase tracking-widest font-mono text-neutral-500">Año</dt>
                  <dd class="text-xs font-mono text-neutral-900 text-right">
                    {{ libroSeleccionado.anio }}
                  </dd>
                </div>
                <div class="grid grid-cols-2 gap-4 border-b border-neutral-200 py-3">
                  <dt class="text-xs uppercase tracking-widest font-mono text-neutral-500">Categoría</dt>
                  <dd class="text-xs uppercase tracking-widest font-mono text-neutral-900 text-right">
                    {{ libroSeleccionado.categoria }}
                  </dd>
                </div>
                <div class="grid grid-cols-2 gap-4 border-b border-neutral-200 py-3">
                  <dt class="text-xs uppercase tracking-widest font-mono text-neutral-500">Estado</dt>
                  <dd
                    class="text-xs uppercase tracking-widest font-mono text-right"
                    :class="libroSeleccionado.estado === 'Disponible' ? 'text-neutral-900' : 'text-neutral-500'"
                  >
                    {{ libroSeleccionado.estado }}
                  </dd>
                </div>
                <div class="grid grid-cols-2 gap-4 border-b border-neutral-200 py-3">
                  <dt class="text-xs uppercase tracking-widest font-mono text-neutral-500">Solicitud</dt>
                  <dd class="text-xs uppercase tracking-widest font-mono text-right text-neutral-600">
                    {{ libroSeleccionado.estadoSolicitud || '—' }}
                  </dd>
                </div>
              </dl>
            </div>
            <div class="mt-8">
              <p class="text-xs uppercase tracking-widest font-mono text-neutral-500">
                Sinopsis
              </p>
              <p class="mt-3 text-sm leading-relaxed text-neutral-900">
                {{ libroSeleccionado.sinopsis }}
              </p>
            </div>
          </div>
          <div class="border-t border-neutral-200 px-6 py-4">
            <button
              type="button"
              :disabled="estadoBoton(libroSeleccionado).disabled"
              @click="solicitarReserva(libroSeleccionado, $event)"
              class="w-full border px-4 py-2 text-sm tracking-wide transition-colors"
              :class="
                estadoBoton(libroSeleccionado).variant === 'disabled'
                  ? 'border-neutral-200 bg-neutral-100 text-neutral-400 cursor-not-allowed'
                  : 'bg-neutral-900 text-white hover:bg-neutral-800 active:scale-[0.99]'
              "
            >
              {{ estadoBoton(libroSeleccionado).label }}
            </button>
          </div>
        </div>
      </aside>
    </Transition>

    <Toast
      :open="!!notificacion"
      :message="notificacion ? notificacion.message : ''"
      :variant="notificacion ? notificacion.variant : 'success'"
      @close="notificacion = null"
    />
  </div>
</template>

<style scoped>
.menu-fade-enter-active,
.menu-fade-leave-active {
  transition: opacity 150ms ease-out;
}
.menu-fade-enter-from,
.menu-fade-leave-to {
  opacity: 0;
}
</style>
