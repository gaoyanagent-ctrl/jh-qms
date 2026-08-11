import { useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import { create } from 'zustand';

interface DirtyState {
  dirtyTabs: Record<string, boolean>; // tabKey -> isDirty
  setTabDirty: (key: string, isDirty: boolean) => void;
  clearTabDirty: (key: string) => void;
}

export const useDirtyStateStore = create<DirtyState>((set) => ({
  dirtyTabs: {},
  setTabDirty: (key, isDirty) =>
    set((state) => ({
      dirtyTabs: { ...state.dirtyTabs, [key]: isDirty }
    })),
  clearTabDirty: (key) =>
    set((state) => {
      const newDirty = { ...state.dirtyTabs };
      delete newDirty[key];
      return { dirtyTabs: newDirty };
    })
}));

export const useRegisterDirty = (isDirty: boolean) => {
  const location = useLocation();
  const tabKey = location.pathname + location.search;
  const setTabDirty = useDirtyStateStore((state) => state.setTabDirty);
  const clearTabDirty = useDirtyStateStore((state) => state.clearTabDirty);

  useEffect(() => {
    setTabDirty(tabKey, isDirty);
    return () => {
      clearTabDirty(tabKey);
    };
  }, [tabKey, isDirty, setTabDirty, clearTabDirty]);
};
