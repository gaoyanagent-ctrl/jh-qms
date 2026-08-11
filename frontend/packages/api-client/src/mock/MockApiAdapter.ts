import { MockRouteRegistry } from './MockRouteRegistry';
import type { MockResponse } from './MockResponse';

export class MockApiAdapter {
  readonly registry = new MockRouteRegistry();

  register(method: string, pattern: string, handler: any) {
    this.registry.register(method, pattern, handler);
  }

  async handleRequest(
    method: string,
    path: string,
    body?: any,
    query?: any,
    headers: Record<string, string> = {}
  ): Promise<MockResponse | null> {
    const match = this.registry.match(method, path);
    if (!match) return null;

    const { handler, pathParams } = match;
    const queryParams: Record<string, string> = {};
    if (query) {
      Object.entries(query).forEach(([k, v]) => {
        queryParams[k] = String(v);
      });
    }

    try {
      return await handler({
        method: method.toUpperCase(),
        url: path,
        path: path.split('?')[0],
        pathParams,
        queryParams,
        headers,
        body
      });
    } catch (e: any) {
      return {
        status: 500,
        data: {
          success: false,
          code: 'INTERNAL_SERVER_ERROR',
          message: e.message || 'Internal Server Error',
          data: undefined
        }
      };
    }
  }
}
