// @vitest-environment jsdom
import { beforeEach, describe, expect, it } from 'vitest';
import { renderHook, act } from '@testing-library/react';
import { useListViewPreference } from './useListViewPreference';
import type { ColumnPreference } from './UserListViewPreference';

describe('useListViewPreference', () => {
  const tableId = 'test-table';
  const defaultCols: ColumnPreference[] = [
    { key: 'col1', visible: true },
    { key: 'col2', visible: false }
  ];

  beforeEach(() => {
    localStorage.clear();
  });

  it('loads default columns when localStorage is empty', () => {
    const { result } = renderHook(() => useListViewPreference(tableId, defaultCols));
    expect(result.current.pref.columns).toEqual(defaultCols);
  });

  it('saves and updates column preferences', () => {
    const { result } = renderHook(() => useListViewPreference(tableId, defaultCols));

    act(() => {
      result.current.updateColumns([
        { key: 'col1', visible: false },
        { key: 'col2', visible: true }
      ]);
    });

    expect(result.current.pref.columns).toEqual([
      { key: 'col1', visible: false },
      { key: 'col2', visible: true }
    ]);

    const stored = localStorage.getItem(`iaf.table.pref.${tableId}`);
    expect(stored).not.toBeNull();
    expect(JSON.parse(stored!).columns).toEqual([
      { key: 'col1', visible: false },
      { key: 'col2', visible: true }
    ]);
  });

  it('merges new default columns into saved preferences', () => {
    localStorage.setItem(
      `iaf.table.pref.${tableId}`,
      JSON.stringify({
        columns: [{ key: 'col1', visible: true }],
        savedQueries: []
      })
    );

    const newDefaultCols: ColumnPreference[] = [
      { key: 'col1', visible: true },
      { key: 'col3', visible: true }
    ];

    const { result } = renderHook(() => useListViewPreference(tableId, newDefaultCols));

    expect(result.current.pref.columns).toHaveLength(2);
    expect(result.current.pref.columns.some((c) => c.key === 'col3')).toBe(true);
  });
});
