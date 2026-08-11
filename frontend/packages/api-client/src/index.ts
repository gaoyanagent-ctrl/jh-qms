import type { Result } from '@iaf/domain-types';
import { MockApiAdapter } from './mock/MockApiAdapter';

export class ApiError extends Error {
  readonly code: string;
  readonly status: number;

  constructor(message: string, code: string, status: number) {
    super(message);
    this.name = 'ApiError';
    this.code = code;
    this.status = status;
  }
}

export interface ApiClientOptions {
  baseUrl?: string;
  getToken?: () => string | null;
  onUnauthorized?: () => void;
  mockAdapter?: MockApiAdapter;
}

export interface RequestOptions extends Omit<RequestInit, 'body'> {
  body?: unknown;
  query?: Record<string, string | number | boolean | null | undefined>;
}

const trimTrailingSlash = (value: string) => value.replace(/\/$/, '');

export class ApiClient {
  private readonly baseUrl: string;
  private readonly getToken?: () => string | null;
  private readonly onUnauthorized?: () => void;
  private readonly mockAdapter?: MockApiAdapter;

  constructor(options: ApiClientOptions = {}) {
    this.baseUrl = trimTrailingSlash(options.baseUrl ?? '');
    this.getToken = options.getToken;
    this.onUnauthorized = options.onUnauthorized;
    this.mockAdapter = options.mockAdapter;
  }

  async get<T>(path: string, options: RequestOptions = {}): Promise<T> {
    return this.request<T>(path, { ...options, method: 'GET' });
  }

  async post<T>(path: string, body?: unknown, options: RequestOptions = {}): Promise<T> {
    return this.request<T>(path, { ...options, method: 'POST', body });
  }

  async put<T>(path: string, body?: unknown, options: RequestOptions = {}): Promise<T> {
    return this.request<T>(path, { ...options, method: 'PUT', body });
  }

  async patch<T>(path: string, body?: unknown, options: RequestOptions = {}): Promise<T> {
    return this.request<T>(path, { ...options, method: 'PATCH', body });
  }

  async request<T>(path: string, options: RequestOptions = {}): Promise<T> {
    const method = options.method ?? 'GET';
    const headers = new Headers(options.headers);
    headers.set('Accept', 'application/json');

    if (options.body !== undefined) {
      headers.set('Content-Type', 'application/json');
    }

    const token = this.getToken?.();
    if (token) {
      headers.set('Authorization', `Bearer ${token}`);
    }

    if (this.mockAdapter) {
      const mockHeaders: Record<string, string> = {};
      headers.forEach((value, key) => {
        mockHeaders[key] = value;
      });

      const mockRes = await this.mockAdapter.handleRequest(
        method,
        path,
        options.body,
        options.query,
        mockHeaders
      );
      if (mockRes) {
        const payload = mockRes.data;
        if (mockRes.status === 401) {
          this.onUnauthorized?.();
        }
        if (mockRes.status >= 400 || !payload.success) {
          throw new ApiError(payload.message || 'Mock Error', payload.code || 'MOCK_ERROR', mockRes.status);
        }
        return payload.data;
      }
    }

    const url = this.toUrl(path, options.query);

    const response = await fetch(url, {
      ...options,
      headers,
      body: options.body === undefined ? undefined : JSON.stringify(options.body)
    });

    const payload = await this.readPayload<T>(response);

    if (response.status === 401) {
      this.onUnauthorized?.();
    }

    if (!response.ok || !payload.success) {
      throw new ApiError(payload.message || response.statusText, payload.code || 'HTTP_ERROR', response.status);
    }

    return payload.data;
  }

  private toUrl(path: string, query?: RequestOptions['query']): string {
    const normalizedPath = path.startsWith('/') ? path : `/${path}`;
    const url = new URL(`${this.baseUrl}${normalizedPath}`, window.location.origin);

    Object.entries(query ?? {}).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        url.searchParams.set(key, String(value));
      }
    });

    return url.toString();
  }

  private async readPayload<T>(response: Response): Promise<Result<T>> {
    const contentType = response.headers.get('content-type') ?? '';
    if (!contentType.includes('application/json')) {
      return {
        success: response.ok,
        code: response.ok ? 'OK' : `HTTP_${response.status}`,
        message: response.statusText,
        data: undefined as T
      };
    }

    return (await response.json()) as Result<T>;
  }
}

export const createApiClient = (options: ApiClientOptions = {}) => new ApiClient(options);

export * from './mock/MockResponse';
export * from './mock/MockRouteRegistry';
export * from './mock/MockApiAdapter';
