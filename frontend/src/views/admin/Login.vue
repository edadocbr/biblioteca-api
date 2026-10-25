<script setup>
import { ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'

const router = useRouter()

const username = ref('')
const password = ref('')
const errorMessage = ref('')

async function handleLogin(e) {
  e.preventDefault()
  errorMessage.value = ''

  try {
    const res = await fetch('http://localhost:8080/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        email: username.value.trim(),
        password: password.value
      })
    })

    if (res.ok) {
      const data = await res.json()
      if (data && data.rol === 'BIBLIOTECARIO') {
        localStorage.setItem('usuario_sgbu', JSON.stringify(data))
        router.push('/dashboard')
        return
      }
      errorMessage.value = 'Acceso denegado: este panel es exclusivo para bibliotecarios.'
      return
    }

    errorMessage.value = 'Credenciales inválidas o usuario inactivo'
  } catch (err) {
    errorMessage.value = 'Credenciales inválidas o usuario inactivo'
  }
}
</script>

<template>
  <div class="min-h-screen flex items-center justify-center p-6 bg-neutral-50">
    <div class="w-full max-w-sm bg-white border border-neutral-200 p-8">
      <header class="mb-6">
        <p class="text-[11px] font-mono uppercase tracking-widest text-neutral-400 mb-1">
          Consola / Biblioteca
        </p>
        <h1 class="text-lg font-semibold tracking-tight text-neutral-900">
          Acceso administrativo
        </h1>
        <p class="mt-1 text-xs text-neutral-500">
          Panel de Control y Gestión
        </p>
      </header>

      <div
        v-if="errorMessage"
        role="alert"
        aria-live="polite"
        class="border border-red-200 bg-red-50 text-red-700 text-xs font-mono p-2.5 mb-4"
      >
        {{ errorMessage }}
      </div>

      <form class="space-y-4" @submit="handleLogin">
        <div>
          <label
            for="username"
            class="text-xs font-mono uppercase text-neutral-600 mb-1.5 block"
          >
            Correo electrónico / Usuario
          </label>
          <input
            id="username"
            v-model="username"
            type="text"
            autocomplete="username"
            autocapitalize="none"
            spellcheck="false"
            class="border border-neutral-200 focus:border-neutral-900 outline-none px-3 py-2 text-sm text-neutral-900 bg-white rounded-none transition-colors duration-150 w-full"
          />
        </div>

        <div>
          <label
            for="password"
            class="text-xs font-mono uppercase text-neutral-600 mb-1.5 block"
          >
            Contraseña / Access Key
          </label>
          <input
            id="password"
            v-model="password"
            type="password"
            autocomplete="current-password"
            class="border border-neutral-200 focus:border-neutral-900 outline-none px-3 py-2 text-sm text-neutral-900 bg-white rounded-none transition-colors duration-150 w-full"
          />
        </div>

        <button
          type="submit"
          class="w-full bg-neutral-900 text-white py-2 text-xs font-mono uppercase tracking-wider font-medium rounded-none hover:bg-neutral-800 transition-colors duration-150 focus-visible:outline focus-visible:outline-1 focus-visible:outline-offset-2 focus-visible:outline-neutral-900 active:scale-[0.99]"
        >
          Acceder al panel
        </button>
      </form>

      <RouterLink
        to="/"
        class="text-xs font-mono text-neutral-400 hover:text-neutral-700 mt-6 block text-center transition-colors duration-150 focus-visible:outline focus-visible:outline-1 focus-visible:outline-offset-4 focus-visible:outline-neutral-900"
      >
        ← Volver al catálogo público
      </RouterLink>
    </div>
  </div>
</template>
