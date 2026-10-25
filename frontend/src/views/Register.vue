<script setup>
import { apiFetch } from '../api/client.js'
import { useLectorAuth } from '../composables/useAuth'
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const nombreCompleto = ref('')
const dni = ref('')
const email = ref('')
const password = ref('')
const codigoEstudiante = ref('')
const error = ref('')

async function handleSubmit(e) {
  e.preventDefault()
  error.value = ''
  try {
    const res = await apiFetch('/auth/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        nombreCompleto: nombreCompleto.value.trim(),
        dni: dni.value.trim(),
        email: email.value.trim(),
        password: password.value,
        codigoEstudiante: codigoEstudiante.value.trim()
      })
    })
    if (res.ok) {
      const data = await res.json()
      useLectorAuth().login(data)
      router.push('/')
      return
    }
    const txt = await res.text()
    error.value = txt || 'Error al registrar'
  } catch (e) {
    error.value = 'No se pudo conectar con el servidor'
  }
}
</script>

<template>
  <div class="min-h-screen flex flex-col justify-between bg-white px-6 py-12 text-neutral-900">
    <header class="flex justify-center">
      <RouterLink
        to="/"
        class="text-lg font-medium tracking-widest text-neutral-900 focus-visible:outline focus-visible:outline-1 focus-visible:outline-offset-4 focus-visible:outline-neutral-900"
      >
        BIBLIOTECA
      </RouterLink>
    </header>

    <main class="w-full max-w-sm mx-auto">
      <h1 class="text-2xl font-medium tracking-tight text-neutral-900 mb-1">
        Crear cuenta
      </h1>
      <p class="text-sm text-neutral-500 mb-8">
        Regístrate para solicitar préstamos y consultar tu historial de lectura.
      </p>

      <form class="space-y-5" @submit="handleSubmit">
        <div>
          <label
            for="nombre"
            class="block mb-2 font-mono text-[11px] uppercase tracking-[0.12em] text-neutral-500"
          >
            Nombre completo
          </label>
          <input
            id="nombre"
            v-model="nombreCompleto"
            type="text"
            autocomplete="name"
            placeholder="Ada Lovelace"
            required
            class="border border-neutral-200 focus:border-neutral-900 outline-none px-3.5 py-2.5 text-sm text-neutral-900 bg-transparent rounded-none transition-colors duration-150 w-full"
          />
        </div>

        <div>
          <label
            for="dni"
            class="block mb-2 font-mono text-[11px] uppercase tracking-[0.12em] text-neutral-500"
          >
            DNI
          </label>
          <input
            id="dni"
            v-model="dni"
            type="text"
            required
            class="border border-neutral-200 focus:border-neutral-900 outline-none px-3.5 py-2.5 text-sm text-neutral-900 bg-transparent rounded-none transition-colors duration-150 w-full"
          />
        </div>

        <div>
          <label
            for="codigo"
            class="block mb-2 font-mono text-[11px] uppercase tracking-[0.12em] text-neutral-500"
          >
            Código de estudiante
          </label>
          <input
            id="codigo"
            v-model="codigoEstudiante"
            type="text"
            required
            class="border border-neutral-200 focus:border-neutral-900 outline-none px-3.5 py-2.5 text-sm text-neutral-900 bg-transparent rounded-none transition-colors duration-150 w-full"
          />
        </div>

        <div>
          <label
            for="correo"
            class="block mb-2 font-mono text-[11px] uppercase tracking-[0.12em] text-neutral-500"
          >
            Correo electrónico
          </label>
          <input
            id="correo"
            v-model="email"
            type="email"
            autocomplete="email"
            placeholder="ada@ejemplo.com"
            required
            class="border border-neutral-200 focus:border-neutral-900 outline-none px-3.5 py-2.5 text-sm text-neutral-900 bg-transparent rounded-none transition-colors duration-150 w-full"
          />
        </div>

        <div>
          <label
            for="password"
            class="block mb-2 font-mono text-[11px] uppercase tracking-[0.12em] text-neutral-500"
          >
            Contraseña
          </label>
          <input
            id="password"
            v-model="password"
            type="password"
            autocomplete="new-password"
            required
            class="border border-neutral-200 focus:border-neutral-900 outline-none px-3.5 py-2.5 text-sm text-neutral-900 bg-transparent rounded-none transition-colors duration-150 w-full"
          />
        </div>

        <div v-if="error" class="border border-neutral-200 bg-neutral-50 px-3 py-2 font-mono text-xs text-neutral-700">
          {{ error }}
        </div>

        <button
          type="submit"
          class="w-full bg-neutral-900 text-white py-2.5 text-sm font-medium rounded-none hover:bg-neutral-800 transition-colors duration-150 focus-visible:outline focus-visible:outline-1 focus-visible:outline-offset-2 focus-visible:outline-neutral-900 active:scale-[0.99]"
        >
          Crear cuenta
        </button>
      </form>

      <p class="mt-6 text-xs text-neutral-500">
        ¿Ya tienes cuenta?
        <RouterLink
          to="/login"
          class="text-neutral-500 text-xs underline underline-offset-4 transition-colors duration-150 ease-out hover:text-neutral-900 focus-visible:outline-none focus-visible:text-neutral-900"
        >
          Iniciar sesión
        </RouterLink>
      </p>
    </main>

    <footer class="flex flex-col items-center gap-5">
      <RouterLink
        to="/"
        class="text-neutral-500 text-xs underline underline-offset-4 transition-colors duration-150 ease-out hover:text-neutral-900 focus-visible:outline-none focus-visible:text-neutral-900"
      >
        ← Volver al catálogo
      </RouterLink>
      <p class="font-mono text-xs text-neutral-400 text-center uppercase tracking-[0.12em]">
        Registro · Biblioteca · 2026
      </p>
    </footer>
  </div>
</template>
