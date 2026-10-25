<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()
const email = ref('')
const password = ref('')
const error = ref('')

async function handleSubmit(e) {
  e.preventDefault()
  error.value = ''
  try {
    const res = await fetch('http://localhost:8080/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email: email.value.trim(), password: password.value })
    })
    if (res.ok) {
      const data = await res.json()
      localStorage.setItem('usuario_sgbu', JSON.stringify(data))

      if (data && data.rol === 'BIBLIOTECARIO') {
        router.push('/dashboard')
        return
      }

      const redirect = route.query.redirect
      if (redirect) {
        router.push(redirect.toString())
        return
      }

      router.push('/')
      return
    }
    const txt = await res.text()
    error.value = txt || 'Credenciales inválidas'
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
        Iniciar sesión
      </h1>
      <p class="text-sm text-neutral-500 mb-8">
        Accede con tus datos para consultar el catálogo y solicitar préstamos.
      </p>

      <form class="space-y-5" @submit="handleSubmit">
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
            autocomplete="current-password"
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
          Iniciar sesión
        </button>
      </form>

      <p class="mt-6 text-xs text-neutral-500">
        ¿No tienes una cuenta?
        <RouterLink
          to="/register"
          class="text-neutral-500 text-xs underline underline-offset-4 transition-colors duration-150 ease-out hover:text-neutral-900 focus-visible:outline-none focus-visible:text-neutral-900"
        >
          Crear cuenta
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
        Acceso · Biblioteca · 2026
      </p>
    </footer>
  </div>
</template>
