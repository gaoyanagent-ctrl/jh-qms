import type {
  QmsDrawing,
  QmsDrawingCreateRequest,
  QmsDrawingRevision,
  QmsDrawingRevisionCreateRequest
} from '@iaf/domain-types';
import { PermissionButton, QMS_PERMISSIONS, hasPermission, useUserPermissions } from '@iaf/permissions';
import { useIafTheme, iafSurfaceWidths } from '@iaf/theme';
import { AppPageContainer, FormInteractionSurface, StatusTag } from '@iaf/ui-core';
import { Alert, App, Button, Card, Descriptions, Form, Input, Select, Space, Table, Typography, Upload, theme } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useEffect, useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useNavigate, useParams } from 'react-router-dom';
import { useRegisterDirty } from '../../../workspace/DirtyStateRegistry';
import {
  useCreateQmsDrawingMutation,
  useCreateQmsRevisionMutation,
  useQmsDrawingsQuery,
  useQmsPartQuery,
  useQmsRevisionsQuery,
  useUploadQmsRevisionFileMutation
} from './hooks';
import { buildPageAIContext, QmsPageContextProvider } from './pageContext';

const drawingTypes: QmsDrawing['drawingType'][] = ['PRODUCT', 'PART', 'ASSEMBLY', 'OTHER'];
const sourceSystems: QmsDrawing['sourceSystem'][] = ['MANUAL', 'PLM', 'MIGRATION'];

