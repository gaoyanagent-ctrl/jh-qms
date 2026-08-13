import type { PlatformMenu } from '@iaf/domain-types';
import { PermissionButton, PermissionGate, PLATFORM_PERMISSIONS } from '@iaf/permissions';
import { AppPageContainer, FormInteractionSurface, IafSurface, IafToolbar, StatusTag } from '@iaf/ui-core';
import { iafSurfaceWidths, useIafTheme } from '@iaf/theme';
import { ArrowDownOutlined, ArrowUpOutlined, MenuFoldOutlined, MenuUnfoldOutlined } from '@ant-design/icons';
import { Button, Form, Input, InputNumber, Select, Space, Table, Tag, Tooltip, message } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useMemo, useState } from 'react';
import type { Key } from 'react';
import { useTranslation } from 'react-i18next';
import { useCreateMenuMutation, useMenusTreeQuery, useUpdateMenuMutation, useUpdateMenuStructureMutation } from './hooks';
import {
  buildGroupMoveUpdates,
  buildSiblingMoveUpdates,
  descendantIdsOf,
  expandableMenuIds,
  flattenMenus
} from './menuTree';
import type { MenuFormValues } from './types';

const toFormValues = (menu: PlatformMenu): MenuFormValues => ({
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

export const PlatformMenuConsolePage = () => {
  const { t } = useTranslation();
  const { formInteractionMode, surfaceWidth } = useIafTheme();
  const [form] = Form.useForm<MenuFormValues>();
  const [createOpen, setCreateOpen] = useState(false);
  const [editing, setEditing] = useState<PlatformMenu | null>(null);
  const [expandedRowKeys, setExpandedRowKeys] = useState<Key[]>([]);
  const menusQuery = useMenusTreeQuery();
  const menuTree = useMemo(() => menusQuery.data ?? [], [menusQuery.data]);
  const flatMenus = useMemo(() => flattenMenus(menuTree), [menuTree]);
  const groupMenus = useMemo(() => flatMenus.filter((menu) => menu.menuType === 'GROUP'), [flatMenus]);
  const excludedParentIds = useMemo(() => {
    if (!editing) return new Set<number>();
    const ids = descendantIdsOf(editing);
    ids.add(editing.id);
    return ids;
  }, [editing]);

  const createMutation = useCreateMenuMutation({
    onSuccess: () => {
      message.success(t('common.feedback.operationSucceeded'));
      setCreateOpen(false);
      form.resetFields();
    }
  });
  const updateMutation = useUpdateMenuMutation({
    onSuccess: () => {
      message.success(t('common.feedback.operationSucceeded'));
      setEditing(null);
      form.resetFields();
    }
  });
  const structureMutation = useUpdateMenuStructureMutation({
    onSuccess: () => message.success(t('platformConfig.menuStructureUpdated'))
  });

  const updateStructure = async (updates: ReturnType<typeof buildSiblingMoveUpdates>) => {
    if (updates.length > 0) await structureMutation.mutateAsync(updates);
  };

  const siblingsOf = (menu: PlatformMenu) =>
    menu.parentId == null
      ? menuTree
      : flatMenus.find((item) => item.id === menu.parentId)?.children ?? [];

  const openCreate = () => {
    form.setFieldsValue({
      parentId: null,
      menuType: 'MENU',
      visible: true,
      enabled: true,
      sortNo: 100
    });
    setCreateOpen(true);
  };

  const openEdit = (menu: PlatformMenu) => {
    setEditing(menu);
    form.setFieldsValue(toFormValues(menu));
  };

  const submitForm = async (values: MenuFormValues) => {
    const request = {
      ...values,
      parentId: values.parentId ?? null,
      routePath: values.routePath || null,
      componentKey: values.componentKey || null,
      icon: values.icon || null,
      sortNo: values.sortNo ?? 0
    };
    if (editing) {
      await updateMutation.mutateAsync({ id: editing.id, values: request });
      return;
    }
    await createMutation.mutateAsync(request);
  };

  const columns: ColumnsType<PlatformMenu> = [
    {
      title: t('platformConfig.menuTitle'),
      dataIndex: 'titleKey',
      render: (titleKey: string, record) => (
        <Space>
          <span>{t(titleKey)}</span>
          <Tag>{record.menuCode}</Tag>
        </Space>
      )
    },
    { title: t('platformConfig.routePath'), dataIndex: 'routePath' },
    { title: t('platformConfig.componentKey'), dataIndex: 'componentKey' },
    {
      title: t('platformConfig.permissionCode'),
      dataIndex: 'permissionCodes',
      render: (codes: string[]) => codes?.map((code) => <Tag key={code}>{code}</Tag>)
    },
    {
      title: t('platformConfig.menuGroup'),
      width: 220,
      render: (_, record) => {
        const currentGroup = flatMenus.find((menu) => menu.id === record.parentId);
        const excludedIds = descendantIdsOf(record);
        excludedIds.add(record.id);
        return (
          <PermissionGate require={PLATFORM_PERMISSIONS.menuUpdate} fallback={<span>{currentGroup ? t(currentGroup.titleKey) : t('platformConfig.rootGroup')}</span>}>
            <Select
              aria-label={t('platformConfig.changeMenuGroup', { menu: t(record.titleKey) })}
              value={record.parentId ?? 'root'}
              disabled={structureMutation.isPending}
              style={{ width: '100%' }}
              onChange={(value) => updateStructure(buildGroupMoveUpdates(menuTree, record.id, value === 'root' ? null : Number(value)))}
              options={[
                { value: 'root', label: t('platformConfig.rootGroup') },
                ...groupMenus
                  .filter((menu) => !excludedIds.has(menu.id))
                  .map((menu) => ({ value: menu.id, label: t(menu.titleKey) }))
              ]}
            />
          </PermissionGate>
        );
      }
    },
    {
      title: t('common.fields.status'),
      render: (_, record) => (
        <Space>
          <StatusTag status={record.enabled ? 'ACTIVE' : 'DISABLED'} label={t(`common.status.${record.enabled ? 'ACTIVE' : 'DISABLED'}`)} />
          {!record.visible && <Tag>{t('platformConfig.hiddenMenu')}</Tag>}
        </Space>
      )
    },
    {
      title: t('common.actions.actions'),
      width: 200,
      render: (_, record) => {
        const siblings = siblingsOf(record);
        const siblingIndex = siblings.findIndex((menu) => menu.id === record.id);
        return (
          <Space size={4}>
            <PermissionGate require={PLATFORM_PERMISSIONS.menuUpdate}>
              <Tooltip title={t('platformConfig.moveMenuUp')}>
                <Button
                  aria-label={t('platformConfig.moveMenuUp')}
                  icon={<ArrowUpOutlined />}
                  size="small"
                  disabled={siblingIndex <= 0 || structureMutation.isPending}
                  onClick={() => updateStructure(buildSiblingMoveUpdates(menuTree, record.id, -1))}
                />
              </Tooltip>
              <Tooltip title={t('platformConfig.moveMenuDown')}>
                <Button
                  aria-label={t('platformConfig.moveMenuDown')}
                  icon={<ArrowDownOutlined />}
                  size="small"
                  disabled={siblingIndex < 0 || siblingIndex >= siblings.length - 1 || structureMutation.isPending}
                  onClick={() => updateStructure(buildSiblingMoveUpdates(menuTree, record.id, 1))}
                />
              </Tooltip>
            </PermissionGate>
            <PermissionButton require={PLATFORM_PERMISSIONS.menuUpdate} size="small" onClick={() => openEdit(record)}>
              {t('common.actions.edit')}
            </PermissionButton>
          </Space>
        );
      }
    }
  ];

  const surfaceOpen = createOpen || Boolean(editing);

  return (
    <AppPageContainer title={t('platformConfig.menuConsole')}>
      <IafSurface>
        <IafToolbar title={t('platformConfig.menuSummary')}>
          <Space>
            <Tag>{t('workspace.totalRecords', { total: flatMenus.length })}</Tag>
            <Button
              icon={<MenuUnfoldOutlined />}
              onClick={() => setExpandedRowKeys(expandableMenuIds(menuTree))}
            >
              {t('platformConfig.expandAllMenus')}
            </Button>
            <Button icon={<MenuFoldOutlined />} onClick={() => setExpandedRowKeys([])}>
              {t('platformConfig.collapseAllMenus')}
            </Button>
            <Button onClick={() => menusQuery.refetch()}>{t('common.actions.refresh')}</Button>
            <PermissionButton require={PLATFORM_PERMISSIONS.menuCreate} type="primary" onClick={openCreate}>
              {t('common.actions.create')}
            </PermissionButton>
          </Space>
        </IafToolbar>
        <Table
          rowKey="id"
          size="small"
          bordered
          loading={menusQuery.isLoading}
          columns={columns}
          dataSource={menuTree}
          pagination={false}
          expandable={{
            expandedRowKeys,
            onExpandedRowsChange: (keys) => setExpandedRowKeys([...keys])
          }}
        />
      </IafSurface>

      <FormInteractionSurface
        mode={formInteractionMode}
        open={surfaceOpen}
        title={editing ? `${t('common.actions.edit')} · ${editing.menuCode}` : `${t('common.actions.create')} · ${t('platformConfig.menuConsole')}`}
        onCancel={() => {
          setCreateOpen(false);
          setEditing(null);
          form.resetFields();
        }}
        onSubmit={() => form.submit()}
        confirmLoading={createMutation.isPending || updateMutation.isPending}
        submitLabel={t('common.actions.save')}
        cancelLabel={t('common.actions.cancel')}
        width={iafSurfaceWidths[surfaceWidth]}
      >
        <Form<MenuFormValues> layout="vertical" form={form} onFinish={submitForm}>
          <Form.Item name="parentId" label={t('platformConfig.parentMenu')}>
            <Select
              allowClear
              options={flatMenus
                .filter((menu) => !excludedParentIds.has(menu.id))
                .map((menu) => ({ value: menu.id, label: `${t(menu.titleKey)} · ${menu.menuCode}` }))}
            />
          </Form.Item>
          <Form.Item name="menuCode" label={t('platformConfig.menuCode')} rules={[{ required: true, message: t('auth.required') }]}>
            <Input />
          </Form.Item>
          <Form.Item name="titleKey" label={t('platformConfig.titleKey')} rules={[{ required: true, message: t('auth.required') }]}>
            <Input />
          </Form.Item>
          <Form.Item name="menuType" label={t('platformConfig.menuType')} rules={[{ required: true, message: t('auth.required') }]}>
            <Select
              options={['GROUP', 'MENU', 'BUTTON'].map((value) => ({ value, label: value }))}
            />
          </Form.Item>
          <Form.Item name="routePath" label={t('platformConfig.routePath')}>
            <Input />
          </Form.Item>
          <Form.Item name="componentKey" label={t('platformConfig.componentKey')}>
            <Input />
          </Form.Item>
          <Form.Item name="icon" label={t('platformConfig.icon')}>
            <Input />
          </Form.Item>
          <Form.Item name="sortNo" label={t('platformConfig.sortNo')}>
            <InputNumber min={0} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="visible" label={t('platformConfig.visible')} rules={[{ required: true, message: t('auth.required') }]}>
            <Select
              options={[
                { value: true, label: t('common.yes') },
                { value: false, label: t('common.no') }
              ]}
            />
          </Form.Item>
          <Form.Item name="enabled" label={t('platformConfig.enabled')} rules={[{ required: true, message: t('auth.required') }]}>
            <Select
              options={[
                { value: true, label: t('common.yes') },
                { value: false, label: t('common.no') }
              ]}
            />
          </Form.Item>
        </Form>
      </FormInteractionSurface>
    </AppPageContainer>
  );
};
