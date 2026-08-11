import type { MockResponse } from './MockResponse';

export interface MockRequest {
  method: string;
  url: string;
  path: string;
  pathParams: Record<string, string>;
  queryParams: Record<string, string>;
  headers: Record<string, string>;
  body: any;
}

export type MockHandler = (req: MockRequest) => MockResponse | Promise<MockResponse>;

interface RegisteredRoute {
  method: string;
  pattern: string;
  regex: RegExp;
  paramNames: string[];
  handler: MockHandler;
}

export class MockRouteRegistry {
  private routes: RegisteredRoute[] = [];

  register(method: string, pattern: string, handler: MockHandler) {
    const paramNames: string[] = [];
    const normalizedPattern = pattern.startsWith('/') ? pattern : `/${pattern}`;
    
    // Replace URL params e.g. :id with ([^/]+) and capture param name
    const regexSource = normalizedPattern
      .replace(/:([a-zA-Z0-9_]+)/g, (_, paramName) => {
        paramNames.push(paramName);
        return '([^/]+)';
      });
      
    const regex = new RegExp(`^${regexSource}$`);
    
    this.routes.push({
      method: method.toUpperCase(),
      pattern: normalizedPattern,
      regex,
      paramNames,
      handler
    });
  }

  match(method: string, path: string): { handler: MockHandler; pathParams: Record<string, string> } | null {
    const upperMethod = method.toUpperCase();
    const normalizedPath = path.split('?')[0];
    
    for (const route of this.routes) {
      if (route.method !== upperMethod) continue;
      const match = normalizedPath.match(route.regex);
      if (match) {
        const pathParams: Record<string, string> = {};
        route.paramNames.forEach((name, index) => {
          pathParams[name] = decodeURIComponent(match[index + 1]);
        });
        return { handler: route.handler, pathParams };
      }
    }
    return null;
  }
}
