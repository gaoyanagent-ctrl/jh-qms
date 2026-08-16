import type { PlatformOrg, PlatformUser } from '@iaf/domain-types';
import { PLATFORM_PERMISSIONS } from '@iaf/permissions';
import { iafSurfaceWidths, useIafTheme } from '@iaf/theme';
import { FormInteractionSurface, StatusTag } from '@iaf/ui-core';
import { ConfigurableListPage, type ListViewDefinition } from '@iaf/table-engine';
import { Form, Input, Modal, Select, Space, Tag, message } from 'antd';
import { useEffect, useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useRegisterDirty } from '../../../workspace/DirtyStateRegistry';
import { useOrgTreeQuery } from '../orgs/hooks';
import {
  useUsersQuery,
  useCreateUserMutation,
  useUpdateUserMutation,
  useDisableUserMutation,
  useResetPasswordMutation,
  useUserOrganizationsQuery,
  useAssignUserOrganizationsMutation,
  useUserRolesQuery,
  useAssignUserRolesMutation
} from './hooks';
import { useRolesQuery } from '../roles/hooks';
import type { UserFormValues, UserOrgFormValues, UserRoleFormValues } from './types';

const flattenOrgTree = (items: PlatformOrg[]): PlatformOrg[] =>
  items.flatMap((item) => [item, ...flattenOrgTree(item.children ?? [])]);

