import { defineConfig } from 'vite'
import path from 'node:path'
import uniPlugin from '@dcloudio/vite-plugin-uni'

const uni = typeof uniPlugin === 'function' ? uniPlugin : uniPlugin.default

export default defineConfig({
  base: '/',
  plugins: [uni()],
  resolve: {
    alias: {
      // 让 wrapper 里的导入不包含 ../，避免构建时 chunk 命名异常
      '@pages': path.resolve(__dirname, 'pages')
    }
  },
  server: {
    port: 5173,
    host: true,
    strictPort: false,
    proxy: {
      '/api': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true
      },
      '/admin': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/admin/, '')
      }
    }
  }
})
