import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';

export interface RouteTab {
  key: string; // URL path + query
  label: string; // Tab title key (e.g. 'menu.users')
  closable: boolean;
  fixed?: boolean;
}

interface RouteTabState {
  tabs: RouteTab[];
  activeTabKey: string;
  addTab: (tab: RouteTab) => void;
  removeTab: (key: string) => void;
  removeOtherTabs: (key: string) => void;
  removeRightTabs: (key: string) => void;
  setActiveTabKey: (key: string) => void;
  updateTabLabel: (key: string, label: string) => void;
  pinTab: (key: string, fixed: boolean) => void;
  clearTabs: () => void;
}

export const useRouteTabStore = create<RouteTabState>()(
  persist(
    (set, get) => ({
      tabs: [
        { key: '/', label: 'menu.workbench', closable: false, fixed: true }
      ],
      activeTabKey: '/',

      addTab: (tab) => {
        const { tabs } = get();
        const existing = tabs.find((t) => t.key === tab.key);
        if (!existing) {
          set({ tabs: [...tabs, tab], activeTabKey: tab.key });
        } else if (existing.label !== tab.label) {
          set({
            tabs: tabs.map((item) => (item.key === tab.key ? { ...item, label: tab.label } : item)),
            activeTabKey: tab.key
          });
        } else {
          set({ activeTabKey: tab.key });
        }
      },

      removeTab: (key) => {
        const { tabs, activeTabKey } = get();
        const tabIndex = tabs.findIndex((t) => t.key === key);
        if (tabIndex === -1) return;

        const newTabs = tabs.filter((t) => t.key !== key);
        let newActiveKey = activeTabKey;

        if (activeTabKey === key) {
          const nextActiveTab = newTabs[tabIndex] || newTabs[tabIndex - 1];
          newActiveKey = nextActiveTab ? nextActiveTab.key : '/';
        }

        set({ tabs: newTabs, activeTabKey: newActiveKey });
      },

      removeOtherTabs: (key) => {
        const { tabs } = get();
        const newTabs = tabs.filter((t) => !t.closable || t.key === key);
        set({ tabs: newTabs, activeTabKey: key });
      },

      removeRightTabs: (key) => {
        const { tabs, activeTabKey } = get();
        const index = tabs.findIndex((t) => t.key === key);
        if (index === -1) return;

        const leftTabs = tabs.slice(0, index + 1);
        const rightTabs = tabs.slice(index + 1);
        const newTabs = [...leftTabs, ...rightTabs.filter((t) => !t.closable)];

        let newActiveKey = activeTabKey;
        if (rightTabs.some((t) => t.key === activeTabKey)) {
          newActiveKey = key;
        }

        set({ tabs: newTabs, activeTabKey: newActiveKey });
      },

      setActiveTabKey: (key) => set({ activeTabKey: key }),

      updateTabLabel: (key, label) => {
        const { tabs } = get();
        const newTabs = tabs.map((t) => (t.key === key ? { ...t, label } : t));
        set({ tabs: newTabs });
      },

      pinTab: (key, fixed) => {
        const { tabs } = get();
        const newTabs = tabs.map((t) =>
          t.key === key ? { ...t, fixed, closable: !fixed } : t
        );
        set({ tabs: newTabs });
      },

      clearTabs: () => {
        set({
          tabs: [
            { key: '/', label: 'menu.workbench', closable: false, fixed: true }
          ],
          activeTabKey: '/'
        });
      }
    }),
    {
      name: 'iaf.workspace.tabs',
      storage: createJSONStorage(() => localStorage)
    }
  )
);
