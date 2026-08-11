import { createApiClient, MockApiAdapter } from '@iaf/api-client';
import { logout, useAuthStore } from '@iaf/auth';
import { registerMocks } from '@iaf/mock-data';

const mockAdapter = new MockApiAdapter();
const isMockApiEnabled = import.meta.env.VITE_IAF_MOCK_API === 'true';

if (isMockApiEnabled) {
  registerMocks(mockAdapter);
}

export const apiClient = createApiClient({
  baseUrl: import.meta.env.VITE_IAF_API_BASE_URL ?? '',
  getToken: () => useAuthStore.getState().token,
  onUnauthorized: logout,
  mockAdapter: isMockApiEnabled ? mockAdapter : undefined
});
