import type { PlatformMenu } from '@iaf/domain-types';
import { PermissionButton, PLATFORM_PERMISSIONS } from '@iaf/permissions';
import { AppPageContainer, FormInteractionSurface, IafSurface, IafToolbar, StatusTag } from '@iaf/ui-core';
import { iafSurfaceWidths, useIafTheme } from '@iaf/theme';
import { Button, Form, Input, InputNumber, Select, Space, Table, Tag, message } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useCreateMenuMutation, useMenusTreeQuery, useUpdateMenuMutation } from './hooks';
import type { MenuFormValues } from './types';

const flattenMenus = (menus: PlatformMenu[]): PlatformMenu[] =>
  menus.flatMap((menu) => [menu, ...flattenMenus(menu.children ?? [])]);

const descendantIdsOf = (menu: PlatformMenu | null): Set<number> => {
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
  const menusQuery = useMenusTreeQuery();
  const flatMenus = useMemo(() => flattenMenus(menusQuery.data ?? []), [menusQuery.data]);
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
      width: 120,
      render: (_, record) => (
        <PermissionButton require={PLATFORM_PERMISSIONS.menuUpdate} size="small" onClick={() => openEdit(record)}>
          {t('common.actions.edit')}
        </PermissionButton>
      )
    }
  ];

  const surfaceOpen = createOpen || Boolean(editing);

  return (
    <AppPageContainer title={t('platformConfig.menuConsole')}>
      <IafSurface>
        <IafToolbar title={t('platformConfig.menuSummary')}>
          <Space>
            <Tag>{t('workspace.totalRecords', { total: flatMenus.length })}</Tag>
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
          dataSource={menusQuery.data ?? []}
          pagination={false}
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
