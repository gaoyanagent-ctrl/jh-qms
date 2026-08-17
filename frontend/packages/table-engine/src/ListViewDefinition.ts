import type { ReactNode } from 'react';

export interface ColumnDefinition<T> {
  key: string;
  dataIndex?: keyof T | string;
  titleKey: string; // Translation key
  render?: (value: any, record: T, index: number) => ReactNode;
  sorter?: boolean;
  width?: number;
  fixed?: 'left' | 'right';
  defaultVisible?: boolean;
}

export interface SearchFieldDefinition {
  key: string;
  labelKey: string;
  type: 'text' | 'number' | 'select';
  options?: { labelKey: string; value: any }[];
  placeholderKey?: string;
}

export interface RowActionDefinition<T> {
  key: string;
  labelKey: string;
  requirePermission?: string;
  hidden?: (record: T) => boolean;
  disabled?: (record: T) => boolean;
  onClick: (record: T) => void;
  confirmTitleKey?: string; // If set, wraps with ConfirmAction/Popconfirm
}

export interface ToolbarActionDefinition {
  key: string;
  labelKey: string;
  type: 'primary' | 'default';
  requirePermission?: string;
  onClick: () => void;
}

export interface ListViewDefinition<T> {
  id: string; // Unique table identifier for preferences
  descriptionKey?: string;
  columns: ColumnDefinition<T>[];
  searchFields?: SearchFieldDefinition[];
  rowActions?: RowActionDefinition<T>[];
  toolbarActions?: ToolbarActionDefinition[];
}
