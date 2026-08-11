import type { PlatformOrg } from '@iaf/domain-types';
import { PLATFORM_PERMISSIONS, useHasPermission, PermissionButton } from '@iaf/permissions';
import { iafSurfaceWidths, useIafTheme } from '@iaf/theme';
import { OrgTreeView } from '@iaf/ui-business';
import { AppPageContainer, FormInteractionSurface, StatusTag } from '@iaf/ui-core';
import { Button, Form, Input, InputNumber, Select, Space, Table, message } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useOrgTreeQuery, useCreateOrgMutation, useUpdateOrgMutation } from './hooks';
import type { OrgFormValues } from './types';

const flattenOrgs = (items: PlatformOrg[]): PlatformOrg[] =>
  items.flatMap((item) => [item, ...flattenOrgs(item.children ?? [])]);

export const OrgTreePage = () => {
  const { t } = useTranslation();
  const { formInteractionMode, surfaceWidth } = useIafTheme();
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [editing, setEditing] = useState<PlatformOrg | null>(null);
  const [createOpen, setCreateOpen] = useState(false);
  const [form] = Form.useForm<OrgFormValues>();

  // Custom Hooks
  const orgQuery = useOrgTreeQuery();

  const createMutation = useCreateOrgMutation({
    onSuccess: () => {
      message.success(t('common.feedback.operationSucceeded'));
      setCreateOpen(false);
      form.resetFields();
    }
  });

  const updateMutation = useUpdateOrgMutation({
    onSuccess: () => {
      message.success(t('common.feedback.operationSucceeded'));
      setEditing(null);
      form.resetFields();
    }
  });

  // Permissions hooks
  const hasUpdate = useHasPermission(PLATFORM_PERMISSIONS.orgUpdate);

  const flatOrgs = useMemo(() => flattenOrgs(orgQuery.data ?? []), [orgQuery.data]);
  const selectedOrg = flatOrgs.find((item) => item.id === selectedId) ?? null;

  const openCreate = () => {
    form.setFieldsValue({
      parentId: selectedOrg?.id ?? null,
      orgType: 'DEPARTMENT',
      status: 'ACTIVE',
      sortNo: 0
    });
    setCreateOpen(true);
  };

  const openEdit = (record: PlatformOrg) => {
    setEditing(record);
    form.setFieldsValue({
      parentId: record.parentId,
      orgCode: record.orgCode,
      orgName: record.orgName,
      orgType: record.orgType,
      status: record.status,
      sortNo: record.sortNo
    });
  };

  const submitForm = async (values: OrgFormValues) => {
    if (editing) {
      await updateMutation.mutateAsync({ id: editing.id, values });
      return;
    }
    await createMutation.mutateAsync(values);
  };

  const columns: ColumnsType<PlatformOrg> = [
    { title: t('orgs.orgCode'), dataIndex: 'orgCode' },
    { title: t('orgs.orgName'), dataIndex: 'orgName' },
    { title: t('orgs.orgType'), dataIndex: 'orgType', render: (type: PlatformOrg['orgType']) => t(`orgs.type.${type}`) },
    {
      title: t('common.fields.status'),
      dataIndex: 'status',
      render: (status: PlatformOrg['status']) => <StatusTag status={status} label={t(`common.status.${status}`)} />
    },
    {
      title: t('common.fields.actions'),
      key: 'actions',
      render: (_, record) =>
        hasUpdate ? (
          <Button type="link" onClick={() => openEdit(record)}>
            {t('common.actions.edit')}
          </Button>
        ) : null
    }
  ];

  const surfaceOpen = createOpen || Boolean(editing);
  const usePageSurface = formInteractionMode === 'page' && surfaceOpen;

  return (
    <>
      {!usePageSurface && (
        <AppPageContainer
          title={t('orgs.title')}
          extra={
            <PermissionButton type="primary" require={PLATFORM_PERMISSIONS.orgCreate} onClick={openCreate}>
              {t('common.actions.create')}
            </PermissionButton>
          }
        >
          <Space align="start" size={16} style={{ width: '100%' }}>
            <div style={{ width: 280 }}>
              <OrgTreeView items={orgQuery.data ?? []} onSelect={setSelectedId} />
            </div>
            <Table
              style={{ flex: 1 }}
              rowKey="id"
              loading={orgQuery.isLoading}
              dataSource={flatOrgs}
              columns={columns}
              pagination={false}
            />
          </Space>
        </AppPageContainer>
      )}
      <FormInteractionSurface
        mode={formInteractionMode}
        open={surfaceOpen}
        title={editing ? `${t('common.actions.edit')} · ${editing.orgCode}` : `${t('common.actions.create')} · ${t('orgs.title')}`}
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
        <Form<OrgFormValues> layout="vertical" form={form} onFinish={submitForm}>
          <Form.Item name="parentId" label={t('orgs.parentId')}>
            <InputNumber style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="orgCode" label={t('orgs.orgCode')} rules={[{ required: true, message: t('auth.required') }]}>
            <Input />
          </Form.Item>
          <Form.Item name="orgName" label={t('orgs.orgName')} rules={[{ required: true, message: t('auth.required') }]}>
            <Input />
          </Form.Item>
          <Form.Item name="orgType" label={t('orgs.orgType')} rules={[{ required: true, message: t('auth.required') }]}>
            <Select
              options={(['COMPANY', 'DEPARTMENT', 'TEAM'] as const).map((value) => ({
                value,
                label: t(`orgs.type.${value}`)
              }))}
            />
          </Form.Item>
          <Form.Item name="status" label={t('common.fields.status')} rules={[{ required: true, message: t('auth.required') }]}>
            <Select
              options={(['ACTIVE', 'DISABLED'] as const).map((value) => ({
                value,
                label: t(`common.status.${value}`)
              }))}
            />
          </Form.Item>
          <Form.Item name="sortNo" label={t('orgs.sortNo')}>
            <InputNumber style={{ width: '100%' }} />
          </Form.Item>
        </Form>
      </FormInteractionSurface>
    </>
  );
};
