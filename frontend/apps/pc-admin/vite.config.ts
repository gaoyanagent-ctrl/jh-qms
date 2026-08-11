import react from '@vitejs/plugin-react';
import { defineConfig } from 'vite';
import { fileURLToPath, URL } from 'node:url';

const apiProxyTarget = process.env.VITE_IAF_API_PROXY_TARGET ?? 'http://localhost:8080';
const fromFrontendRoot = (path: string) => fileURLToPath(new URL(`../../${path}`, import.meta.url));

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@iaf/api-client': fromFrontendRoot('packages/api-client/src'),
      '@iaf/auth': fromFrontendRoot('packages/auth/src'),
      '@iaf/domain-types': fromFrontendRoot('packages/domain-types/src'),
      '@iaf/form-engine': fromFrontendRoot('packages/form-engine/src'),
      '@iaf/i18n': fromFrontendRoot('packages/i18n/src'),
      '@iaf/mock-data': fromFrontendRoot('packages/mock-data/src/register.ts'),
      '@iaf/permissions': fromFrontendRoot('packages/permissions/src'),
      '@iaf/table-engine': fromFrontendRoot('packages/table-engine/src'),
      '@iaf/theme': fromFrontendRoot('packages/theme/src'),
      '@iaf/ui-business': fromFrontendRoot('packages/ui-business/src'),
      '@iaf/ui-core': fromFrontendRoot('packages/ui-core/src')
    }
  },
  server: {
    port: 5173,
    // Allow access via tunneled hostnames (iaf.naturedao.tech etc.)
    allowedHosts: true,
    proxy: {
      '/api': {
        target: apiProxyTarget,
        changeOrigin: true
      }
    }
  },
  test: {
    environment: 'jsdom',
    setupFiles: './src/test/setup.ts'
  }
});
