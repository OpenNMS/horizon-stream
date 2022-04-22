import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import svgLoader from 'vite-svg-loader'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import featherResolver from './auto-feather-resolver'

export default defineConfig({
  resolve: {
    alias: {
      '@/': new URL('./src/', import.meta.url).pathname,
      '~@featherds': '@featherds'
    },
    dedupe: ['vue']
  },
  plugins: [
    vue(),
    svgLoader(),
    AutoImport({
      imports: ['vue', 'vue-router', '@vueuse/core'],
      eslintrc: {
        enabled: true,
        filepath: './.eslintrc-auto-import.json'
      }
    }),
    Components({
      resolvers: [featherResolver]
    })
  ],
  define: {
    'process.env': process.env
  },
  test: {
    globals: true,
    environment: 'happy-dom'
  }
})
