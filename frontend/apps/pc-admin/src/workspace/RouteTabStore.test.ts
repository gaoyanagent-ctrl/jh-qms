import { beforeEach, describe, expect, it } from 'vitest';
import { useRouteTabStore } from './RouteTabStore';

describe('RouteTabStore', () => {
  beforeEach(() => {
    useRouteTabStore.getState().clearTabs();
  });

  it('has initial default workbench tab', () => {
    const state = useRouteTabStore.getState();
    expect(state.tabs).toHaveLength(1);
    expect(state.tabs[0].key).toBe('/');
    expect(state.activeTabKey).toBe('/');
  });

  it('adds a tab and sets it active', () => {
    const store = useRouteTabStore.getState();
    store.addTab({ key: '/platform/users', label: 'menu.users', closable: true });

    const state = useRouteTabStore.getState();
    expect(state.tabs).toHaveLength(2);
    expect(state.tabs[1].key).toBe('/platform/users');
    expect(state.activeTabKey).toBe('/platform/users');
  });

  it('removes a tab and updates active index', () => {
    const store = useRouteTabStore.getState();
    store.addTab({ key: '/platform/users', label: 'menu.users', closable: true });
    store.addTab({ key: '/platform/orgs', label: 'menu.orgs', closable: true });

    // Active key is /platform/orgs, now remove it
    useRouteTabStore.getState().removeTab('/platform/orgs');

    const state = useRouteTabStore.getState();
    expect(state.tabs).toHaveLength(2); // '/' and '/platform/users'
    expect(state.activeTabKey).toBe('/platform/users');
  });

  it('pins a tab correctly', () => {
    const store = useRouteTabStore.getState();
    store.addTab({ key: '/platform/users', label: 'menu.users', closable: true });
    
    useRouteTabStore.getState().pinTab('/platform/users', true);

    const state = useRouteTabStore.getState();
    expect(state.tabs[1].fixed).toBe(true);
    expect(state.tabs[1].closable).toBe(false);
  });
});
