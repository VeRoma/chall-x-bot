import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    // 1. РАЗРЕШАЕМ NGROK (отключаем проверку хоста)
    allowedHosts: true,

    // 2. ПРОКСИ (чтобы запросы к бэкенду шли без ошибок CORS и https->http)
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
      }
    }
  }
})