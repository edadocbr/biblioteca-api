// Cliente HTTP centralizado. Mantiene la API de fetch (devuelve Response).
// VITE_API_BASE_URL por defecto es '/api' (ruta relativa al origen que sirve la app).
const rawBase = import.meta.env.VITE_API_BASE_URL
export const API_BASE_URL = (rawBase && rawBase.trim() ? rawBase.trim() : '/api').replace(/\/+$/, '')

export function apiFetch(path, options) {
  const normalized = path.startsWith('/') ? path : `/${path}`
  return fetch(`${API_BASE_URL}${normalized}`, options)
}
