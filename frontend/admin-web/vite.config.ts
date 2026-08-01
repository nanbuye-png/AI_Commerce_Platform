import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    // 管理端固定端口（与后端 CORS allowed-origins 对齐）
    port: 5175,
  },
  preview: {
    port: 5175,
  },
})