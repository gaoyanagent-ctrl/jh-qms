import type { PlatformMenu, PlatformPermission, PlatformRole } from '@iaf/domain-types';
import { PLATFORM_PERMISSION_OPTIONS, PLATFORM_PERMISSIONS } from '@iaf/permissions';
import { iafSurfaceWidths, useIafTheme } from '@iaf/theme';
import { PermissionChecklist } from '@iaf/ui-business';
import { FormInteractionSurface, StatusTag } from '@iaf/ui-core';
import { ConfigurableListPage, type ListViewDefinition } from '@iaf/table-engine';
import { Checkbox, Form, Input, Modal, Select, Space, Tag, message } from 'antd';
import { useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useMenusTreeQuery } from '../menus/hooks';
import {
  useRolesQuery,
  useCreateRoleMutation,
  useUpdateRoleMutation,
  useAssignPermissionsMutation,
  useAssignMenusMutation,
  usePermissionsQuery
} from './hooks';
import type { RoleFormValues } from './types';

const flattenMenus = (menus: PlatformMenu[]): PlatformMenu[] =>
  menus.flatMap((menu) => [menu, ...flattenMenus(menu.children ?? [])]);

const toPermissionOption = (permission: PlatformPermission) => {
  const code = permission.permissionCode ?? permission.code ?? '';
  const staticOption = PLATFORM_PERMISSION_OPTIONS.find((option) => option.code === code);
  if (staticOption) return staticOption;
  return {
    code,
    nameKey: permission.permissionName ?? permission.nameKey ?? code,
    groupKey: permission.groupKey ?? `permissions.groups.${permission.moduleCode ?? 'platform'}`
  };
};

