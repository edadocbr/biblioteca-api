import vue from '@vitejs/plugin-vue'
import tailwindcss from '@tailwindcss/vite'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue(), tailwindcss()],
  server: {
    // En desarrollo, /api se redirige al backend para usar rutas relativas
    proxy: {
      '/api': process.env.DEV_PROXY_TARGET || 'http://localhost:8080'
    }
  }
})
