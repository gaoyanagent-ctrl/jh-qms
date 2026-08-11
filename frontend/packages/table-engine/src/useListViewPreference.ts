import { useState } from 'react';
import type { ColumnPreference, UserListViewPreference } from './UserListViewPreference';

const getStorage = () => (typeof window === 'undefined' ? null : window.localStorage);

export const useListViewPreference = (tableId: string, defaultColumns: ColumnPreference[]) => {
  const [pref, setPref] = useState<UserListViewPreference>(() => {
    const storage = getStorage();
    const saved = storage?.getItem(`iaf.table.pref.${tableId}`);
    if (saved) {
      try {
        const parsed = JSON.parse(saved);
        if (parsed && Array.isArray(parsed.columns)) {
          // Merge defaults to handle newly added columns or schema updates
          const mergedColumns = [...parsed.columns];
          defaultColumns.forEach((defCol) => {
            if (!mergedColumns.some((col) => col.key === defCol.key)) {
              mergedColumns.push(defCol);
            }
          });
          return {
            columns: mergedColumns,
            savedQueries: parsed.savedQueries || []
          };
        }
      } catch {
        // ignore
      }
    }
    return {
      columns: defaultColumns,
      savedQueries: []
    };
  });

  const savePref = (newPref: UserListViewPreference) => {
    setPref(newPref);
    getStorage()?.setItem(`iaf.table.pref.${tableId}`, JSON.stringify(newPref));
  };

  const updateColumns = (columns: ColumnPreference[]) => {
    savePref({
      ...pref,
      columns
    });
  };

  return {
    pref,
    updateColumns,
    saveQueries: (savedQueries: UserListViewPreference['savedQueries']) => {
      savePref({ ...pref, savedQueries });
    }
  };
};
