import '@testing-library/jest-dom/vitest';
import { initIafI18n } from '@iaf/i18n';
import { cleanup } from '@testing-library/react';
import { afterEach, beforeAll, beforeEach } from 'vitest';
import { useAuthStore } from '@iaf/auth';

beforeAll(async () => {
  await initIafI18n('zh-CN');
});

beforeEach(() => {
  window.localStorage.clear();
  useAuthStore.setState({ token: null, principal: null });
});

afterEach(() => {
  cleanup();
});

Object.defineProperty(window, 'matchMedia', {
  writable: true,
  value: (query: string) => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: () => undefined,
    removeListener: () => undefined,
    addEventListener: () => undefined,
    removeEventListener: () => undefined,
    dispatchEvent: () => false
  })
});

Object.defineProperty(window, 'getComputedStyle', {
  value: () => ({
    getPropertyValue: () => ''
  })
});
