import { AppPageContainer } from '@iaf/ui-core';
import { MDM_PERMISSIONS, PermissionButton } from '@iaf/permissions';
import { Alert, Button, Card, Checkbox, Form, Input, InputNumber, Modal, Popconfirm, Select, Space, Table, Tag, Typography, message } from 'antd';
import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { mdmApi } from './api';
import { useMdmModels, useMdmSchema, usePublishMdmModel, useSaveMdmModelDraft } from './hooks';
import { useRolesQuery } from '../platform/roles/hooks';
import type { MdmDataType, MdmReferenceConfig, SaveMdmModelDraft } from './types';

const TYPES:MdmDataType[]=['STRING','TEXT','INTEGER','DECIMAL','BOOLEAN','DATE','DATETIME','ENUM','REFERENCE'];
type DesignerValues={approvalRequired:boolean;approvalRoleId?:number;modelApprovalRoleId:number;fields:SaveMdmModelDraft['fields'];uiSchemaText:string};

export const MdmModelDesignerPage=()=>{
  const {t}=useTranslation();
  const {modelCode=''}=useParams(); const navigate=useNavigate(); const query=useMdmSchema(modelCode); const [form]=Form.useForm<DesignerValues>();
  const [referenceForm]=Form.useForm<MdmReferenceConfig>();const [referenceFieldIndex,setReferenceFieldIndex]=useState<number>();
  const referenceTarget=Form.useWatch('targetModelCode',referenceForm);
  const models=useMdmModels();
  const roles=useRolesQuery({pageNo:1,pageSize:200});
  const [editingPublished,setEditingPublished]=useState(false);
  const save=useSaveMdmModelDraft(modelCode,()=>message.success('模型草稿已保存'));
  const publish=usePublishMdmModel(modelCode,()=>{message.success(t('mdm.modelApproval.submitted'));navigate('/mdm/models');});
  useEffect(()=>{if(query.data){const approval=query.data.uiSchema.approval as {roleId?:number}|undefined;const{approval:_approval,...visibleUi}=query.data.uiSchema;form.setFieldsValue({approvalRequired:query.data.approvalRequired,approvalRoleId:approval?.roleId,modelApprovalRoleId:query.data.modelApprovalRoleId??undefined,fields:query.data.fields.map(({id:_,length,...field})=>({...field,maxLength:length})),uiSchemaText:JSON.stringify(visibleUi,null,2)});}},[query.data,form]);
  const submit=(values:DesignerValues)=>{let uiSchema:Record<string,unknown>;try{uiSchema=JSON.parse(values.uiSchemaText||'{}') as Record<string,unknown>;}catch{message.error('UI Schema 必须是有效 JSON');return;}if(values.approvalRequired)uiSchema.approval={roleId:values.approvalRoleId};save.mutate({approvalRequired:values.approvalRequired,modelApprovalRoleId:values.modelApprovalRoleId,fields:values.fields.map((field,index)=>({...field,sortNo:(index+1)*10,enumOptions:Array.isArray(field.enumOptions)?field.enumOptions:[]})),uiSchema});};
  if(query.isError)return <Alert type="error" showIcon message="模型定义加载失败"/>;
  const editable=query.data?.status==='DRAFT'||editingPublished;
  const modelOptions=(models.data??[]).map(model=>({value:model.code,label:`${model.name} (${model.code})`}));
  const targetFields=(targetCode?:string)=>{const target=models.data?.find(model=>model.code===targetCode);return [{code:'businessCode',name:'业务编码'},{code:'name',name:'名称'},{code:'lifecycleStatus',name:'状态'},...(target?.fields??[])].map(field=>({value:field.code,label:`${field.name} (${field.code})`}));};
  const openReference=(index:number)=>{referenceForm.setFieldsValue(form.getFieldValue(['fields',index,'referenceConfig'])??{targetModelCode:'',valueFieldCode:'businessCode',displayFieldCode:'name',statusFieldCode:'lifecycleStatus',allowedStatuses:['ACTIVE']});setReferenceFieldIndex(index);};
  const saveReference=async()=>{const value=await referenceForm.validateFields();form.setFieldValue(['fields',referenceFieldIndex!,'referenceConfig'],value);setReferenceFieldIndex(undefined);};
  return <AppPageContainer title={query.data?`${query.data.name} · 模型设计器`:'模型设计器'} extra={<Space><Button onClick={()=>navigate('/mdm/models')}>返回</Button>{query.data?.status==='PUBLISHED'&&!editingPublished&&<PermissionButton require={MDM_PERMISSIONS.modelUpdate} type="primary" onClick={()=>setEditingPublished(true)}>修改并创建新版本</PermissionButton>}{editable&&<><PermissionButton require={MDM_PERMISSIONS.modelUpdate} loading={save.isPending} onClick={()=>form.submit()}>保存草稿</PermissionButton>{query.data?.status==='DRAFT'&&<Popconfirm title={t('mdm.modelApproval.confirmSubmit',{version:query.data.currentModelVersion+1})} onConfirm={()=>publish.mutate()}><PermissionButton require={MDM_PERMISSIONS.modelPublish} type="primary" loading={publish.isPending}>{t('mdm.modelApproval.submit')}</PermissionButton></Popconfirm>}</>}</Space>}>
    {query.data?.status==='PUBLISHED'&&!editingPublished&&<Alert type="info" showIcon message={`版本 v${query.data.currentModelVersion} 已发布。点击“修改并创建新版本”进入编辑，保存后形成待发布草稿。`} style={{marginBottom:16}}/>}
    {query.data?.status==='PUBLISHED'&&editingPublished&&<Alert type="warning" showIcon message={`正在基于 v${query.data.currentModelVersion} 修改。请先保存草稿，再校验并发布新版本。`} style={{marginBottom:16}}/>}
    {query.data?.status==='DRAFT'&&query.data.currentModelVersion>0&&<Alert type="warning" showIcon message={`v${query.data.currentModelVersion+1} 草稿待发布；当前已发布版本为 v${query.data.currentModelVersion}。`} style={{marginBottom:16}}/>}
    {query.data?.status==='PENDING_APPROVAL'&&<Alert type="info" showIcon message={t('mdm.modelApproval.pending',{version:query.data.currentModelVersion+1})} style={{marginBottom:16}}/>}
    <Form form={form} layout="vertical" onFinish={submit} disabled={!editable}>
      <Card title={t('mdm.modelApproval.settingsTitle')}>
        <Typography.Paragraph type="secondary">{t('mdm.modelApproval.settingsHelp')}</Typography.Paragraph>
        <Form.Item name="modelApprovalRoleId" label={t('mdm.modelApproval.roleLabel')} rules={[{required:true,message:t('common.validation.required')}]} style={{maxWidth:520,marginBottom:0}}><Select showSearch optionFilterProp="label" loading={roles.isLoading} options={(roles.data?.records??[]).filter(role=>role.status!=='DISABLED').map(role=>({value:role.id,label:`${role.roleName} (${role.roleCode})`}))}/></Form.Item>
      </Card>
      <Card title={t('mdm.approval.settingsTitle')} style={{marginTop:16}}>
        <Form.Item name="approvalRequired" valuePropName="checked" style={{marginBottom:8}}><Checkbox>{t('mdm.approval.requiredLabel')}</Checkbox></Form.Item>
        <Typography.Paragraph type="secondary" style={{marginBottom:0}}>{t('mdm.approval.requiredHelp')}</Typography.Paragraph>
        <Form.Item noStyle shouldUpdate={(previous,current)=>previous.approvalRequired!==current.approvalRequired}>{({getFieldValue})=>getFieldValue('approvalRequired')?<Form.Item name="approvalRoleId" label={t('mdm.approval.roleLabel')} extra={t('mdm.approval.roleHelp')} rules={[{required:true,message:t('common.validation.required')}]} style={{maxWidth:520,marginTop:16,marginBottom:0}}><Select showSearch optionFilterProp="label" loading={roles.isLoading} options={(roles.data?.records??[]).filter(role=>role.status!=='DISABLED').map(role=>({value:role.id,label:`${role.roleName} (${role.roleCode})`}))}/></Form.Item>:null}</Form.Item>
      </Card>
      <Card title="字段定义" style={{marginTop:16}} extra={editable&&<Form.List name="fields">{(_,operations)=><Button onClick={()=>operations.add({code:'',name:'',dataType:'STRING',required:false,unique:false,readonly:false,searchable:true,sortable:false,listVisible:true,maxLength:null,enumOptions:[],helpText:'',sortNo:0})}>新增字段</Button>}</Form.List>}>
        <Form.List name="fields">{(fields,{remove})=><Table pagination={false} rowKey="key" dataSource={fields} columns={[
          {title:'字段编码',render:(_,field)=><Form.Item name={[field.name,'code']} rules={[{required:true},{pattern:/^[a-z][A-Za-z0-9_]{1,63}$/}]} noStyle><Input aria-label="字段编码"/></Form.Item>},
          {title:'名称',render:(_,field)=><Form.Item name={[field.name,'name']} rules={[{required:true}]} noStyle><Input aria-label="字段名称"/></Form.Item>},
          {title:'类型',width:140,render:(_,field)=><Form.Item name={[field.name,'dataType']} noStyle><Select aria-label="数据类型" options={TYPES.map(value=>({value,label:value}))}/></Form.Item>},
          {title:'长度',width:100,render:(_,field)=><Form.Item name={[field.name,'maxLength']} noStyle><InputNumber aria-label="最大长度" min={1}/></Form.Item>},
          {title:'属性',width:250,render:(_,field)=><Space wrap><Form.Item name={[field.name,'required']} valuePropName="checked" noStyle><Checkbox>必填</Checkbox></Form.Item><Form.Item name={[field.name,'unique']} valuePropName="checked" noStyle><Checkbox>唯一</Checkbox></Form.Item><Form.Item name={[field.name,'searchable']} valuePropName="checked" noStyle><Checkbox>可搜索</Checkbox></Form.Item><Form.Item name={[field.name,'listVisible']} valuePropName="checked" noStyle><Checkbox>列表展示</Checkbox></Form.Item></Space>},
          {title:'类型配置',width:280,render:(_,field)=><Form.Item noStyle shouldUpdate>{({getFieldValue})=>{const type=getFieldValue(['fields',field.name,'dataType']);if(type==='ENUM')return <Form.Item name={[field.name,'enumOptions']} rules={[{required:true,message:'枚举字段至少录入一个选项'}]} noStyle><Select aria-label="枚举选项" mode="tags" tokenSeparators={[',','，']} placeholder="输入选项后按 Enter" options={[]}/></Form.Item>;if(type!=='REFERENCE')return <Typography.Text type="secondary">无额外配置</Typography.Text>;const config=getFieldValue(['fields',field.name,'referenceConfig']) as MdmReferenceConfig|undefined;return <Space direction="vertical" size={6}><Space wrap size={4}>{config?.targetModelCode?<><Tag>{config.targetModelCode}</Tag><Typography.Text type="secondary">{config.valueFieldCode} → {config.displayFieldCode}</Typography.Text></>:<Typography.Text type="warning">{t('mdm.reference.notConfigured')}</Typography.Text>}</Space><Button onClick={()=>openReference(field.name)} disabled={!editable}>{t('mdm.reference.configure')}</Button></Space>}}</Form.Item>},
          {title:'操作',width:70,render:(_,field)=><Button danger type="link" onClick={()=>remove(field.name)}>删除</Button>}
        ]}/>}</Form.List>
      </Card>
      <Card title="UI Schema" style={{marginTop:16}}><Typography.Paragraph type="secondary">表单分区与列表列定义，使用 JSON 保存并随模型版本固化。</Typography.Paragraph><Form.Item name="uiSchemaText" rules={[{required:true}]}><Input.TextArea aria-label="UI Schema" autoSize={{minRows:8,maxRows:18}} style={{fontFamily:'monospace'}}/></Form.Item></Card>
    </Form>
    <Modal title={t('mdm.reference.dialogTitle')} open={referenceFieldIndex!==undefined} onCancel={()=>setReferenceFieldIndex(undefined)} onOk={saveReference} okText={t('common.actions.confirm')} cancelText={t('common.actions.cancel')} width={640} destroyOnHidden>
      <Alert type="info" showIcon message={t('mdm.reference.dialogHelp')} style={{marginBottom:16}}/>
      <Form form={referenceForm} layout="vertical" preserve={false}>
        <Form.Item name="targetModelCode" label={t('mdm.reference.targetModel')} rules={[{required:true,message:t('common.validation.required')}]}><Select showSearch optionFilterProp="label" options={modelOptions} onChange={()=>referenceForm.setFieldsValue({valueFieldCode:undefined,displayFieldCode:undefined,statusFieldCode:undefined})}/></Form.Item>
        <Form.Item name="valueFieldCode" label={t('mdm.reference.valueField')} extra={t('mdm.reference.valueFieldHelp')} rules={[{required:true,message:t('common.validation.required')}]}><Select showSearch optionFilterProp="label" options={targetFields(referenceTarget)}/></Form.Item>
        <Form.Item name="displayFieldCode" label={t('mdm.reference.displayField')} extra={t('mdm.reference.displayFieldHelp')} rules={[{required:true,message:t('common.validation.required')}]}><Select showSearch optionFilterProp="label" options={targetFields(referenceTarget)}/></Form.Item>
        <Form.Item name="statusFieldCode" label={t('mdm.reference.statusField')} extra={t('mdm.reference.statusFieldHelp')}><Select allowClear showSearch optionFilterProp="label" options={targetFields(referenceTarget)}/></Form.Item>
        <Form.Item name="allowedStatuses" label={t('mdm.reference.allowedStatuses')} extra={t('mdm.reference.allowedStatusesHelp')}><Select mode="tags" tokenSeparators={[',','，']} options={[]}/></Form.Item>
      </Form>
    </Modal>
  </AppPageContainer>;
};
