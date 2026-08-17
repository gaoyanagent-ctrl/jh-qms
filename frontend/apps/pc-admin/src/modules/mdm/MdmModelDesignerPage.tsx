import { AppPageContainer } from '@iaf/ui-core';
import { MDM_PERMISSIONS, PermissionButton } from '@iaf/permissions';
import { Alert, Button, Card, Checkbox, Form, Input, InputNumber, Popconfirm, Select, Space, Table, Typography, message } from 'antd';
import { useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { mdmApi } from './api';
import { useMdmSchema, usePublishMdmModel, useSaveMdmModelDraft } from './hooks';
import type { MdmDataType, SaveMdmModelDraft } from './types';

const TYPES:MdmDataType[]=['STRING','TEXT','INTEGER','DECIMAL','BOOLEAN','DATE','DATETIME','ENUM'];
type DesignerValues={fields:SaveMdmModelDraft['fields'];uiSchemaText:string};

export const MdmModelDesignerPage=()=>{
  const {modelCode=''}=useParams(); const navigate=useNavigate(); const query=useMdmSchema(modelCode); const [form]=Form.useForm<DesignerValues>();
  const save=useSaveMdmModelDraft(modelCode,()=>message.success('模型草稿已保存'));
  const publish=usePublishMdmModel(modelCode,()=>{message.success('模型已发布');navigate('/mdm/models');});
  useEffect(()=>{if(query.data)form.setFieldsValue({fields:query.data.fields.map(({id:_,length,...field})=>({...field,maxLength:length})),uiSchemaText:JSON.stringify(query.data.uiSchema,null,2)});},[query.data,form]);
  const submit=(values:DesignerValues)=>{let uiSchema:Record<string,unknown>;try{uiSchema=JSON.parse(values.uiSchemaText||'{}') as Record<string,unknown>;}catch{message.error('UI Schema 必须是有效 JSON');return;}save.mutate({fields:values.fields.map((field,index)=>({...field,sortNo:(index+1)*10,enumOptions:Array.isArray(field.enumOptions)?field.enumOptions:[]})),uiSchema});};
  if(query.isError)return <Alert type="error" showIcon message="模型定义加载失败"/>;
  const editable=query.data?.status==='DRAFT';
  return <AppPageContainer title={query.data?`${query.data.name} · 模型设计器`:'模型设计器'} extra={<Space><Button onClick={()=>navigate('/mdm/models')}>返回</Button>{editable&&<><PermissionButton require={MDM_PERMISSIONS.modelUpdate} loading={save.isPending} onClick={()=>form.submit()}>保存草稿</PermissionButton><Popconfirm title="发布后本版本定义将不可编辑，确认发布？" onConfirm={()=>publish.mutate()}><PermissionButton require={MDM_PERMISSIONS.modelPublish} type="primary" loading={publish.isPending}>校验并发布</PermissionButton></Popconfirm></>}</Space>}>
    {!editable&&query.data&&<Alert type="info" showIcon message={`版本 v${query.data.currentModelVersion} 已发布，当前为只读定义。`} style={{marginBottom:16}}/>}
    <Form form={form} layout="vertical" onFinish={submit} disabled={!editable}>
      <Card title="字段定义" extra={editable&&<Form.List name="fields">{(_,operations)=><Button onClick={()=>operations.add({code:'',name:'',dataType:'STRING',required:false,unique:false,readonly:false,searchable:true,sortable:false,listVisible:true,maxLength:null,enumOptions:[],helpText:'',sortNo:0})}>新增字段</Button>}</Form.List>}>
        <Form.List name="fields">{(fields,{remove})=><Table pagination={false} rowKey="key" dataSource={fields} columns={[
          {title:'字段编码',render:(_,field)=><Form.Item name={[field.name,'code']} rules={[{required:true},{pattern:/^[a-z][A-Za-z0-9_]{1,63}$/}]} noStyle><Input aria-label="字段编码"/></Form.Item>},
          {title:'名称',render:(_,field)=><Form.Item name={[field.name,'name']} rules={[{required:true}]} noStyle><Input aria-label="字段名称"/></Form.Item>},
          {title:'类型',width:140,render:(_,field)=><Form.Item name={[field.name,'dataType']} noStyle><Select aria-label="数据类型" options={TYPES.map(value=>({value,label:value}))}/></Form.Item>},
          {title:'长度',width:100,render:(_,field)=><Form.Item name={[field.name,'maxLength']} noStyle><InputNumber aria-label="最大长度" min={1}/></Form.Item>},
          {title:'属性',width:250,render:(_,field)=><Space wrap><Form.Item name={[field.name,'required']} valuePropName="checked" noStyle><Checkbox>必填</Checkbox></Form.Item><Form.Item name={[field.name,'unique']} valuePropName="checked" noStyle><Checkbox>唯一</Checkbox></Form.Item><Form.Item name={[field.name,'searchable']} valuePropName="checked" noStyle><Checkbox>可搜索</Checkbox></Form.Item><Form.Item name={[field.name,'listVisible']} valuePropName="checked" noStyle><Checkbox>列表展示</Checkbox></Form.Item></Space>},
          {title:'枚举选项',render:(_,field)=><Form.Item name={[field.name,'enumOptions']} noStyle><Select aria-label="枚举选项" mode="tags" tokenSeparators={[',']} open={false}/></Form.Item>},
          {title:'操作',width:70,render:(_,field)=><Button danger type="link" onClick={()=>remove(field.name)}>删除</Button>}
        ]}/>}</Form.List>
      </Card>
      <Card title="UI Schema" style={{marginTop:16}}><Typography.Paragraph type="secondary">表单分区与列表列定义，使用 JSON 保存并随模型版本固化。</Typography.Paragraph><Form.Item name="uiSchemaText" rules={[{required:true}]}><Input.TextArea aria-label="UI Schema" autoSize={{minRows:8,maxRows:18}} style={{fontFamily:'monospace'}}/></Form.Item></Card>
    </Form>
  </AppPageContainer>;
};
