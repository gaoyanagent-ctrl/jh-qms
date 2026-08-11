import type { PlatformOrg, PlatformPermission } from '@iaf/domain-types';
import { Checkbox, Tree } from 'antd';
import type { DataNode } from 'antd/es/tree';

export const toOrgTreeData = (items: PlatformOrg[]): DataNode[] =>
  items.map((item) => ({
    key: item.id,
    title: item.orgName,
    children: toOrgTreeData(item.children ?? [])
  }));

export const OrgTreeView = ({ items, onSelect }: { items: PlatformOrg[]; onSelect?: (id: number) => void }) => (
  <Tree
    blockNode
    defaultExpandAll
    treeData={toOrgTreeData(items)}
    onSelect={(keys) => {
      const [id] = keys;
      if (typeof id === 'number') {
        onSelect?.(id);
      }
    }}
  />
);

export const PermissionChecklist = ({
  options,
  value,
  translate,
  onChange
}: {
  options: PlatformPermission[];
  value: string[];
  translate: (key: string) => string;
  onChange: (value: string[]) => void;
}) => (
  <Checkbox.Group
    value={value}
    options={options.map((permission) => ({
      label: `${translate(permission.nameKey)} (${permission.code})`,
      value: permission.code
    }))}
    onChange={(nextValue) => onChange(nextValue.map(String))}
  />
);
