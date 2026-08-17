import React, { useMemo } from 'react';
import { App, Badge, Button, Card, Segmented, Space, Table, Tag, Typography, theme } from 'antd';
import { FilterOutlined, ReloadOutlined } from '@ant-design/icons';
import { useTranslation } from 'react-i18next';
import { PermissionButton, PermissionGate } from '@iaf/permissions';
import { useIafTheme } from '@iaf/theme';
import { AppPageContainer, ConfirmAction } from '@iaf/ui-core';
import { SearchPanelRenderer } from './SearchPanelRenderer';
import { ColumnSettings } from './ColumnSettings';
import type { ListViewDefinition } from './ListViewDefinition';
import { useListViewPreference } from './useListViewPreference';
import type { ColumnPreference } from './UserListViewPreference';

interface ConfigurableListPageProps<T> {
  definition: ListViewDefinition<T>;
  loading: boolean;
  dataSource: T[];
  total: number;
  pageNo: number;
  pageSize: number;
  onPageChange: (page: number, size: number) => void;
  onSearch: (query: Record<string, any>) => void;
  onReset: () => void;
  onRefresh?: () => void;
  selectedRowKeys?: React.Key[];
  onSelectedRowKeysChange?: (keys: React.Key[]) => void;
  notice?: React.ReactNode;
}