export const RoleListPage = () => {
  const { t } = useTranslation();
  const { formInteractionMode, surfaceWidth } = useIafTheme();
  const [keyword, setKeyword] = useState('');
  const [pageNo, setPageNo] = useState(1);
  const [editing, setEditing] = useState<PlatformRole | null>(null);
  const [createOpen, setCreateOpen] = useState(false);
  const [assignTarget, setAssignTarget] = useState<PlatformRole | null>(null);
  const [assignMenuTarget, setAssignMenuTarget] = useState<PlatformRole | null>(null);
  const [permissionCodes, setPermissionCodes] = useState<string[]>([]);
  const [menuCodes, setMenuCodes] = useState<string[]>([]);
  const [form] = Form.useForm<RoleFormValues>();

  // Custom Hooks
  const rolesQuery = useRolesQuery({ keyword, pageNo, pageSize: 10 });
  const permissionsQuery = usePermissionsQuery();
  const menusQuery = useMenusTreeQuery();
  const permissionOptions = useMemo(
    () => permissionsQuery.data?.map(toPermissionOption).filter((item) => item.code) ?? PLATFORM_PERMISSION_OPTIONS,
    [permissionsQuery.data]
  );
  const menuOptions = useMemo(() => flattenMenus(menusQuery.data ?? []), [menusQuery.data]);

  const createMutation = useCreateRoleMutation({
    onSuccess: () => {
      message.success(t('common.feedback.operationSucceeded'));
      setCreateOpen(false);
      form.resetFields();
    }
  });

  const updateMutation = useUpdateRoleMutation({
    onSuccess: () => {
      message.success(t('common.feedback.operationSucceeded'));
      setEditing(null);
      form.resetFields();
    }
  });

  const assignMutation = useAssignPermissionsMutation({
    onSuccess: () => {
      message.success(t('common.feedback.operationSucceeded'));
      setAssignTarget(null);
      setPermissionCodes([]);
    }
  });

  const assignMenusMutation = useAssignMenusMutation({
    onSuccess: () => {
      message.success(t('common.feedback.operationSucceeded'));
      setAssignMenuTarget(null);
      setMenuCodes([]);
    }
  });

  const openCreate = () => {
    form.setFieldsValue({ roleType: 'PLATFORM', status: 'ACTIVE' });
    setCreateOpen(true);
  };

  const openEdit = (record: PlatformRole) => {
    setEditing(record);
    form.setFieldsValue({
      roleCode: record.roleCode,
      roleName: record.roleName,
      roleType: record.roleType,
      status: record.status
    });
  };

  const submitForm = async (values: RoleFormValues) => {
    if (editing) {
      await updateMutation.mutateAsync({ id: editing.id, values });
      return;
    }
    await createMutation.mutateAsync(values);
  };

  const definition: ListViewDefinition<PlatformRole> = {
    id: 'roles',
    columns: [
      { key: 'roleCode', dataIndex: 'roleCode', titleKey: 'roles.roleCode', sorter: true },
      { key: 'roleName', dataIndex: 'roleName', titleKey: 'roles.roleName', sorter: true },
      { key: 'roleType', dataIndex: 'roleType', titleKey: 'roles.roleType' },
      {
        key: 'status',
        dataIndex: 'status',
        titleKey: 'common.fields.status',
        render: (status: PlatformRole['status']) => (
          <StatusTag status={status} label={t(`common.status.${status}`)} />
        )
      }
    ],
    searchFields: [
      { key: 'keyword', labelKey: 'common.actions.search', type: 'text', placeholderKey: 'common.actions.search' }
    ],
    toolbarActions: [
      {
        key: 'create',
        labelKey: 'common.actions.create',
        type: 'primary',
        requirePermission: PLATFORM_PERMISSIONS.roleCreate,
        onClick: openCreate
      }
    ],
    rowActions: [
      {
        key: 'edit',
        labelKey: 'common.actions.edit',
        requirePermission: PLATFORM_PERMISSIONS.roleUpdate,
        onClick: (record) => openEdit(record)
      },
      {
        key: 'assignPermissions',
        labelKey: 'roles.assignPermissions',
        requirePermission: PLATFORM_PERMISSIONS.roleAssignPermission,
        onClick: (record) => {
          setAssignTarget(record);
          setPermissionCodes(record.permissions);
        }
      },
      {
        key: 'assignMenus',
        labelKey: 'roles.assignMenus',
        requirePermission: PLATFORM_PERMISSIONS.roleAssignMenu,
        onClick: (record) => {
          setAssignMenuTarget(record);
          setMenuCodes(record.menuCodes ?? []);
        }
      }
    ]
  };

  const surfaceOpen = createOpen || Boolean(editing);
  const usePageSurface = formInteractionMode === 'page' && surfaceOpen;

  return (
    <>
      {!usePageSurface && (
        <ConfigurableListPage<PlatformRole>
          definition={definition}
          loading={rolesQuery.isLoading}
          dataSource={rolesQuery.data?.records ?? []}
          total={rolesQuery.data?.total ?? 0}
          pageNo={pageNo}
          pageSize={10}
          onPageChange={setPageNo}
          onSearch={(query) => {
            setKeyword(query.keyword || '');
            setPageNo(1);
          }}
          onReset={() => {
            setKeyword('');
            setPageNo(1);
          }}
          onRefresh={() => rolesQuery.refetch()}
        />
      )}

      <FormInteractionSurface
        mode={formInteractionMode}
        open={surfaceOpen}
        title={editing ? `${t('common.actions.edit')} · ${editing.roleCode}` : `${t('common.actions.create')} · ${t('roles.title')}`}
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
        <Form<RoleFormValues> layout="vertical" form={form} onFinish={submitForm}>
          <Form.Item name="roleCode" label={t('roles.roleCode')} rules={[{ required: true, message: t('auth.required') }]}>
            <Input />
          </Form.Item>
          <Form.Item name="roleName" label={t('roles.roleName')} rules={[{ required: true, message: t('auth.required') }]}>
            <Input />
          </Form.Item>
          <Form.Item name="roleType" label={t('roles.roleType')} rules={[{ required: true, message: t('auth.required') }]}>
            <Input />
          </Form.Item>
          <Form.Item name="status" label={t('common.fields.status')} rules={[{ required: true, message: t('auth.required') }]}>
            <Select
              options={(['ACTIVE', 'DISABLED'] as const).map((value) => ({
                value,
                label: t(`common.status.${value}`)
              }))}
            />
          </Form.Item>
        </Form>
      </FormInteractionSurface>

      <Modal
        width={720}
        open={Boolean(assignTarget)}
        title={t('roles.assignPermissions')}
        onCancel={() => {
          setAssignTarget(null);
          setPermissionCodes([]);
        }}
        onOk={() => assignTarget && assignMutation.mutate({ id: assignTarget.id, values: permissionCodes })}
        confirmLoading={assignMutation.isPending}
      >
        <PermissionChecklist
          options={permissionOptions}
          value={permissionCodes}
          translate={t}
          onChange={setPermissionCodes}
        />
      </Modal>

      <Modal
        width={720}
        open={Boolean(assignMenuTarget)}
        title={t('roles.assignMenus')}
        onCancel={() => {
          setAssignMenuTarget(null);
          setMenuCodes([]);
        }}
        onOk={() => assignMenuTarget && assignMenusMutation.mutate({ id: assignMenuTarget.id, values: menuCodes })}
        confirmLoading={assignMenusMutation.isPending}
      >
        <Checkbox.Group value={menuCodes} onChange={(values) => setMenuCodes(values.map(String))} style={{ width: '100%' }}>
          <Space direction="vertical" style={{ width: '100%' }}>
            {menuOptions.map((menu) => (
              <Checkbox key={menu.menuCode} value={menu.menuCode}>
                <Space>
                  <span>{t(menu.titleKey)}</span>
                  <Tag>{menu.menuCode}</Tag>
                  {menu.routePath && <Tag color="blue">{menu.routePath}</Tag>}
                </Space>
              </Checkbox>
            ))}
          </Space>
        </Checkbox.Group>
      </Modal>
    </>
  );
};
