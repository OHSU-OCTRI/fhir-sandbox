/// <reference types="vitest/config" />
import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import vuetify from 'vite-plugin-vuetify';

// https://vite.dev/config/
export default defineConfig({
  base: '',
  build: {
    manifest: true,
    outDir: 'target/classes/static',
    rollupOptions: {
      input: [
        'src/main/resources/frontend/launch-modal.ts',
        'src/main/resources/frontend/managed-content.js'
      ],
      output: {
        entryFileNames: 'assets/js/[name]-[hash].js',
        chunkFileNames: 'assets/js/[name]-[hash].js',
        assetFileNames: assetInfo => {
          if (assetInfo.name && assetInfo.name.endsWith('.css')) {
            return 'assets/css/[name]-[hash].[ext]';
          }
          return 'assets/[name]-[hash].[ext]';
        }
      }
    }
  },
  define: {
    __VUE_PROD_DEVTOOLS__: true
  },
  plugins: [vue(), vuetify({ autoImport: true })],
  test: {
    environment: 'jsdom',
    exclude: ['node_modules/*', 'target/*']
  }
});