export const UserListPage = () => {
  const { t } = useTranslation();
  const { formInteractionMode, surfaceWidth } = useIafTheme();
  const [keyword, setKeyword] = useState('');
  const [pageNo, setPageNo] = useState(1);
  const [editing, setEditing] = useState<PlatformUser | null>(null);
  const [createOpen, setCreateOpen] = useState(false);
  const [resetTarget, setResetTarget] = useState<PlatformUser | null>(null);
  const [orgTarget, setOrgTarget] = useState<PlatformUser | null>(null);
  const [roleTarget, setRoleTarget] = useState<PlatformUser | null>(null);
  const [form] = Form.useForm<UserFormValues>();
  const [resetForm] = Form.useForm<{ newPassword: string }>();
  const [orgForm] = Form.useForm<UserOrgFormValues>();
  const [roleForm] = Form.useForm<UserRoleFormValues>();
  const selectedOrgIds = Form.useWatch('orgIds', orgForm) ?? [];

  // Register dirty state when create/edit form is open
  useRegisterDirty(createOpen || Boolean(editing));

  // Custom Hooks
  const usersQuery = useUsersQuery({ keyword, pageNo, pageSize: 10 });
  const orgTreeQuery = useOrgTreeQuery();
  const userOrganizationsQuery = useUserOrganizationsQuery(orgTarget?.id);
  const userRolesQuery = useUserRolesQuery(roleTarget?.id);
  const rolesQuery = useRolesQuery({ pageNo: 1, pageSize: 200 });
  const orgOptions = useMemo(() => {
    return flattenOrgTree(orgTreeQuery.data ?? []).map((org) => ({
      label: `${org.orgName} (${org.orgCode})`,
      value: org.id
    }));
  }, [orgTreeQuery.data]);

  const createMutation = useCreateUserMutation({
    onSuccess: () => {
      message.success(t('common.feedback.operationSucceeded'));
      setCreateOpen(false);
      form.resetFields();
    }
  });

  const updateMutation = useUpdateUserMutation({
    onSuccess: () => {
      message.success(t('common.feedback.operationSucceeded'));
      setEditing(null);
      form.resetFields();
    }
  });

  const disableMutation = useDisableUserMutation({
    onSuccess: () => {
      message.success(t('common.feedback.operationSucceeded'));
    }
  });

  const resetMutation = useResetPasswordMutation({
    onSuccess: () => {
      message.success(t('common.feedback.operationSucceeded'));
      setResetTarget(null);
      resetForm.resetFields();
    }
  });

  const assignOrgMutation = useAssignUserOrganizationsMutation({
    onSuccess: () => {
      message.success(t('common.feedback.operationSucceeded'));
      setOrgTarget(null);
      orgForm.resetFields();
    }
  });
  const assignRoleMutation = useAssignUserRolesMutation({
    onSuccess: () => {
      message.success(t('common.feedback.operationSucceeded'));
      setRoleTarget(null);
      roleForm.resetFields();
    }
  });

  useEffect(() => {
    if (!orgTarget || !userOrganizationsQuery.data) {
      return;
    }
    orgForm.setFieldsValue({
      orgIds: userOrganizationsQuery.data.organizations.map((item) => item.orgId),
      primaryOrgId: userOrganizationsQuery.data.primaryOrgId ?? undefined
    });
  }, [orgForm, orgTarget, userOrganizationsQuery.data]);

  useEffect(() => {
    if (roleTarget && userRolesQuery.data) {
      roleForm.setFieldsValue({ roleIds: userRolesQuery.data.roleIds });
    }
  }, [roleForm, roleTarget, userRolesQuery.data]);

  const openEdit = (record: PlatformUser) => {
    setEditing(record);
    form.setFieldsValue({
      displayName: record.displayName,
      mobile: record.mobile,
      email: record.email
    });
  };

  const submitForm = async (values: UserFormValues) => {
    if (editing) {
      await updateMutation.mutateAsync({
        id: editing.id,
        values: {
          displayName: values.displayName,
          mobile: values.mobile,
          email: values.email
        }
      });
      return;
    }
    await createMutation.mutateAsync(values);
  };

  const submitOrgForm = async (values: UserOrgFormValues) => {
    if (!orgTarget) {
      return;
    }
    const orgIds = values.orgIds ?? [];
    if (orgIds.length > 0 && !values.primaryOrgId) {
      orgForm.setFields([{ name: 'primaryOrgId', errors: [t('auth.required')] }]);
      return;
    }
    if (values.primaryOrgId && !orgIds.includes(values.primaryOrgId)) {
      orgForm.setFields([{ name: 'primaryOrgId', errors: [t('users.primaryOrgMustBeAssigned')] }]);
      return;
    }
    await assignOrgMutation.mutateAsync({
      id: orgTarget.id,
      values: {
        organizations: orgIds.map((orgId) => ({
          orgId,
          primary: orgId === values.primaryOrgId,
          scopeWeight: orgId === values.primaryOrgId ? 100 : 0
        }))
      }
    });
  };

  const definition: ListViewDefinition<PlatformUser> = {
    id: 'users',
    columns: [
      { key: 'username', dataIndex: 'username', titleKey: 'users.username', sorter: true },
      { key: 'displayName', dataIndex: 'displayName', titleKey: 'users.displayName', sorter: true },
      { key: 'mobile', dataIndex: 'mobile', titleKey: 'users.mobile' },
      { key: 'email', dataIndex: 'email', titleKey: 'users.email' },
      {
        key: 'organizations',
        dataIndex: 'organizations',
        titleKey: 'users.organizations',
        render: (_: unknown, record: PlatformUser) => {
          const organizations = record.organizations ?? [];
          if (organizations.length === 0) {
            return <span>{t('common.feedback.empty')}</span>;
          }
          return (
            <Space size={[4, 4]} wrap>
              {organizations.slice(0, 3).map((org) => (
                <Tag key={org.orgId} color={org.primary ? 'processing' : undefined}>
                  {org.orgName}
                </Tag>
              ))}
              {organizations.length > 3 ? <Tag>+{organizations.length - 3}</Tag> : null}
            </Space>
          );
        }
      },
      {
        key: 'status',
        dataIndex: 'status',
        titleKey: 'common.fields.status',
        render: (status: PlatformUser['status']) => (
          <StatusTag status={status} label={t(`common.status.${status}`)} />
        )
      },
      {
        key: 'createdAt',
        dataIndex: 'createdAt',
        titleKey: 'common.fields.createdAt',
        defaultVisible: false
      },
      {
        key: 'updatedAt',
        dataIndex: 'updatedAt',
        titleKey: 'common.fields.updatedAt',
        defaultVisible: false
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
        requirePermission: PLATFORM_PERMISSIONS.userCreate,
        onClick: () => setCreateOpen(true)
      }
    ],
    rowActions: [
      {
        key: 'edit',
        labelKey: 'common.actions.edit',
        requirePermission: PLATFORM_PERMISSIONS.userUpdate,
        onClick: (record) => openEdit(record)
      },
      {
        key: 'assignOrganizations',
        labelKey: 'users.assignOrganizations',
        requirePermission: PLATFORM_PERMISSIONS.userUpdate,
        onClick: (record) => setOrgTarget(record)
      },
      {
        key: 'assignRoles',
        labelKey: 'users.assignRoles',
        requirePermission: PLATFORM_PERMISSIONS.userUpdate,
        onClick: (record) => setRoleTarget(record)
      },
      {
        key: 'disable',
        labelKey: 'common.actions.disable',
        requirePermission: PLATFORM_PERMISSIONS.userDisable,
        disabled: (record) => record.status === 'DISABLED',
        onClick: (record) => disableMutation.mutate(record.id),
        confirmTitleKey: 'common.feedback.confirmDisable'
      },
      {
        key: 'resetPassword',
        labelKey: 'common.actions.resetPassword',
        requirePermission: PLATFORM_PERMISSIONS.userResetPassword,
        onClick: (record) => setResetTarget(record)
      }
    ]
  };

  const surfaceOpen = createOpen || Boolean(editing);
  const usePageSurface = formInteractionMode === 'page' && surfaceOpen;

  return (
    <>
      {!usePageSurface && (
        <ConfigurableListPage<PlatformUser>
          definition={definition}
          loading={usersQuery.isLoading}
          dataSource={usersQuery.data?.records ?? []}
          total={usersQuery.data?.total ?? 0}
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
          onRefresh={() => usersQuery.refetch()}
        />
      )}

      <FormInteractionSurface
        mode={formInteractionMode}
        open={surfaceOpen}
        title={editing ? `${t('common.actions.edit')} · ${editing.username}` : `${t('common.actions.create')} · ${t('users.title')}`}
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
        <Form<UserFormValues> layout="vertical" form={form} onFinish={submitForm}>
          {editing ? null : (
            <>
              <Form.Item name="username" label={t('users.username')} rules={[{ required: true, message: t('auth.required') }]}>
                <Input />
              </Form.Item>
              <Form.Item name="password" label={t('users.password')} rules={[{ required: true, message: t('auth.required'), min: 8 }]}>
                <Input.Password />
              </Form.Item>
              <Form.Item name="primaryOrgId" label={t('users.primaryOrganization')} rules={[{ required: true, message: t('auth.required') }]}>
                <Select showSearch loading={orgTreeQuery.isLoading} options={orgOptions} optionFilterProp="label" />
              </Form.Item>
            </>
          )}
          <Form.Item name="displayName" label={t('users.displayName')} rules={[{ required: true, message: t('auth.required') }]}>
            <Input />
          </Form.Item>
          <Form.Item name="mobile" label={t('users.mobile')}>
            <Input />
          </Form.Item>
          <Form.Item name="email" label={t('users.email')}>
            <Input />
          </Form.Item>
        </Form>
      </FormInteractionSurface>

      <Modal
        open={Boolean(resetTarget)}
        title={t('common.actions.resetPassword')}
        onCancel={() => {
          setResetTarget(null);
          resetForm.resetFields();
        }}
        onOk={() => resetForm.submit()}
        confirmLoading={resetMutation.isPending}
      >
        <Form<{ newPassword: string }>
          layout="vertical"
          form={resetForm}
          onFinish={(values) => resetTarget && resetMutation.mutate({ id: resetTarget.id, newPassword: values.newPassword })}
        >
          <Form.Item name="newPassword" label={t('users.newPassword')} rules={[{ required: true, message: t('auth.required'), min: 8 }]}>
            <Input.Password />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        open={Boolean(orgTarget)}
        title={orgTarget ? `${t('users.assignOrganizations')} · ${orgTarget.username}` : t('users.assignOrganizations')}
        onCancel={() => {
          setOrgTarget(null);
          orgForm.resetFields();
        }}
        onOk={() => orgForm.submit()}
        confirmLoading={assignOrgMutation.isPending}
      >
        <Form<UserOrgFormValues> layout="vertical" form={orgForm} onFinish={submitOrgForm}>
          <Form.Item name="orgIds" label={t('users.organizations')}>
            <Select
              mode="multiple"
              allowClear
              showSearch
              loading={orgTreeQuery.isLoading || userOrganizationsQuery.isLoading}
              options={orgOptions}
              optionFilterProp="label"
            />
          </Form.Item>
          <Form.Item
            name="primaryOrgId"
            label={t('users.primaryOrganization')}
            dependencies={['orgIds']}
          >
            <Select
              allowClear
              showSearch
              loading={orgTreeQuery.isLoading || userOrganizationsQuery.isLoading}
              options={orgOptions.filter((option) => selectedOrgIds.includes(option.value))}
              optionFilterProp="label"
            />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        open={Boolean(roleTarget)}
        title={roleTarget ? `${t('users.assignRoles')} · ${roleTarget.username}` : t('users.assignRoles')}
        onCancel={() => {
          setRoleTarget(null);
          roleForm.resetFields();
        }}
        onOk={() => roleForm.submit()}
        confirmLoading={assignRoleMutation.isPending}
      >
        <Form<UserRoleFormValues>
          layout="vertical"
          form={roleForm}
          onFinish={(values) => roleTarget && assignRoleMutation.mutate({ id: roleTarget.id, values })}
        >
          <Form.Item name="roleIds" label={t('users.roles')} initialValue={[]}>
            <Select
              mode="multiple"
              allowClear
              showSearch
              loading={rolesQuery.isLoading || userRolesQuery.isLoading}
              options={(rolesQuery.data?.records ?? [])
                .filter((role) => role.status !== 'DISABLED')
                .map((role) => ({ label: `${role.roleName} (${role.roleCode})`, value: role.id }))}
              optionFilterProp="label"
            />
          </Form.Item>
        </Form>
      </Modal>
    </>
  );
};
