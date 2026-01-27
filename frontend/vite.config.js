import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false, // 로컬 개발 시 SSL 문제 방지
        // rewrite: (path) => path.replace(/^\/api/, ''), // 절대 주석 해제 금지 (백엔드 경로에 /api 있음)
      },
    },
  },
})
