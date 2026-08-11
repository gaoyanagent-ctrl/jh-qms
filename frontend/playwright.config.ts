import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './e2e',
  timeout: 30_000,
  expect: {
    timeout: 5_000
  },
  use: {
    baseURL: 'http://127.0.0.1:5175',
    trace: 'on-first-retry'
  },
  webServer: {
    command: 'node ../../node_modules/vite/bin/vite.js --host 127.0.0.1 --port 5175',
    cwd: './apps/pc-admin',
    url: 'http://127.0.0.1:5175',
    reuseExistingServer: false,
    env: {
      ...process.env,
      VITE_IAF_MOCK_API: 'true'
    }
  },
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] }
    }
  ]
});
