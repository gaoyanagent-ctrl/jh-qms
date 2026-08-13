import type { PlatformMenu } from '@iaf/domain-types';
import { describe, expect, it } from 'vitest';
import { buildGroupMoveUpdates, buildSiblingMoveUpdates, expandableMenuIds } from './menuTree';

const menu = (id: number, parentId: number | null, children: PlatformMenu[] = []): PlatformMenu => ({
  id,
  tenantId: 1,
  parentId,
  menuCode: `menu.${id}`,
  menuType: children.length ? 'GROUP' : 'MENU',
  titleKey: `menu.${id}`,
  routePath: null,
  componentKey: null,
  icon: null,
  sortNo: id * 10,
  visible: true,
  enabled: true,
  version: 1,
  permissionCodes: [],
  children
});

const tree = [menu(1, null, [menu(2, 1), menu(3, 1)]), menu(4, null, [])];

describe('menu tree structure planning', () => {
  it('collects only expandable nodes', () => {
    expect(expandableMenuIds(tree)).toEqual([1]);
  });

  it('moves a menu within its siblings and normalizes order', () => {
    const updates = buildSiblingMoveUpdates(tree, 3, -1);
    expect(updates.map((update) => [update.id, update.values.sortNo])).toEqual([[3, 10], [2, 20]]);
    expect(buildSiblingMoveUpdates(tree, 2, -1)).toEqual([]);
  });

  it('moves a menu to another group and prevents descendant cycles', () => {
    const updates = buildGroupMoveUpdates(tree, 4, 1);
    expect(updates.at(-1)).toMatchObject({ id: 4, values: { parentId: 1, sortNo: 30 } });
    expect(buildGroupMoveUpdates(tree, 1, 2)).toEqual([]);
  });
});
