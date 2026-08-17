import { AppPageContainer, IafSectionHeader, IafSurface, StatusTag } from '@iaf/ui-core';
import { Alert, App, Button, Descriptions, Drawer, Empty, Form, Input, Modal, Space, Table, Tabs, Tag, Typography } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import dayjs from 'dayjs';
import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useMdmApprovalTasks, useMdmRecordAction } from '../../mdm/hooks';
import type { MdmApprovalTask, MdmApprovalTaskScope, MdmRecordActionType } from '../../mdm/types';

export const ApprovalTaskCenterPage=()=>{
  const{t}=useTranslation();const{message}=App.useApp();
  const[scope,setScope]=useState<MdmApprovalTaskScope>('TODO');const[selected,setSelected]=useState<MdmApprovalTask>();
  const[action,setAction]=useState<MdmRecordActionType>();const[actionForm]=Form.useForm<{comment:string}>();
  const tasks=useMdmApprovalTasks(scope);
  const mutation=useMdmRecordAction(selected?.modelCode??'',()=>{message.success(t('common.feedback.operationSucceeded'));setAction(undefined);setSelected(undefined);actionForm.resetFields();tasks.refetch();});
  const columns:ColumnsType<MdmApprovalTask>=[
    {title:t('mdm.approval.task.businessCode'),dataIndex:'businessCode',width:170},
    {title:t('mdm.approval.task.recordName'),dataIndex:'recordName'},
    {title:t('mdm.approval.task.model'),dataIndex:'modelName',width:160,render:(value,record)=><Space><Typography.Text>{value}</Typography.Text><Tag>{record.modelCode}</Tag></Space>},
    {title:t('mdm.approval.task.requester'),dataIndex:'submittedByName',width:150},
    {title:t('common.fields.createdAt'),dataIndex:'submittedAt',width:180,render:value=>dayjs(value).format('YYYY-MM-DD HH:mm:ss')},
    {title:t('common.fields.status'),dataIndex:'lifecycleStatus',width:130,render:value=><StatusTag status={value} label={t(`mdm.status.${value}`)}/>},
    {title:t('common.fields.actions'),width:90,fixed:'right',render:(_,record)=><Button type="link" onClick={()=>setSelected(record)}>{t('common.actions.view')}</Button>}
  ];
  const decide=(next:MdmRecordActionType)=>{setAction(next);actionForm.resetFields();};
  return <AppPageContainer title={t('approval.title')} extra={scope==='TODO'?<Tag color="processing">{t('approval.todoCount',{count:tasks.data?.length??0})}</Tag>:undefined}>
    <IafSurface title={<IafSectionHeader title={t('approval.listTitle')} description={t('mdm.approval.task.description')}/>}>
      <Tabs activeKey={scope} onChange={key=>setScope(key as MdmApprovalTaskScope)} items={[{key:'TODO',label:t('approval.tabs.todo')},{key:'DONE',label:t('approval.tabs.done')},{key:'STARTED',label:t('approval.tabs.started')}]}/>
      {tasks.isError?<Alert type="error" showIcon message={t('mdm.feedback.loadFailed')} action={<Button onClick={()=>tasks.refetch()}>{t('common.actions.retry')}</Button>}/>:<Table rowKey="recordId" size="small" columns={columns} dataSource={tasks.data??[]} loading={tasks.isLoading} locale={{emptyText:<Empty description={t('common.feedback.empty')}/>}} scroll={{x:'max-content'}}/>}
    </IafSurface>
    <Drawer open={Boolean(selected)} onClose={()=>setSelected(undefined)} title={selected?`${selected.businessCode} · ${selected.recordName}`:''} width={720} footer={selected&&scope==='TODO'?<Space style={{width:'100%',justifyContent:'flex-end'}}><Button danger onClick={()=>decide('REJECT')}>{t('mdm.actions.rejectRecord')}</Button><Button type="primary" onClick={()=>decide('APPROVE')}>{t('mdm.actions.approveRecord')}</Button></Space>:undefined}>
      {selected&&<Descriptions bordered column={1} size="small" items={[
        {key:'model',label:t('mdm.approval.task.model'),children:`${selected.modelName} (${selected.modelCode})`},
        {key:'requester',label:t('mdm.approval.task.requester'),children:selected.submittedByName},
        {key:'time',label:t('common.fields.createdAt'),children:dayjs(selected.submittedAt).format('YYYY-MM-DD HH:mm:ss')},
        {key:'status',label:t('common.fields.status'),children:<StatusTag status={selected.lifecycleStatus} label={t(`mdm.status.${selected.lifecycleStatus}`)}/>}
      ]}/>}
    </Drawer>
    <Modal open={Boolean(action)} title={action==='APPROVE'?t('mdm.actions.approveRecord'):t('mdm.actions.rejectRecord')} onCancel={()=>setAction(undefined)} onOk={()=>actionForm.submit()} confirmLoading={mutation.isPending} okButtonProps={{danger:action==='REJECT'}} destroyOnHidden>
      <Form form={actionForm} layout="vertical" onFinish={({comment})=>selected&&action&&mutation.mutate({id:selected.recordId,action,comment:comment??''})}>
        {mutation.isError&&<Alert type="error" showIcon message={mutation.error.message} style={{marginBottom:16}}/>}
        <Form.Item name="comment" label={t('mdm.approval.comment')} rules={action==='REJECT'?[{required:true,message:t('common.validation.required')}]:undefined}><Input.TextArea rows={4} maxLength={500} showCount/></Form.Item>
      </Form>
    </Modal>
  </AppPageContainer>;
};