export const QmsPartDetailPage = () => {
  const { t, i18n } = useTranslation();
  const { message } = App.useApp();
  const { token } = theme.useToken();
  const navigate = useNavigate();
  const routeParams = useParams<{ partId: string }>();
  const partId = Number(routeParams.partId);
  const validPartId = Number.isSafeInteger(partId) && partId > 0;
  const permissions = useUserPermissions();
  const canViewDrawings = hasPermission(permissions, QMS_PERMISSIONS.drawingView);
  const canViewRevisions = hasPermission(permissions, QMS_PERMISSIONS.drawingRevisionView);
  const canUploadRevisionFile = hasPermission(permissions, QMS_PERMISSIONS.drawingRevisionUpload);
  const { formInteractionMode, surfaceWidth, workspaceMode } = useIafTheme();
  const [selectedDrawingId, setSelectedDrawingId] = useState<number>();
  const [drawingOpen, setDrawingOpen] = useState(false);
  const [revisionOpen, setRevisionOpen] = useState(false);
  const [dirty, setDirty] = useState(false);
  const [drawingForm] = Form.useForm<QmsDrawingCreateRequest>();
  const [revisionForm] = Form.useForm<QmsDrawingRevisionCreateRequest>();
  useRegisterDirty((drawingOpen || revisionOpen) && dirty);

  const partQuery = useQmsPartQuery(validPartId ? partId : undefined);
  const drawingsQuery = useQmsDrawingsQuery(validPartId ? partId : undefined, canViewDrawings);
  const revisionsQuery = useQmsRevisionsQuery(selectedDrawingId, canViewRevisions);
  const uploadFile = useUploadQmsRevisionFileMutation(selectedDrawingId, () => message.success(t('qmsRevisions.feedback.uploadSucceeded')));
  const selectedDrawing = drawingsQuery.data?.find((drawing) => drawing.id === selectedDrawingId);

  useEffect(() => {
    const drawings = drawingsQuery.data ?? [];
    if (drawings.length === 0) {
      setSelectedDrawingId(undefined);
    } else if (!drawings.some((drawing) => drawing.id === selectedDrawingId)) {
      setSelectedDrawingId(drawings[0].id);
    }
  }, [drawingsQuery.data, selectedDrawingId]);

  const closeForms = () => {
    if (drawingOpen) drawingForm.resetFields();
    if (revisionOpen) revisionForm.resetFields();
    setDrawingOpen(false);
    setRevisionOpen(false);
    setDirty(false);
    createDrawing.reset();
    createRevision.reset();
  };

  const createDrawing = useCreateQmsDrawingMutation(partId, () => {
    message.success(t('common.feedback.operationSucceeded'));
    closeForms();
  });
  const createRevision = useCreateQmsRevisionMutation(selectedDrawingId, () => {
    message.success(t('common.feedback.operationSucceeded'));
    closeForms();
  });

  const formatDateTime = (value?: string | null) => value
    ? new Intl.DateTimeFormat(i18n.language, { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(value))
    : t('common.notAvailable');

  const drawingColumns = useMemo<ColumnsType<QmsDrawing>>(() => [
    { title: t('qmsDrawings.fields.drawingNo'), dataIndex: 'drawingNo', width: 150 },
    { title: t('qmsDrawings.fields.drawingName'), dataIndex: 'drawingName', width: 210 },
    { title: t('qmsDrawings.fields.drawingType'), dataIndex: 'drawingType', width: 120, render: (value) => t(`qms.drawingType.${value}`) },
    ...(workspaceMode === 'expert' ? [{ title: t('qmsDrawings.fields.sourceSystem'), dataIndex: 'sourceSystem', width: 130, render: (value: string) => t(`qms.sourceSystem.${value}`) }] : []),
    { title: t('common.fields.status'), dataIndex: 'status', width: 110, render: (value) => <StatusTag status={value} label={t(`qms.status.${value}`)} /> }
  ], [t, workspaceMode]);

  const revisionColumns = useMemo<ColumnsType<QmsDrawingRevision>>(() => [
    { title: t('qmsRevisions.fields.revisionCode'), dataIndex: 'revisionCode', width: 130 },
    ...(workspaceMode === 'expert' ? [{ title: t('qmsRevisions.fields.revisionSeq'), dataIndex: 'revisionSeq', width: 90 }] : []),
    { title: t('qmsRevisions.fields.effectiveDate'), dataIndex: 'effectiveDate', width: 130, render: (value) => value ?? t('common.notAvailable') },
    { title: t('qmsRevisions.fields.parseStatus'), dataIndex: 'parseStatus', width: 135, render: (value) => <StatusTag status={value} label={t(`qms.status.${value}`)} /> },
    { title: t('qmsRevisions.fields.reviewStatus'), dataIndex: 'reviewStatus', width: 135, render: (value) => <StatusTag status={value} label={t(`qms.status.${value}`)} /> },
    { title: t('common.fields.status'), dataIndex: 'status', width: 120, render: (value) => <StatusTag status={value} label={t(`qms.status.${value}`)} /> },
    { title: t('qmsRevisions.fields.file'), width: 190, fixed: 'right', render: (_, revision) => revision.fileId
      ? <Typography.Text>{revision.fileType} · {revision.checksum?.slice(0, 8)}</Typography.Text>
      : <Upload disabled={!canUploadRevisionFile} accept=".pdf,.dwg" maxCount={1} showUploadList={false} beforeUpload={(file) => { uploadFile.mutate({ revisionId: revision.id, file }); return false; }}>
          <Button disabled={!canUploadRevisionFile} title={!canUploadRevisionFile ? t('qmsRevisions.feedback.uploadPermissionRequired') : undefined} size="small" loading={uploadFile.isPending}>{t('qmsRevisions.actions.upload')}</Button>
        </Upload> },
    ...(workspaceMode === 'expert' ? [{ title: t('common.fields.createdAt'), dataIndex: 'createdAt', width: 190, render: (value: string) => formatDateTime(value) }] : [])
  ], [canUploadRevisionFile, i18n.language, t, workspaceMode, uploadFile]);

  const submitDrawing = async (values: QmsDrawingCreateRequest) => {
    try {
      await createDrawing.mutateAsync(values);
    } catch {
      // Keep the form and backend error visible so the operator can correct it.
    }
  };

  const submitRevision = async (values: QmsDrawingRevisionCreateRequest) => {
    try {
      await createRevision.mutateAsync({ ...values, effectiveDate: values.effectiveDate || null, supersedesRevisionId: values.supersedesRevisionId || null });
    } catch {
      // Keep the form and backend error visible so the operator can correct it.
    }
  };

  const pageContext = {
    module: 'qms.engineering', pageType: 'detail' as const, objectType: 'Part',
    objectId: validPartId ? String(partId) : undefined, routePath: `/qms/engineering/parts/${routeParams.partId ?? ''}`
  };
  const aiContext = buildPageAIContext({
    ...pageContext,
    currentStatus: partQuery.data ? { part: partQuery.data.status, drawing: selectedDrawing?.status ?? '' } : undefined,
    permissions: permissions.filter((permission) => permission.startsWith('qms:')),
    availableActions: [
      ...(hasPermission(permissions, QMS_PERMISSIONS.drawingCreate) ? ['createDrawing'] : []),
      ...(hasPermission(permissions, QMS_PERMISSIONS.drawingRevisionCreate) && selectedDrawing ? ['createRevision'] : []),
      ...(canUploadRevisionFile && selectedDrawing ? ['uploadRevisionFile'] : [])
    ],
    visibleFields: [
      'partNo', 'partName', 'materialNo', 'vehicleModel', 'importanceLevel', 'status',
      ...(canViewDrawings ? ['drawings'] : []),
      ...(canViewRevisions ? ['revisions'] : []),
      ...(workspaceMode === 'expert' ? ['orgId', 'version', 'timestamps'] : []),
      ...(workspaceMode === 'expert' && canViewDrawings ? ['drawingSource'] : []),
      ...(workspaceMode === 'expert' && canViewRevisions ? ['revisionSequence'] : [])
    ]
  });

  if (!validPartId) {
    return (
      <QmsPageContextProvider page={pageContext} ai={aiContext}>
        <AppPageContainer title={t('qmsPartDetail.title')}>
          <Alert type="error" showIcon message={t('qms.feedback.invalidPartId')} action={<Button onClick={() => navigate('/qms/engineering/parts')}>{t('common.actions.back')}</Button>} />
        </AppPageContainer>
      </QmsPageContextProvider>
    );
  }

  if (partQuery.isError || drawingsQuery.isError) {
    return (
      <QmsPageContextProvider page={pageContext} ai={aiContext}>
        <AppPageContainer title={t('qmsPartDetail.title')}>
          <Alert
            type="error" showIcon message={t('common.feedback.loadFailed')} description={t('qms.feedback.loadPartDetailFailed')}
            action={<Button onClick={() => Promise.all([partQuery.refetch(), drawingsQuery.refetch()])}>{t('common.actions.retry')}</Button>}
          />
        </AppPageContainer>
      </QmsPageContextProvider>
    );
  }

  const formOpen = drawingOpen || revisionOpen;
  const usePageSurface = formInteractionMode === 'page' && formOpen;
  const part = partQuery.data;

  return (
    <QmsPageContextProvider page={pageContext} ai={aiContext}>
      {!usePageSurface && (
        <AppPageContainer
          title={part ? `${part.partNo} · ${part.partName}` : t('common.feedback.loading')}
          extra={<Button onClick={() => navigate('/qms/engineering/parts')}>{t('common.actions.back')}</Button>}
        >
          <Card loading={partQuery.isLoading} styles={{ body: { padding: token.paddingLG } }}>
            {part && (
              <Descriptions bordered size="small" column={{ xs: 1, sm: 2, lg: workspaceMode === 'expert' ? 4 : 3 }}>
                <Descriptions.Item label={t('qmsParts.fields.partNo')}>{part.partNo}</Descriptions.Item>
                <Descriptions.Item label={t('qmsParts.fields.partName')}>{part.partName}</Descriptions.Item>
                <Descriptions.Item label={t('qmsParts.fields.materialNo')}>{part.materialNo ?? t('common.notAvailable')}</Descriptions.Item>
                <Descriptions.Item label={t('qmsParts.fields.vehicleModel')}>{part.vehicleModel ?? t('common.notAvailable')}</Descriptions.Item>
                <Descriptions.Item label={t('qmsParts.fields.importanceLevel')}>{part.importanceLevel ?? t('common.notAvailable')}</Descriptions.Item>
                <Descriptions.Item label={t('common.fields.status')}><StatusTag status={part.status} label={t(`qms.status.${part.status}`)} /></Descriptions.Item>
                {workspaceMode === 'expert' && <Descriptions.Item label={t('qmsParts.fields.orgId')}>{part.orgId}</Descriptions.Item>}
                {workspaceMode === 'expert' && <Descriptions.Item label={t('qmsParts.fields.version')}>{part.version}</Descriptions.Item>}
                {workspaceMode === 'expert' && <Descriptions.Item label={t('common.fields.updatedAt')}>{formatDateTime(part.updatedAt)}</Descriptions.Item>}
              </Descriptions>
            )}
          </Card>

          <Card
            title={t('qmsDrawings.title')}
            extra={<PermissionButton require={QMS_PERMISSIONS.drawingCreate} type="primary" onClick={() => { drawingForm.setFieldsValue({ drawingType: 'PART', sourceSystem: 'MANUAL' }); setDrawingOpen(true); }}>{t('qmsDrawings.actions.create')}</PermissionButton>}
          >
            {!canViewDrawings ? (
              <Alert type="info" showIcon message={t('qms.feedback.drawingViewRestricted')} />
            ) : (
              <Table<QmsDrawing>
                rowKey="id" size="small" bordered loading={drawingsQuery.isLoading} dataSource={drawingsQuery.data ?? []}
                columns={drawingColumns} pagination={false} scroll={{ x: 'max-content' }}
                rowClassName={(record) => record.id === selectedDrawingId ? 'ant-table-row-selected' : ''}
                onRow={(record) => ({
                  onClick: () => setSelectedDrawingId(record.id),
                  onKeyDown: (event) => {
                    if (event.key === 'Enter' || event.key === ' ') {
                      event.preventDefault();
                      setSelectedDrawingId(record.id);
                    }
                  },
                  tabIndex: 0,
                  style: { cursor: 'pointer' },
                  'aria-selected': record.id === selectedDrawingId
                })}
                locale={{ emptyText: t('qmsDrawings.empty') }}
              />
            )}
          </Card>

          <Card
            title={selectedDrawing ? `${t('qmsRevisions.title')} · ${selectedDrawing.drawingNo}` : t('qmsRevisions.title')}
            extra={selectedDrawing && <PermissionButton require={QMS_PERMISSIONS.drawingRevisionCreate} type="primary" onClick={() => setRevisionOpen(true)}>{t('qmsRevisions.actions.create')}</PermissionButton>}
          >
            {!canViewRevisions ? (
              <Alert type="info" showIcon message={t('qms.feedback.revisionViewRestricted')} />
            ) : !selectedDrawing ? (
              <Typography.Text type="secondary">{t('qmsRevisions.selectDrawing')}</Typography.Text>
            ) : revisionsQuery.isError ? (
              <Alert type="error" showIcon message={t('qms.feedback.loadRevisionsFailed')} action={<Button onClick={() => revisionsQuery.refetch()}>{t('common.actions.retry')}</Button>} />
            ) : (
              <Table<QmsDrawingRevision>
                rowKey="id" size="small" bordered loading={revisionsQuery.isLoading} dataSource={revisionsQuery.data ?? []}
                columns={revisionColumns} pagination={false} scroll={{ x: 'max-content' }} locale={{ emptyText: t('qmsRevisions.empty') }}
              />
            )}
          </Card>
        </AppPageContainer>
      )}

      <FormInteractionSurface
        mode={formInteractionMode} open={drawingOpen} title={`${t('qmsDrawings.actions.create')} · ${part?.partNo ?? ''}`}
        onCancel={closeForms} onSubmit={() => drawingForm.submit()} confirmLoading={createDrawing.isPending}
        submitLabel={t('common.actions.save')} cancelLabel={t('common.actions.cancel')} width={iafSurfaceWidths[surfaceWidth]}
      >
        <Form<QmsDrawingCreateRequest> form={drawingForm} layout="vertical" onFinish={submitDrawing} onFieldsChange={() => setDirty(true)}>
          {createDrawing.isError && <Form.Item><Alert type="error" showIcon message={t('qms.feedback.createDrawingFailed')} description={createDrawing.error.message} /></Form.Item>}
          <Form.Item name="drawingNo" label={t('qmsDrawings.fields.drawingNo')} rules={[{ required: true, message: t('common.validation.required') }, { max: 128, message: t('common.validation.maxLength', { max: 128 }) }]}><Input autoFocus /></Form.Item>
          <Form.Item name="drawingName" label={t('qmsDrawings.fields.drawingName')} rules={[{ required: true, message: t('common.validation.required') }, { max: 256, message: t('common.validation.maxLength', { max: 256 }) }]}><Input /></Form.Item>
          <Form.Item name="drawingType" label={t('qmsDrawings.fields.drawingType')} rules={[{ required: true, message: t('common.validation.required') }]}>
            <Select options={drawingTypes.map((value) => ({ value, label: t(`qms.drawingType.${value}`) }))} />
          </Form.Item>
          <Form.Item name="sourceSystem" label={t('qmsDrawings.fields.sourceSystem')}>
            <Select options={sourceSystems.map((value) => ({ value, label: t(`qms.sourceSystem.${value}`) }))} />
          </Form.Item>
        </Form>
      </FormInteractionSurface>

      <FormInteractionSurface
        mode={formInteractionMode} open={revisionOpen} title={`${t('qmsRevisions.actions.create')} · ${selectedDrawing?.drawingNo ?? ''}`}
        onCancel={closeForms} onSubmit={() => revisionForm.submit()} confirmLoading={createRevision.isPending}
        submitLabel={t('common.actions.save')} cancelLabel={t('common.actions.cancel')} width={iafSurfaceWidths[surfaceWidth]}
      >
        <Form<QmsDrawingRevisionCreateRequest> form={revisionForm} layout="vertical" onFinish={submitRevision} onFieldsChange={() => setDirty(true)}>
          {createRevision.isError && <Form.Item><Alert type="error" showIcon message={t('qms.feedback.createRevisionFailed')} description={createRevision.error.message} /></Form.Item>}
          <Form.Item name="revisionCode" label={t('qmsRevisions.fields.revisionCode')} rules={[{ required: true, message: t('common.validation.required') }, { max: 64, message: t('common.validation.maxLength', { max: 64 }) }]}><Input autoFocus /></Form.Item>
          <Form.Item name="effectiveDate" label={t('qmsRevisions.fields.effectiveDate')}><Input type="date" /></Form.Item>
          <Form.Item name="supersedesRevisionId" label={t('qmsRevisions.fields.supersedesRevision')}>
            <Select allowClear options={(revisionsQuery.data ?? []).map((revision) => ({ value: revision.id, label: revision.revisionCode }))} />
          </Form.Item>
        </Form>
      </FormInteractionSurface>
    </QmsPageContextProvider>
  );
};
