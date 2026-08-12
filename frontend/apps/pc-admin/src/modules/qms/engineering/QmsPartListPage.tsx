import type { QmsPart, QmsPartCreateRequest } from '@iaf/domain-types';
import { QMS_PERMISSIONS, hasPermission, useUserPermissions } from '@iaf/permissions';
import { ConfigurableListPage, type ListViewDefinition } from '@iaf/table-engine';
import { useIafTheme, iafSurfaceWidths } from '@iaf/theme';
import { AppPageContainer, FormInteractionSurface, StatusTag } from '@iaf/ui-core';
import { Alert, App, Button, Form, Input, InputNumber, Space } from 'antd';
import { useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';
import { useRegisterDirty } from '../../../workspace/DirtyStateRegistry';
import { useCreateQmsPartMutation, useQmsPartsQuery } from './hooks';
import { buildPageAIContext, QmsPageContextProvider } from './pageContext';

const normalizeCreateRequest = (values: QmsPartCreateRequest): QmsPartCreateRequest => ({
  ...values,
  materialNo: values.materialNo || null,
  vehicleModel: values.vehicleModel || null,
  importanceLevel: values.importanceLevel || null
});

export const QmsPartListPage = () => {
  const { t } = useTranslation();
  const { message } = App.useApp();
  const navigate = useNavigate();
  const permissions = useUserPermissions();
  const { formInteractionMode, surfaceWidth } = useIafTheme();
  const [form] = Form.useForm<QmsPartCreateRequest>();
  const [createOpen, setCreateOpen] = useState(false);
  const [dirty, setDirty] = useState(false);
  const [params, setParams] = useState({ keyword: '', pageNo: 1, pageSize: 20 });
  useRegisterDirty(createOpen && dirty);

  const partsQuery = useQmsPartsQuery(params);
  const createMutation = useCreateQmsPartMutation(() => {
    message.success(t('common.feedback.operationSucceeded'));
    setCreateOpen(false);
    setDirty(false);
    form.resetFields();
  });

  const definition = useMemo<ListViewDefinition<QmsPart>>(() => ({
    id: 'qmsParts',
    descriptionKey: 'qmsParts.description',
    columns: [
      { key: 'partNo', dataIndex: 'partNo', titleKey: 'qmsParts.fields.partNo', fixed: 'left', width: 170 },
      { key: 'partName', dataIndex: 'partName', titleKey: 'qmsParts.fields.partName', width: 220 },
      { key: 'materialNo', dataIndex: 'materialNo', titleKey: 'qmsParts.fields.materialNo', width: 160 },
      { key: 'vehicleModel', dataIndex: 'vehicleModel', titleKey: 'qmsParts.fields.vehicleModel', width: 160 },
      { key: 'importanceLevel', dataIndex: 'importanceLevel', titleKey: 'qmsParts.fields.importanceLevel', width: 120 },
      {
        key: 'status', dataIndex: 'status', titleKey: 'common.fields.status', width: 110,
        render: (status: QmsPart['status']) => <StatusTag status={status} label={t(`qms.status.${status}`)} />
      },
      { key: 'updatedAt', dataIndex: 'updatedAt', titleKey: 'common.fields.updatedAt', width: 190 }
    ],
    searchFields: [{ key: 'keyword', labelKey: 'qmsParts.search.keyword', type: 'text', placeholderKey: 'qmsParts.search.placeholder' }],
    toolbarActions: [{
      key: 'create', labelKey: 'common.actions.create', type: 'primary', requirePermission: QMS_PERMISSIONS.partCreate,
      onClick: () => setCreateOpen(true)
    }],
    rowActions: [{
      key: 'view', labelKey: 'common.actions.view', requirePermission: QMS_PERMISSIONS.partView,
      onClick: (record) => navigate(`/qms/engineering/parts/${record.id}`)
    }]
  }), [navigate, t]);

  const closeCreate = () => {
    setCreateOpen(false);
    setDirty(false);
    createMutation.reset();
    form.resetFields();
  };

  const submit = async (values: QmsPartCreateRequest) => {
    try {
      await createMutation.mutateAsync(normalizeCreateRequest(values));
    } catch {
      // The mutation error remains visible in the form surface for recovery.
    }
  };

  const pageContext = { module: 'qms.engineering', pageType: 'list' as const, objectType: 'Part', routePath: '/qms/engineering/parts' };
  const aiContext = buildPageAIContext({
    ...pageContext,
    permissions: permissions.filter((permission) => permission.startsWith('qms:')),
    availableActions: hasPermission(permissions, QMS_PERMISSIONS.partCreate) ? ['createPart', 'searchParts', 'viewPart'] : ['searchParts', 'viewPart'],
    visibleFields: ['partNo', 'partName', 'materialNo', 'vehicleModel', 'importanceLevel', 'status', 'updatedAt']
  });
  const usePageSurface = formInteractionMode === 'page' && createOpen;

  return (
    <QmsPageContextProvider page={pageContext} ai={aiContext}>
      {!usePageSurface && (partsQuery.isError ? (
        <AppPageContainer title={t('qmsParts.title')}>
          <Alert
            type="error"
            showIcon
            message={t('common.feedback.loadFailed')}
            description={t('qms.feedback.loadPartsFailed')}
            action={<Button onClick={() => partsQuery.refetch()}>{t('common.actions.retry')}</Button>}
          />
        </AppPageContainer>
      ) : (
        <ConfigurableListPage
          definition={definition}
          loading={partsQuery.isLoading || partsQuery.isFetching}
          dataSource={partsQuery.data?.records ?? []}
          total={partsQuery.data?.total ?? 0}
          pageNo={params.pageNo}
          pageSize={params.pageSize}
          onPageChange={(pageNo, pageSize) => setParams((current) => ({ ...current, pageNo, pageSize }))}
          onSearch={(query) => setParams((current) => ({ ...current, keyword: String(query.keyword ?? ''), pageNo: 1 }))}
          onReset={() => setParams((current) => ({ ...current, keyword: '', pageNo: 1 }))}
          onRefresh={() => partsQuery.refetch()}
        />
      ))}
      <FormInteractionSurface
        mode={formInteractionMode}
        open={createOpen}
        title={`${t('common.actions.create')} · ${t('qmsParts.title')}`}
        onCancel={closeCreate}
        onSubmit={() => form.submit()}
        confirmLoading={createMutation.isPending}
        submitLabel={t('common.actions.save')}
        cancelLabel={t('common.actions.cancel')}
        width={iafSurfaceWidths[surfaceWidth]}
      >
        <Form<QmsPartCreateRequest> form={form} layout="vertical" onFinish={submit} onFieldsChange={() => setDirty(true)}>
          {createMutation.isError && (
            <Form.Item>
              <Alert type="error" showIcon message={t('qms.feedback.createPartFailed')} description={createMutation.error.message} />
            </Form.Item>
          )}
          <Space size={16} wrap style={{ width: '100%' }}>
          <Form.Item name="partNo" label={t('qmsParts.fields.partNo')} rules={[{ required: true, message: t('common.validation.required') }, { max: 128, message: t('common.validation.maxLength', { max: 128 }) }]}>
              <Input autoFocus autoComplete="off" />
            </Form.Item>
            <Form.Item name="materialNo" label={t('qmsParts.fields.materialNo')} rules={[{ max: 128, message: t('common.validation.maxLength', { max: 128 }) }]}>
              <Input autoComplete="off" />
            </Form.Item>
          </Space>
          <Form.Item name="partName" label={t('qmsParts.fields.partName')} rules={[{ required: true, message: t('common.validation.required') }, { max: 256, message: t('common.validation.maxLength', { max: 256 }) }]}>
            <Input autoComplete="off" />
          </Form.Item>
          <Space size={16} wrap style={{ width: '100%' }}>
            <Form.Item name="customerId" label={t('qmsParts.fields.customerId')}><InputNumber min={1} style={{ width: '100%' }} /></Form.Item>
            <Form.Item name="supplierId" label={t('qmsParts.fields.supplierId')}><InputNumber min={1} style={{ width: '100%' }} /></Form.Item>
          </Space>
          <Form.Item name="vehicleModel" label={t('qmsParts.fields.vehicleModel')} rules={[{ max: 128, message: t('common.validation.maxLength', { max: 128 }) }]}><Input /></Form.Item>
          <Form.Item name="importanceLevel" label={t('qmsParts.fields.importanceLevel')} rules={[{ max: 32, message: t('common.validation.maxLength', { max: 32 }) }]}><Input /></Form.Item>
        </Form>
      </FormInteractionSurface>
    </QmsPageContextProvider>
  );
};
