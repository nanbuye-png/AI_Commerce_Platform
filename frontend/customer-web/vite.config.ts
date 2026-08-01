import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@shared': path.resolve(__dirname, '../shared'),
    },
  },
  server: {
    // 开发端口（与后端 CORS allowed-origins 默认配置一致）
    // 注意：Vite 开发服务器默认已启用 SPA history fallback，
    // 直接访问 /products、/search 等深层路由会返回 index.html，不会 404。
    port: 5173,
  },
  preview: {
    // `npm run preview` 预览生产构建（默认同样支持 SPA fallback）
    port: 5173,
  },
})