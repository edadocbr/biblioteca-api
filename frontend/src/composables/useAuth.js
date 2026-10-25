import { ref } from 'vue'

// Sesiones independientes (multi-guard): administrador y lector.
function createSession(userKey, tokenKey, rolEsperado) {
  const user = ref(null)

  function load() {
    try {
      const raw = localStorage.getItem(userKey)
      const parsed = raw ? JSON.parse(raw) : null
      user.value = parsed && parsed.rol === rolEsperado ? parsed : null
    } catch {
      user.value = null
    }
    return user.value
  }

  function login(data) {
    localStorage.setItem(userKey, JSON.stringify(data))
    if (data && data.token) localStorage.setItem(tokenKey, data.token)
    user.value = data
  }

  function logout() {
    localStorage.removeItem(userKey)
    localStorage.removeItem(tokenKey)
    user.value = null
  }

  load()
  return { user, load, login, logout, isAuthenticated: () => !!load() }
}

const adminSession = createSession('sgbu_admin_user', 'sgbu_admin_token', 'BIBLIOTECARIO')
const lectorSession = createSession('sgbu_lector_user', 'sgbu_lector_token', 'LECTOR')

export function useAdminAuth() {
  return adminSession
}

export function useLectorAuth() {
  return lectorSession
}