export function ConfigurableListPage<T extends { id: string | number }>({
  definition,
  loading,
  dataSource,
  total,
  pageNo,
  pageSize,
  onPageChange,
  onSearch,
  onReset,
  onRefresh,
  selectedRowKeys: controlledSelectedRowKeys,
  onSelectedRowKeysChange,
  notice
}: ConfigurableListPageProps<T>) {
  const { t } = useTranslation();
  const { token } = theme.useToken();
  const { message } = App.useApp();
  const { density } = useIafTheme();
  const [internalSelectedRowKeys, setInternalSelectedRowKeys] = React.useState<React.Key[]>([]);
  const selectedRowKeys = controlledSelectedRowKeys ?? internalSelectedRowKeys;
  const setSelectedRowKeys = onSelectedRowKeysChange ?? setInternalSelectedRowKeys;
  const [activeQuery, setActiveQuery] = React.useState<Record<string, any>>({});
  const tableSize = density === 'comfortable' ? 'middle' : 'small';

  // Create default preferences
  const defaultPrefs: ColumnPreference[] = useMemo(() => {
    return definition.columns.map((col) => ({
      key: col.key,
      visible: col.defaultVisible !== false,
      width: col.width,
      fixed: col.fixed
    }));
  }, [definition.columns]);

  const { pref, updateColumns } = useListViewPreference(definition.id, defaultPrefs);

  const titleKeys = useMemo(() => {
    const keys: Record<string, string> = {};
    definition.columns.forEach((col) => {
      keys[col.key] = col.titleKey;
    });
    return keys;
  }, [definition.columns]);

  // Compute table columns based on preferences
  const tableColumns = useMemo(() => {
    const prefMap = new Map(pref.columns.map((c) => [c.key, c]));

    const filtered = definition.columns.filter((col) => {
      const p = prefMap.get(col.key);
      return p ? p.visible : col.defaultVisible !== false;
    });

    const ordered = [...filtered].sort((a, b) => {
      const idxA = pref.columns.findIndex((c) => c.key === a.key);
      const idxB = pref.columns.findIndex((c) => c.key === b.key);
      return idxA - idxB;
    });

    const antdCols: any[] = ordered.map((col) => ({
      key: col.key,
      dataIndex: col.dataIndex as string,
      title: t(col.titleKey),
      render: col.render,
      sorter: col.sorter,
      width: col.width,
      fixed: col.fixed
    }));

    if (definition.rowActions && definition.rowActions.length > 0) {
      antdCols.push({
        key: 'actions',
        dataIndex: 'actions',
        title: t('common.fields.actions'),
        width: 180,
        fixed: 'right' as any,
        render: (_: any, record: T) => (
          <Space>
            {definition.rowActions!.map((action) => {
              if (action.hidden?.(record)) return null;
              const label = t(action.labelKey);
              const disabled = action.disabled?.(record);
              const onClick = () => action.onClick(record);

              const btn = (
                <Button type="link" size="small" disabled={disabled} onClick={onClick}>
                  {label}
                </Button>
              );

              const actionContent = action.confirmTitleKey ? (
                <ConfirmAction key={action.key} title={t(action.confirmTitleKey)} onConfirm={onClick}>
                  <Button type="link" size="small" disabled={disabled}>
                    {label}
                  </Button>
                </ConfirmAction>
              ) : (
                <React.Fragment key={action.key}>{btn}</React.Fragment>
              );

              if (action.requirePermission) {
                return (
                  <PermissionGate key={action.key} require={action.requirePermission}>
                    {actionContent}
                  </PermissionGate>
                );
              }

              return actionContent;
            })}
          </Space>
        )
      });
    }

    return antdCols as any[];
  }, [definition.columns, definition.rowActions, pref.columns, t]);

  const activeFilterCount = Object.keys(activeQuery).length;
  const visibleColumnCount = pref.columns.filter((column) => column.visible).length;

  const toolbarExtra = (
    <Space wrap>
      {definition.toolbarActions?.map((action) => {
        const btn = (
          <Button key={action.key} type={action.type} danger={action.danger} disabled={action.disabled} loading={action.loading} onClick={action.onClick}>
            {t(action.labelKey)}
          </Button>
        );

        if (action.requirePermission) {
          return (
            <PermissionButton
              key={action.key}
              type={action.type}
              danger={action.danger}
              disabled={action.disabled}
              loading={action.loading}
              require={action.requirePermission}
              onClick={action.onClick}
            >
              {t(action.labelKey)}
            </PermissionButton>
          );
        }

        return btn;
      })}
      <Segmented
        size="small"
        value={density}
        options={[
          { label: t('settings.densities.compact'), value: 'compact' },
          { label: t('settings.densities.standard'), value: 'standard' },
          { label: t('settings.densities.comfortable'), value: 'comfortable' }
        ]}
        disabled
      />
      <Button
        aria-label={t('common.actions.refresh')}
        icon={<ReloadOutlined />}
        onClick={() => {
          if (onRefresh) {
            onRefresh();
            message.success(t('common.feedback.operationSucceeded'));
          }
        }}
      >
        {t('common.actions.refresh')}
      </Button>
      <ColumnSettings columns={pref.columns} titleKeys={titleKeys} onChange={updateColumns} />
    </Space>
  );

  return (
    <AppPageContainer
      title={t(`${definition.id}.title`) || definition.id}
      extra={toolbarExtra}
    >
      <Card
        variant="borderless"
        styles={{ body: { padding: token.padding } }}
        style={{ border: `1px solid ${token.colorBorderSecondary}`, boxShadow: token.boxShadowTertiary }}
      >
        <Space align="center" style={{ justifyContent: 'space-between', width: '100%', marginBottom: token.marginSM }}>
          <Space size={8} wrap>
            <Badge status={loading ? 'processing' : 'success'} text={loading ? t('common.feedback.loading') : t('workspace.listReady')} />
            <Tag bordered={false}>{t('workspace.totalRecords', { total })}</Tag>
            <Tag bordered={false}>{t('workspace.visibleColumns', { count: visibleColumnCount })}</Tag>
            {selectedRowKeys.length > 0 && <Tag color="processing">{t('workspace.selectedRows', { count: selectedRowKeys.length })}</Tag>}
            {activeFilterCount > 0 && (
              <Tag icon={<FilterOutlined />} color="blue">
                {t('workspace.activeFilters', { count: activeFilterCount })}
              </Tag>
            )}
          </Space>
          <Typography.Text type="secondary">{definition.descriptionKey ? t(definition.descriptionKey) : t('workspace.standardListView')}</Typography.Text>
        </Space>
        {notice}
        {definition.searchFields && definition.searchFields.length > 0 && (
          <SearchPanelRenderer
            fields={definition.searchFields}
            onSearch={(query) => {
              setActiveQuery(query);
              onSearch(query);
            }}
            onReset={() => {
              setActiveQuery({});
              onReset();
            }}
          />
        )}
        <Table
          size={tableSize}
          bordered
          sticky
          rowKey="id"
          loading={loading}
          dataSource={dataSource}
          columns={tableColumns}
          rowSelection={{
            selectedRowKeys,
            onChange: setSelectedRowKeys,
            preserveSelectedRowKeys: true
          }}
          scroll={{ x: 'max-content' }}
          locale={{ emptyText: t('common.feedback.empty') }}
          pagination={{
            current: pageNo,
            total: total,
            pageSize: pageSize,
            showSizeChanger: true,
            showTotal: (nextTotal) => t('workspace.totalRecords', { total: nextTotal }),
            onChange: (p, s) => onPageChange(p, s)
          }}
        />
      </Card>
    </AppPageContainer>
  );
}
