import type { MenuUpdateRequest, PlatformMenu } from '@iaf/domain-types';

export const flattenMenus = (menus: PlatformMenu[]): PlatformMenu[] =>
  menus.flatMap((menu) => [menu, ...flattenMenus(menu.children ?? [])]);

export const expandableMenuIds = (menus: PlatformMenu[]): number[] =>
  flattenMenus(menus)
    .filter((menu) => Boolean(menu.children?.length))
    .map((menu) => menu.id);

export const descendantIdsOf = (menu: PlatformMenu | null): Set<number> => {
  const ids = new Set<number>();
  const collect = (children: PlatformMenu[] = []) => {
    children.forEach((child) => {
      ids.add(child.id);
      collect(child.children ?? []);
    });
  };
  collect(menu?.children ?? []);
  return ids;
};

export const toMenuUpdateRequest = (menu: PlatformMenu): MenuUpdateRequest => ({
  parentId: menu.parentId,
  menuCode: menu.menuCode,
  menuType: menu.menuType,
  titleKey: menu.titleKey,
  routePath: menu.routePath,
  componentKey: menu.componentKey,
  icon: menu.icon,
  sortNo: menu.sortNo,
  visible: menu.visible,
  enabled: menu.enabled
});

export interface MenuStructureUpdate {
  id: number;
  values: MenuUpdateRequest;
}

const normalizeSiblings = (siblings: PlatformMenu[], parentId: number | null): MenuStructureUpdate[] =>
  siblings.map((menu, index) => ({
    id: menu.id,
    values: {
      ...toMenuUpdateRequest(menu),
      parentId,
      sortNo: (index + 1) * 10
    }
  }));

export const buildSiblingMoveUpdates = (
  menus: PlatformMenu[],
  menuId: number,
  direction: -1 | 1
): MenuStructureUpdate[] => {
  const menu = flattenMenus(menus).find((item) => item.id === menuId);
  if (!menu) return [];
  const siblings = (menu.parentId == null
    ? menus
    : flattenMenus(menus).find((item) => item.id === menu.parentId)?.children ?? []
  ).slice();
  const currentIndex = siblings.findIndex((item) => item.id === menuId);
  const targetIndex = currentIndex + direction;
  if (currentIndex < 0 || targetIndex < 0 || targetIndex >= siblings.length) return [];
  [siblings[currentIndex], siblings[targetIndex]] = [siblings[targetIndex], siblings[currentIndex]];
  return normalizeSiblings(siblings, menu.parentId);
};

export const buildGroupMoveUpdates = (
  menus: PlatformMenu[],
  menuId: number,
  nextParentId: number | null
): MenuStructureUpdate[] => {
  const allMenus = flattenMenus(menus);
  const menu = allMenus.find((item) => item.id === menuId);
  if (!menu || menu.parentId === nextParentId) return [];
  if (nextParentId === menu.id || descendantIdsOf(menu).has(nextParentId ?? -1)) return [];

  const sourceSiblings = (menu.parentId == null
    ? menus
    : allMenus.find((item) => item.id === menu.parentId)?.children ?? []
  ).filter((item) => item.id !== menuId);
  const targetSiblings = (nextParentId == null
    ? menus
    : allMenus.find((item) => item.id === nextParentId)?.children ?? []
  ).filter((item) => item.id !== menuId);

  return [
    ...normalizeSiblings(sourceSiblings, menu.parentId),
    ...normalizeSiblings([...targetSiblings, menu], nextParentId)
  ];
};
