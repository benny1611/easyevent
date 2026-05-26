import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import svgr from 'vite-plugin-svgr'
import basicSsl from '@vitejs/plugin-basic-ssl'

export default defineConfig(({ command }) => {
  return {
    plugins: [
      react(),
      svgr(),
      // Dynamically load basicSsl only for local development testing
      ...(command === 'serve' ? [basicSsl()] : [])
    ],
    server: {
      host: '0.0.0.0',
      port: 5173
    }
  }
})