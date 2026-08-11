import { describe, expect, it } from 'vitest';
import { MockRouteRegistry } from './MockRouteRegistry';
import { mockSuccess } from './MockResponse';

describe('MockRouteRegistry', () => {
  it('registers and matches routes with path parameters', () => {
    const registry = new MockRouteRegistry();
    const handler = () => mockSuccess({ matched: true });
    
    registry.register('GET', '/api/platform/users/:id', handler);
    registry.register('POST', '/api/platform/users', handler);

    // Test exact match
    const match1 = registry.match('GET', '/api/platform/users/123');
    expect(match1).not.toBeNull();
    expect(match1!.pathParams).toEqual({ id: '123' });
    expect(match1!.handler).toBe(handler);

    // Test match on POST
    const match2 = registry.match('POST', '/api/platform/users');
    expect(match2).not.toBeNull();
    expect(match2!.pathParams).toEqual({});

    // Test unmatched paths
    const match3 = registry.match('GET', '/api/platform/users');
    expect(match3).toBeNull();

    const match4 = registry.match('GET', '/api/platform/orgs');
    expect(match4).toBeNull();
  });
});
