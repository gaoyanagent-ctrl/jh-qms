import type { MenuCreateRequest, MenuUpdateRequest, PlatformMenu } from '@iaf/domain-types';

export type { MenuCreateRequest, MenuUpdateRequest, PlatformMenu };

export interface MenuFormValues {
  parentId?: number | null;
  menuCode: string;
  menuType: string;
  titleKey: string;
  routePath?: string | null;
  componentKey?: string | null;
  icon?: string | null;
  sortNo?: number;
  visible: boolean;
  enabled: boolean;
}
