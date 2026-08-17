import { MDM_PERMISSIONS, PermissionButton } from '@iaf/permissions';
import { AppPageContainer, IafSectionHeader, IafSurface, StatusTag } from '@iaf/ui-core';
import { Alert, App, Button, Descriptions, Drawer, Empty, Form, Input, Modal, Space, Table, Tabs, Tag } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import dayjs from 'dayjs';
import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useMdmApprovalTasks, useMdmModelApprovalTasks, useMdmModelPublishAction, useMdmRecordAction } from '../../mdm/hooks';
import type { MdmApprovalTaskScope } from '../../mdm/types';

type Decision='APPROVE'|'REJECT';
type UnifiedTask={key:string;type:'MODEL'|'RECORD';modelCode:string;modelName:string;title:string;subtitle:string;status:string;submittedByName:string;submittedAt:string;recordId?:string;targetVersion?:number};

export const ApprovalTaskCenterPage=()=>{
  const{t}=useTranslation();const{message}=App.useApp();const[scope,setScope]=useState<MdmApprovalTaskScope>('TODO');
  const[selected,setSelected]=useState<UnifiedTask>();const[action,setAction]=useState<Decision>();const[actionForm]=Form.useForm<{comment:string}>();
  const records=useMdmApprovalTasks(scope);const models=useMdmModelApprovalTasks(scope);
  const done=()=>{message.success(t('common.feedback.operationSucceeded'));setAction(undefined);setSelected(undefined);actionForm.resetFields();records.refetch();models.refetch();};
  const recordMutation=useMdmRecordAction(selected?.modelCode??'',done);const modelMutation=useMdmModelPublishAction(selected?.modelCode??'',done);
  const data:UnifiedTask[]=[...(models.data??[]).map(item=>({key:`MODEL:${item.modelId}`,type:'MODEL' as const,modelCode:item.modelCode,modelName:item.modelName,title:item.modelName,subtitle:`${t('mdm.modelApproval.targetVersion')} v${item.targetVersion}`,status:item.approvalStatus,submittedByName:item.submittedByName,submittedAt:item.submittedAt,targetVersion:item.targetVersion})),...(records.data??[]).map(item=>({key:`RECORD:${item.recordId}`,type:'RECORD' as const,modelCode:item.modelCode,modelName:item.modelName,title:item.recordName,subtitle:item.businessCode,status:item.lifecycleStatus,submittedByName:item.submittedByName,submittedAt:item.submittedAt,recordId:item.recordId}))].sort((a,b)=>b.submittedAt.localeCompare(a.submittedAt));
  const columns:ColumnsType<UnifiedTask>=[
    {title:t('mdm.approval.task.type'),dataIndex:'type',width:110,render:value=><Tag color={value==='MODEL'?'purple':'blue'}>{t(`mdm.approval.task.type${value}`)}</Tag>},
    {title:t('mdm.approval.task.object'),dataIndex:'title',render:(value,row)=><Space direction="vertical" size={0}><span>{value}</span><Tag>{row.subtitle}</Tag></Space>},
    {title:t('mdm.approval.task.model'),dataIndex:'modelName',width:180},
    {title:t('mdm.approval.task.requester'),dataIndex:'submittedByName',width:150},
    {title:t('common.fields.createdAt'),dataIndex:'submittedAt',width:180,render:value=>dayjs(value).format('YYYY-MM-DD HH:mm:ss')},
    {title:t('common.fields.status'),dataIndex:'status',width:140,render:value=><StatusTag status={value} label={t(`mdm.status.${value}`)}/>},
    {title:t('common.fields.actions'),width:90,fixed:'right',render:(_,row)=><Button type="link" onClick={()=>setSelected(row)}>{t('common.actions.view')}</Button>}
  ];
  const decide=(next:Decision)=>{setAction(next);actionForm.resetFields();};const pending=recordMutation.isPending||modelMutation.isPending;const failure=recordMutation.error??modelMutation.error;
  const submitDecision=({comment}:{comment:string})=>{if(!selected||!action)return;if(selected.type==='MODEL')modelMutation.mutate({action,comment:comment??''});else recordMutation.mutate({id:selected.recordId!,action,comment:comment??''});};
  return <AppPageContainer title={t('approval.title')} extra={scope==='TODO'?<Tag color="processing">{t('approval.todoCount',{count:data.length})}</Tag>:undefined}>
    <IafSurface title={<IafSectionHeader title={t('approval.listTitle')} description={t('mdm.approval.task.description')}/> }>
      <Tabs activeKey={scope} onChange={key=>setScope(key as MdmApprovalTaskScope)} items={[{key:'TODO',label:t('approval.tabs.todo')},{key:'DONE',label:t('approval.tabs.done')},{key:'STARTED',label:t('approval.tabs.started')}]}/>
      {records.isError||models.isError?<Alert type="error" showIcon message={t('mdm.feedback.loadFailed')} action={<Button onClick={()=>{records.refetch();models.refetch();}}>{t('common.actions.retry')}</Button>}/>:<Table rowKey="key" size="small" columns={columns} dataSource={data} loading={records.isLoading||models.isLoading} locale={{emptyText:<Empty description={t('common.feedback.empty')}/>}} scroll={{x:'max-content'}}/>}
    </IafSurface>
    <Drawer open={Boolean(selected)} onClose={()=>setSelected(undefined)} title={selected?.title} width={720} footer={selected&&scope==='TODO'?<Space style={{width:'100%',justifyContent:'flex-end'}}><PermissionButton require={selected.type==='MODEL'?MDM_PERMISSIONS.modelApprove:MDM_PERMISSIONS.recordApprove} danger onClick={()=>decide('REJECT')}>{t('mdm.actions.rejectRecord')}</PermissionButton><PermissionButton require={selected.type==='MODEL'?MDM_PERMISSIONS.modelApprove:MDM_PERMISSIONS.recordApprove} type="primary" onClick={()=>decide('APPROVE')}>{t('mdm.actions.approveRecord')}</PermissionButton></Space>:undefined}>
      {selected&&<Descriptions bordered column={1} size="small" items={[{key:'type',label:t('mdm.approval.task.type'),children:t(`mdm.approval.task.type${selected.type}`)},{key:'model',label:t('mdm.approval.task.model'),children:`${selected.modelName} (${selected.modelCode})`},{key:'object',label:t('mdm.approval.task.object'),children:selected.subtitle},{key:'requester',label:t('mdm.approval.task.requester'),children:selected.submittedByName},{key:'time',label:t('common.fields.createdAt'),children:dayjs(selected.submittedAt).format('YYYY-MM-DD HH:mm:ss')},{key:'status',label:t('common.fields.status'),children:<StatusTag status={selected.status} label={t(`mdm.status.${selected.status}`)}/>} ]}/>}
    </Drawer>
    <Modal open={Boolean(action)} title={action==='APPROVE'?t('mdm.actions.approveRecord'):t('mdm.actions.rejectRecord')} onCancel={()=>setAction(undefined)} onOk={()=>actionForm.submit()} confirmLoading={pending} okButtonProps={{danger:action==='REJECT'}} destroyOnHidden>
      <Form form={actionForm} layout="vertical" onFinish={submitDecision}>{failure&&<Alert type="error" showIcon message={failure.message} style={{marginBottom:16}}/>}<Form.Item name="comment" label={t('mdm.approval.comment')} rules={action==='REJECT'?[{required:true,message:t('common.validation.required')}]:undefined}><Input.TextArea rows={4} maxLength={500} showCount/></Form.Item></Form>
    </Modal>
  </AppPageContainer>;
};
