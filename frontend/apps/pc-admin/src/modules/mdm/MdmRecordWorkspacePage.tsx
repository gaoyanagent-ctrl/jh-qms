import { MDM_PERMISSIONS } from '@iaf/permissions';
import { ConfigurableListPage, type ColumnDefinition, type ListViewDefinition } from '@iaf/table-engine';
import { AppPageContainer, FormInteractionSurface, StatusTag } from '@iaf/ui-core';
import { useIafTheme, iafSurfaceWidths } from '@iaf/theme';
import { Alert, App, Button, DatePicker, Divider, Drawer, Empty, Form, Input, InputNumber, Modal, Select, Space, Switch, Table, Tag, Timeline, Typography } from 'antd';
import dayjs, { type Dayjs } from 'dayjs';
import { useMemo, useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { useParams } from 'react-router-dom';
import { useRegisterDirty } from '../../workspace/DirtyStateRegistry';
import { useMdmRecordAction, useMdmRecordActions, useMdmRecords, useMdmRecordVersions, useMdmSchema, useSaveMdmRecord } from './hooks';
import { MdmPageContextProvider } from './pageContext';
import { MdmExcelPasteModal } from './MdmExcelPasteModal';
import { MdmExcelFileImportModal } from './MdmExcelFileImportModal';
import type { MdmField, MdmRecord, MdmRecordActionType, MdmRecordVersion, SaveMdmRecord } from './types';
import { mdmApi } from './api';

type AttributeValue = string | number | boolean | null | undefined;
type FormValues = { businessCode:string; name:string; effectiveFrom?:Dayjs; effectiveTo?:Dayjs; attributes:Record<string,AttributeValue> };
export type MdmVersionDiff = { key:string; before:unknown; after:unknown };

const comparableSnapshot = (snapshot:Record<string,unknown>) => {
  const attributes = snapshot.attributes && typeof snapshot.attributes === 'object' ? snapshot.attributes as Record<string,unknown> : {};
  const { attributes:_attributes,id:_id,modelId:_modelId,modelCode:_modelCode,currentVersionNo:_currentVersionNo,modelVersionNo:_modelVersionNo,version:_version,createdAt:_createdAt,updatedAt:_updatedAt,...business } = snapshot;
  return {...business,...attributes};
};

export const buildMdmVersionDiff = (current:MdmRecordVersion, previous?:MdmRecordVersion):MdmVersionDiff[] => {
  const after=comparableSnapshot(current.snapshot); const before=previous?comparableSnapshot(previous.snapshot):{};
  return [...new Set([...Object.keys(before),...Object.keys(after)])].filter(key=>JSON.stringify(before[key])!==JSON.stringify(after[key])).map(key=>({key,before:before[key],after:after[key]}));
};

const displayValue=(value:unknown)=>value===undefined||value===null||value===''?'—':typeof value==='boolean'?(value?'是':'否'):typeof value==='object'?JSON.stringify(value):String(value);

export const buildMdmDynamicColumns = (fields: MdmField[]): ColumnDefinition<MdmRecord>[] => fields.map((field) => ({
  key:field.code, titleKey:field.name, width:150, defaultVisible:field.listVisible,
  render:(_value, record) => String(record.attributes[field.code] ?? '')
}));

export const allowedMdmRecordActions=(status:string):MdmRecordActionType[]=>status==='DRAFT'||status==='REJECTED'?['SUBMIT']:status==='PENDING_APPROVAL'?['APPROVE','REJECT']:status==='ACTIVE'?['DEACTIVATE']:[];

const recordFieldValue=(record:MdmRecord,fieldCode:string):unknown=>fieldCode==='businessCode'?record.businessCode:fieldCode==='name'?record.name:fieldCode==='lifecycleStatus'?record.lifecycleStatus:record.attributes[fieldCode];

const ReferenceSelect=({field}:{field:MdmField})=>{
  const {t}=useTranslation();
  const config=field.referenceConfig;const [keyword,setKeyword]=useState('');
  const query=useQuery({queryKey:['mdm-reference-options',config?.targetModelCode,keyword],queryFn:()=>mdmApi.records(config!.targetModelCode,{keyword,pageNo:1,pageSize:100}),enabled:Boolean(config?.targetModelCode)});
  const options=(query.data?.records??[]).filter(record=>!config?.statusFieldCode||!config.allowedStatuses?.length||config.allowedStatuses.includes(String(recordFieldValue(record,config.statusFieldCode)))).map(record=>{const value=recordFieldValue(record,config?.valueFieldCode??'businessCode');const display=recordFieldValue(record,config?.displayFieldCode??'name');return {value:String(value??''),label:`${String(display??value??'')} (${String(value??'')})`};});
  return <Select showSearch allowClear filterOption={false} onSearch={setKeyword} loading={query.isFetching} options={options} placeholder={config?.targetModelCode?t('mdm.reference.search',{model:config.targetModelCode}):t('mdm.reference.incomplete')} disabled={!config?.targetModelCode} notFoundContent={query.isFetching?t('common.feedback.loading'):t('mdm.reference.empty')} />;
};

const fieldControl = (field: MdmField) => {
  if (field.dataType === 'BOOLEAN') return <Switch />;
  if (field.dataType === 'ENUM') return <Select options={field.enumOptions.map((value) => ({ value, label: value }))} />;
  if (field.dataType === 'INTEGER' || field.dataType === 'DECIMAL') return <InputNumber style={{ width: '100%' }} />;
  if (field.dataType === 'DATE') return <DatePicker style={{ width: '100%' }} />;
  if (field.dataType === 'REFERENCE') return <ReferenceSelect field={field}/>;
  return field.dataType === 'TEXT' ? <Input.TextArea rows={3} /> : <Input />;
};

export const MdmRecordWorkspacePage = () => {
  const { t } = useTranslation(); const { message } = App.useApp();
  const code = useParams().modelCode ?? 'material'; const { formInteractionMode, surfaceWidth } = useIafTheme();
  const [form] = Form.useForm<FormValues>(); const [params, setParams] = useState({ keyword:'', pageNo:1, pageSize:20 });
  const [open, setOpen] = useState(false); const [editing, setEditing] = useState<MdmRecord>(); const [dirty, setDirty] = useState(false);
  const [historyRecord,setHistoryRecord]=useState<MdmRecord>(); const versions=useMdmRecordVersions(code,historyRecord?.id); const actions=useMdmRecordActions(code,historyRecord?.id);
  const [pendingAction,setPendingAction]=useState<{record:MdmRecord;action:MdmRecordActionType}>(); const [actionForm]=Form.useForm<{comment:string}>();
  const [pasteOpen,setPasteOpen]=useState(false);
  const [fileImportOpen,setFileImportOpen]=useState(false);
  useRegisterDirty(open && dirty); const schema = useMdmSchema(code); const records = useMdmRecords(code, params);
  const close = () => { setOpen(false); setEditing(undefined); setDirty(false); form.resetFields(); };
  const save = useSaveMdmRecord(code, () => { message.success(t('common.feedback.operationSucceeded')); close(); });
  const edit = (record:MdmRecord) => {
    setEditing(record);
    form.setFieldsValue({ businessCode:record.businessCode, name:record.name,
      effectiveFrom:record.effectiveFrom ? dayjs(record.effectiveFrom) : undefined, effectiveTo:record.effectiveTo ? dayjs(record.effectiveTo) : undefined,
      attributes: record.attributes as Record<string, AttributeValue> });
    setOpen(true);
  };
  const definition = useMemo<ListViewDefinition<MdmRecord>>(() => {
    const dynamicColumns = buildMdmDynamicColumns(schema.data?.fields ?? []);
    return { id:`mdm-${code}`, descriptionKey:'mdm.records.description', columns:[
      { key:'businessCode', dataIndex:'businessCode', titleKey:'mdm.fields.businessCode', fixed:'left', width:160 },
      { key:'name', dataIndex:'name', titleKey:'mdm.fields.name', fixed:'left', width:200 }, ...dynamicColumns,
      { key:'lifecycleStatus', dataIndex:'lifecycleStatus', titleKey:'common.fields.status', width:110, render:(status) => <StatusTag status={String(status)} label={t(`mdm.status.${String(status)}`)} /> },
      { key:'currentVersionNo', dataIndex:'currentVersionNo', titleKey:'mdm.fields.dataVersion', width:100 }
    ], searchFields:[{ key:'keyword', labelKey:'mdm.search.keyword', type:'text', placeholderKey:'mdm.search.placeholder' }],
    toolbarActions:[{ key:'create', labelKey:'common.actions.create', type:'primary', requirePermission:MDM_PERMISSIONS.recordCreate, onClick:() => setOpen(true) },{key:'excelFileImport',labelKey:'mdm.actions.excelFileImport',type:'default',requirePermission:MDM_PERMISSIONS.recordCreate,onClick:()=>setFileImportOpen(true)},{key:'excelPaste',labelKey:'mdm.actions.excelPaste',type:'default',requirePermission:MDM_PERMISSIONS.recordCreate,onClick:()=>setPasteOpen(true)}],
    rowActions:[
      { key:'edit', labelKey:'common.actions.edit', requirePermission:MDM_PERMISSIONS.recordUpdate, hidden:record=>!['DRAFT','REJECTED'].includes(record.lifecycleStatus), onClick:edit },
      { key:'submit', labelKey:'mdm.actions.submitRecord', requirePermission:MDM_PERMISSIONS.recordSubmit, hidden:record=>!allowedMdmRecordActions(record.lifecycleStatus).includes('SUBMIT'), onClick:record=>setPendingAction({record,action:'SUBMIT'}) },
      { key:'approve', labelKey:'mdm.actions.approveRecord', requirePermission:MDM_PERMISSIONS.recordApprove, hidden:record=>!allowedMdmRecordActions(record.lifecycleStatus).includes('APPROVE'), onClick:record=>setPendingAction({record,action:'APPROVE'}) },
      { key:'reject', labelKey:'mdm.actions.rejectRecord', requirePermission:MDM_PERMISSIONS.recordApprove, hidden:record=>!allowedMdmRecordActions(record.lifecycleStatus).includes('REJECT'), onClick:record=>setPendingAction({record,action:'REJECT'}) },
      { key:'deactivate', labelKey:'mdm.actions.deactivateRecord', requirePermission:MDM_PERMISSIONS.recordDisable, hidden:record=>!allowedMdmRecordActions(record.lifecycleStatus).includes('DEACTIVATE'), onClick:record=>setPendingAction({record,action:'DEACTIVATE'}) },
      {key:'history',labelKey:'mdm.actions.versionHistory',requirePermission:MDM_PERMISSIONS.recordView,onClick:setHistoryRecord}
    ] };
  }, [code, schema.data?.fields, t]);
  const submit = (values:FormValues) => {
    const request:SaveMdmRecord = { businessCode:values.businessCode, name:values.name, lifecycleStatus:editing?.lifecycleStatus ?? 'DRAFT',
      scopeType:'GROUP', scopeIds:[], effectiveFrom:values.effectiveFrom?.format('YYYY-MM-DD'), effectiveTo:values.effectiveTo?.format('YYYY-MM-DD'),
      attributes:values.attributes ?? {}, expectedVersion:editing?.version, changeReason:editing ? t('mdm.changeReason.edit') : t('mdm.changeReason.create') };
    save.mutate({ id:editing?.id, request });
  };
  const actionMutation=useMdmRecordAction(code,()=>{message.success(t('common.feedback.operationSucceeded'));setPendingAction(undefined);actionForm.resetFields();});
  const context = { module:'mdm' as const, pageType:'workspace' as const, objectType:code, routePath:`/mdm/models/${code}/records`,
    visibleFields:['businessCode','name',...(schema.data?.fields.map((field) => field.code) ?? [])], availableActions:['search','create','edit'] };
  if (schema.isError) return <AppPageContainer title={t('mdm.records.title')}><Alert type="error" showIcon message={t('mdm.feedback.loadSchemaFailed')} /></AppPageContainer>;
  return <MdmPageContextProvider value={context}>
    {records.isError ? <AppPageContainer title={t('mdm.records.title')}><Alert type="error" showIcon message={t('mdm.feedback.loadFailed')} action={<Button onClick={() => records.refetch()}>{t('common.actions.retry')}</Button>} /></AppPageContainer>
      : <ConfigurableListPage definition={definition} loading={records.isLoading || schema.isLoading} dataSource={records.data?.records ?? []} total={records.data?.total ?? 0} pageNo={params.pageNo} pageSize={params.pageSize}
        onPageChange={(pageNo,pageSize) => setParams((current) => ({...current,pageNo,pageSize}))} onSearch={(query) => setParams((current) => ({...current,keyword:String(query.keyword ?? ''),pageNo:1}))}
        onReset={() => setParams((current) => ({...current,keyword:'',pageNo:1}))} onRefresh={() => records.refetch()} />}
    <FormInteractionSurface mode={formInteractionMode} open={open} title={`${editing ? t('common.actions.edit') : t('common.actions.create')} · ${schema.data?.name ?? ''}`} onCancel={close} onSubmit={() => form.submit()} confirmLoading={save.isPending} submitLabel={t('common.actions.save')} cancelLabel={t('common.actions.cancel')} width={iafSurfaceWidths[surfaceWidth]}>
      <Form form={form} layout="vertical" onFinish={submit} onFieldsChange={() => setDirty(true)} initialValues={{ attributes:{} }}>
        {save.isError && <Alert type="error" showIcon message={save.error.message} />}
        <Alert type="info" showIcon message={t('mdm.approval.currentStatus')} description={t(`mdm.status.${editing?.lifecycleStatus ?? 'DRAFT'}`)} style={{marginBottom:16}} />
        <Form.Item name="businessCode" label={t('mdm.fields.businessCode')} rules={[{required:true,message:t('common.validation.required')}]}><Input autoFocus /></Form.Item>
        <Form.Item name="name" label={t('mdm.fields.name')} rules={[{required:true,message:t('common.validation.required')}]}><Input /></Form.Item>
        {schema.data?.effectiveDateEnabled && <><Form.Item name="effectiveFrom" label={t('mdm.fields.effectiveFrom')}><DatePicker style={{width:'100%'}} /></Form.Item><Form.Item name="effectiveTo" label={t('mdm.fields.effectiveTo')}><DatePicker style={{width:'100%'}} /></Form.Item></>}
        {schema.data?.fields.map((field) => <Form.Item key={field.code} name={['attributes',field.code]} label={field.name} extra={field.helpText} valuePropName={field.dataType === 'BOOLEAN' ? 'checked' : 'value'} rules={[{required:field.required,message:t('common.validation.required')}]}>{fieldControl(field)}</Form.Item>)}
      </Form>
    </FormInteractionSurface>
    {schema.data&&<MdmExcelPasteModal open={pasteOpen} onClose={()=>setPasteOpen(false)} model={schema.data}/>}
    {schema.data&&<MdmExcelFileImportModal open={fileImportOpen} onClose={()=>setFileImportOpen(false)} model={schema.data}/>}
    <Modal open={Boolean(pendingAction)} title={pendingAction?t(`mdm.actions.${pendingAction.action.toLowerCase()}Record`):''} onCancel={()=>{setPendingAction(undefined);actionForm.resetFields();}} onOk={()=>actionForm.submit()} confirmLoading={actionMutation.isPending} okButtonProps={{danger:pendingAction?.action==='REJECT'||pendingAction?.action==='DEACTIVATE'}} destroyOnHidden>
      <Form form={actionForm} layout="vertical" onFinish={({comment})=>pendingAction&&actionMutation.mutate({id:pendingAction.record.id,action:pendingAction.action,comment:comment??''})}>
        {actionMutation.isError&&<Alert type="error" showIcon message={actionMutation.error.message} style={{marginBottom:16}}/>}
        <Alert type="info" showIcon message={`${pendingAction?.record.businessCode??''} · ${pendingAction?.record.name??''}`} description={pendingAction&&`${t(`mdm.status.${pendingAction.record.lifecycleStatus}`)} → ${t(`mdm.status.${pendingAction.action==='SUBMIT'?(schema.data?.approvalRequired?'PENDING_APPROVAL':'ACTIVE'):pendingAction.action==='APPROVE'?'ACTIVE':pendingAction.action==='REJECT'?'REJECTED':'INACTIVE'}`)}`} style={{marginBottom:16}}/>
        <Form.Item name="comment" label={t('mdm.approval.comment')} rules={pendingAction?.action==='REJECT'?[{required:true,message:t('common.validation.required')}]:undefined}><Input.TextArea rows={4} maxLength={500} showCount placeholder={t('mdm.approval.commentPlaceholder')}/></Form.Item>
      </Form>
    </Modal>
    <Drawer open={Boolean(historyRecord)} onClose={()=>setHistoryRecord(undefined)} title={`${historyRecord?.businessCode??''} · ${t('mdm.actions.versionHistory')}`} width={760} destroyOnHidden>
      <Typography.Title level={5}>{t('mdm.approval.history')}</Typography.Title>
      {actions.isError
        ? <Alert type="error" showIcon message={t('mdm.feedback.loadFailed')}/>
        : actions.isLoading
          ? <Typography.Text type="secondary">{t('common.feedback.loading')}</Typography.Text>
          : actions.data?.length
            ? <Timeline items={actions.data.map(action=>({color:action.action==='REJECT'||action.action==='DEACTIVATE'?'red':'green',children:<Space direction="vertical" size={2}><Space wrap><Tag>{t(`mdm.approval.action.${action.action}`)}</Tag><Typography.Text>{t(`mdm.status.${action.fromStatus}`)} → {t(`mdm.status.${action.toStatus}`)}</Typography.Text></Space><Typography.Text>{action.actorName}</Typography.Text><Typography.Text type="secondary">{dayjs(action.createdAt).format('YYYY-MM-DD HH:mm:ss')}</Typography.Text>{action.comment&&<Typography.Text>{action.comment}</Typography.Text>}</Space>}))}/>
            : <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description={t('mdm.approval.noHistory')}/>
      }
      <Divider/><Typography.Title level={5}>{t('mdm.approval.versionHistory')}</Typography.Title>
      {versions.isError?<Alert type="error" showIcon message={t('mdm.feedback.loadFailed')}/>:versions.isLoading?<Typography.Text type="secondary">{t('common.feedback.loading')}</Typography.Text>:versions.data?.length?<Timeline items={versions.data.map((version,index)=>{const changes=buildMdmVersionDiff(version,versions.data?.[index+1]);return {children:<Space direction="vertical" size={8} style={{width:'100%'}}><Space wrap><Typography.Text strong>v{version.versionNo}</Typography.Text><Tag color={version.changeType==='CREATE'?'green':'blue'}>{version.changeType}</Tag><Typography.Text>{version.createdByName}</Typography.Text><Typography.Text type="secondary">{dayjs(version.createdAt).format('YYYY-MM-DD HH:mm:ss')}</Typography.Text></Space>{version.changeReason&&<Typography.Text type="secondary">变更原因：{version.changeReason}</Typography.Text>}<Table size="small" pagination={false} rowKey="key" dataSource={changes} columns={[{title:'变更字段',dataIndex:'key',width:180,render:(key:string)=>schema.data?.fields.find(field=>field.code===key)?.name??t(`mdm.fields.${key}`,{defaultValue:key})},{title:'变更前',dataIndex:'before',render:displayValue},{title:'变更后',dataIndex:'after',render:displayValue}]}/></Space>};})}/>:<Empty description="暂无版本历史"/>}
    </Drawer>
  </MdmPageContextProvider>;
};
