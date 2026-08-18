import { AppPageContainer,StatusTag } from '@iaf/ui-core';
import { MDM_PERMISSIONS, PermissionButton } from '@iaf/permissions';
import { Alert,Button,Card,Checkbox,Descriptions,Empty,Form,Input,List,Modal,Select,Space,Typography } from 'antd';
import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';
import { useCreateMdmModel,useMdmModels } from './hooks';
import { MdmPageContextProvider } from './pageContext';
import type { CreateMdmModel } from './types';
import { MdmModelDictionaryImportModal } from './MdmModelDictionaryImportModal';

export const MdmModelCenterPage=()=>{
  const{t}=useTranslation();const nav=useNavigate();const query=useMdmModels();const[open,setOpen]=useState(false);const[importOpen,setImportOpen]=useState(false);const[form]=Form.useForm<CreateMdmModel>();
  const create=useCreateMdmModel(code=>{setOpen(false);form.resetFields();nav(`/mdm/models/${code}/design`);});
  const domainOptions=[...new Set((query.data??[]).map(model=>model.domainCode))].map(value=>({value,label:value}));
  const openCreate=()=>{create.reset();form.setFieldsValue({domainCode:domainOptions[0]?.value,recordType:'MASTER',versionEnabled:true,effectiveDateEnabled:false,organizationScopeEnabled:false,approvalRequired:false});setOpen(true);};
  const context={module:'mdm' as const,pageType:'list' as const,objectType:'MasterModel',routePath:'/mdm/models',visibleFields:['code','name','domainCode','status','currentModelVersion'],availableActions:['createModel','importModelDictionary','designModel','openWorkspace']};
  return <MdmPageContextProvider value={context}>
    <AppPageContainer title={t('mdm.models.title')} extra={<Space><PermissionButton require={MDM_PERMISSIONS.modelView} onClick={()=>setImportOpen(true)}>{t('mdm.modelImport.action')}</PermissionButton><PermissionButton require={MDM_PERMISSIONS.modelCreate} type="primary" onClick={openCreate}>{t('mdm.actions.createModel')}</PermissionButton><Button onClick={()=>query.refetch()}>{t('common.actions.refresh')}</Button></Space>}>
      {query.isError?<Alert type="error" showIcon message={t('mdm.feedback.loadFailed')}/>:query.data?.length?<List grid={{gutter:16,xs:1,md:2,xl:3}} dataSource={query.data} loading={query.isLoading} renderItem={model=><List.Item><Card title={<Space><Typography.Text strong>{model.name}</Typography.Text><StatusTag status={model.status} label={t(`mdm.status.${model.status}`)}/></Space>} actions={[<Button type="link" key="design" onClick={()=>nav(`/mdm/models/${model.code}/design`)}>{t('mdm.actions.designModel')}</Button>,<Button type="link" key="open" disabled={model.status!=='PUBLISHED'} onClick={()=>nav(`/mdm/models/${model.code}/records`)}>{t('mdm.actions.openWorkspace')}</Button>]}><Descriptions size="small" column={1} items={[{key:'code',label:t('mdm.fields.modelCode'),children:model.code},{key:'domain',label:t('mdm.fields.domain'),children:model.domainCode},{key:'version',label:t('mdm.fields.modelVersion'),children:model.currentModelVersion},{key:'fields',label:t('mdm.fields.fieldCount'),children:model.fields.length}]}/></Card></List.Item>}/>:!query.isLoading&&<Empty description={t('common.feedback.empty')}/>}
    </AppPageContainer>
    <MdmModelDictionaryImportModal open={importOpen} onClose={()=>setImportOpen(false)}/>
    <Modal title={t('mdm.actions.createModel')} open={open} onCancel={()=>{setOpen(false);create.reset();}} onOk={()=>form.submit()} confirmLoading={create.isPending} destroyOnHidden>
      <Form form={form} layout="vertical" onFinish={values=>create.mutate(values)}>{create.isError&&<Alert type="error" showIcon message={create.error.message} style={{marginBottom:16}}/>}<Form.Item name="domainCode" label={t('mdm.fields.domain')} rules={[{required:true}]}><Select options={domainOptions} showSearch optionFilterProp="label"/></Form.Item><Form.Item name="code" label={t('mdm.fields.modelCode')} rules={[{required:true},{pattern:/^[a-z][A-Za-z0-9_]{1,63}$/}]}><Input/></Form.Item><Form.Item name="name" label={t('mdm.fields.name')} rules={[{required:true}]}><Input/></Form.Item><Form.Item name="recordType" label="记录类型"><Select options={[{value:'MASTER',label:'MASTER'},{value:'REFERENCE',label:'REFERENCE'}]}/></Form.Item><Space wrap><Form.Item name="versionEnabled" valuePropName="checked"><Checkbox>数据版本</Checkbox></Form.Item><Form.Item name="effectiveDateEnabled" valuePropName="checked"><Checkbox>生效日期</Checkbox></Form.Item><Form.Item name="organizationScopeEnabled" valuePropName="checked"><Checkbox>组织范围</Checkbox></Form.Item><Form.Item name="approvalRequired" valuePropName="checked"><Checkbox>记录生效需要审批</Checkbox></Form.Item></Space></Form>
    </Modal>
  </MdmPageContextProvider>;
};
