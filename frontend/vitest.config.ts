import { defineConfig } from 'vitest/config';
import { fileURLToPath, URL } from 'node:url';

const fromFrontendRoot = (path: string) => fileURLToPath(new URL(path, import.meta.url));

export default defineConfig({
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
  test: {
    environment: 'jsdom',
    setupFiles: './apps/pc-admin/src/test/setup.ts',
    include: ['apps/**/*.test.{ts,tsx}', 'packages/**/*.test.{ts,tsx}'],
    exclude: ['**/node_modules/**', '**/dist/**']
  }
});
