<script setup>
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { RouterLink, useRouter } from 'vue-router'

const router = useRouter()

const categorias = ['Todos', 'Arquitectura', 'Diseño', 'Filosofía', 'Ensayo']
const categoriaSeleccionada = ref('Todos')
const busqueda = ref('')
const libroSeleccionado = ref(null)
const cargando = ref(true)

const usuario = ref(null)
const menuAbierto = ref(false)

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

// Imagen estática compartida para cubrir la falta de campo en el modelo de base de datos
const PORTADA_DEFAULT = 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=800&auto=format&fit=crop&q=80'

// Estado reactivo que ahora se alimenta de MariaDB
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
    const res = await fetch('http://localhost:8080/api/libros')
    if (!res.ok) throw new Error('Error al consultar el catálogo')
    
    const data = await res.json()

    // Adaptamos el DTO de Spring Boot al formato visual que consume el template
    libros.value = data.map((item, index) => ({
      id: item.isbn,
      titulo: item.titulo,
      autor: item.autor,
      anio: item.anioPublicacion,
      isbn: item.isbn,
      categoria: categorias[(index % 4) + 1], // Distribución visual entre las categorías existentes
      estado: item.ejemplaresDisponibles > 0 ? 'Disponible' : 'En préstamo',
      sinopsis: `Obra publicada por la editorial ${item.editorial}. Ejemplares registrados en el sistema: ${item.ejemplaresDisponibles} unidad(es) disponible(s).`,
      portada: PORTADA_DEFAULT
    }))
  } catch (error) {
    console.error('Error cargando libros desde la base de datos:', error)
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

const totalDisponibles = computed(() => libros.value.filter((l) => l.estado === 'Disponible').length)

function abrirDetalle(libro) {
  libroSeleccionado.value = libro
}

function cerrarDetalle() {
  libroSeleccionado.value = null
}

function reservarEjemplar() {
  router.push('/login')
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
              class="w-full bg-neutral-900 text-white px-4 py-2 text-sm tracking-wide transition-colors duration-150 ease-out hover:bg-neutral-800 active:scale-[0.99]"
              @click="reservarEjemplar"
            >
              Reservar ejemplar
            </button>
          </div>
        </div>
      </aside>
    </Transition>
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
