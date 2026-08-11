export interface ColumnPreference {
  key: string;
  visible: boolean;
  width?: number;
  fixed?: 'left' | 'right';
}

export interface UserListViewPreference {
  columns: ColumnPreference[];
  savedQueries: {
    name: string;
    query: Record<string, any>;
  }[];
}
