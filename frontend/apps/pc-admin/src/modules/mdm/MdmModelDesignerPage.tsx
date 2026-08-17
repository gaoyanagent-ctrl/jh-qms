import { AppPageContainer } from '@iaf/ui-core';
import { MDM_PERMISSIONS, PermissionButton } from '@iaf/permissions';
import { Alert, Button, Card, Checkbox, Form, Input, InputNumber, Popconfirm, Select, Space, Table, Typography, message } from 'antd';
import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { mdmApi } from './api';
import { useMdmModels, useMdmSchema, usePublishMdmModel, useSaveMdmModelDraft } from './hooks';
import type { MdmDataType, SaveMdmModelDraft } from './types';

const TYPES:MdmDataType[]=['STRING','TEXT','INTEGER','DECIMAL','BOOLEAN','DATE','DATETIME','ENUM','REFERENCE'];
type DesignerValues={fields:SaveMdmModelDraft['fields'];uiSchemaText:string};

export const MdmModelDesignerPage=()=>{
  const {modelCode=''}=useParams(); const navigate=useNavigate(); const query=useMdmSchema(modelCode); const [form]=Form.useForm<DesignerValues>();
  const models=useMdmModels();
  const [editingPublished,setEditingPublished]=useState(false);
  const save=useSaveMdmModelDraft(modelCode,()=>message.success('模型草稿已保存'));
  const publish=usePublishMdmModel(modelCode,()=>{message.success('模型已发布');navigate('/mdm/models');});
  useEffect(()=>{if(query.data)form.setFieldsValue({fields:query.data.fields.map(({id:_,length,...field})=>({...field,maxLength:length})),uiSchemaText:JSON.stringify(query.data.uiSchema,null,2)});},[query.data,form]);
  const submit=(values:DesignerValues)=>{let uiSchema:Record<string,unknown>;try{uiSchema=JSON.parse(values.uiSchemaText||'{}') as Record<string,unknown>;}catch{message.error('UI Schema 必须是有效 JSON');return;}save.mutate({fields:values.fields.map((field,index)=>({...field,sortNo:(index+1)*10,enumOptions:Array.isArray(field.enumOptions)?field.enumOptions:[]})),uiSchema});};
  if(query.isError)return <Alert type="error" showIcon message="模型定义加载失败"/>;
  const editable=query.data?.status==='DRAFT'||editingPublished;
  const modelOptions=(models.data??[]).map(model=>({value:model.code,label:`${model.name} (${model.code})`}));
  const targetFields=(targetCode?:string)=>{const target=models.data?.find(model=>model.code===targetCode);return [{code:'businessCode',name:'业务编码'},{code:'name',name:'名称'},{code:'lifecycleStatus',name:'状态'},...(target?.fields??[])].map(field=>({value:field.code,label:`${field.name} (${field.code})`}));};
  return <AppPageContainer title={query.data?`${query.data.name} · 模型设计器`:'模型设计器'} extra={<Space><Button onClick={()=>navigate('/mdm/models')}>返回</Button>{query.data?.status==='PUBLISHED'&&!editingPublished&&<PermissionButton require={MDM_PERMISSIONS.modelUpdate} type="primary" onClick={()=>setEditingPublished(true)}>修改并创建新版本</PermissionButton>}{editable&&<><PermissionButton require={MDM_PERMISSIONS.modelUpdate} loading={save.isPending} onClick={()=>form.submit()}>保存草稿</PermissionButton>{query.data?.status==='DRAFT'&&<Popconfirm title={`确认将草稿发布为 v${query.data.currentModelVersion+1}？`} onConfirm={()=>publish.mutate()}><PermissionButton require={MDM_PERMISSIONS.modelPublish} type="primary" loading={publish.isPending}>校验并发布</PermissionButton></Popconfirm>}</>}</Space>}>
    {query.data?.status==='PUBLISHED'&&!editingPublished&&<Alert type="info" showIcon message={`版本 v${query.data.currentModelVersion} 已发布。点击“修改并创建新版本”进入编辑，保存后形成待发布草稿。`} style={{marginBottom:16}}/>}
    {query.data?.status==='PUBLISHED'&&editingPublished&&<Alert type="warning" showIcon message={`正在基于 v${query.data.currentModelVersion} 修改。请先保存草稿，再校验并发布新版本。`} style={{marginBottom:16}}/>}
    {query.data?.status==='DRAFT'&&query.data.currentModelVersion>0&&<Alert type="warning" showIcon message={`v${query.data.currentModelVersion+1} 草稿待发布；当前已发布版本为 v${query.data.currentModelVersion}。`} style={{marginBottom:16}}/>}
    <Form form={form} layout="vertical" onFinish={submit} disabled={!editable}>
      <Card title="字段定义" extra={editable&&<Form.List name="fields">{(_,operations)=><Button onClick={()=>operations.add({code:'',name:'',dataType:'STRING',required:false,unique:false,readonly:false,searchable:true,sortable:false,listVisible:true,maxLength:null,enumOptions:[],helpText:'',sortNo:0})}>新增字段</Button>}</Form.List>}>
        <Form.List name="fields">{(fields,{remove})=><Table pagination={false} rowKey="key" dataSource={fields} columns={[
          {title:'字段编码',render:(_,field)=><Form.Item name={[field.name,'code']} rules={[{required:true},{pattern:/^[a-z][A-Za-z0-9_]{1,63}$/}]} noStyle><Input aria-label="字段编码"/></Form.Item>},
          {title:'名称',render:(_,field)=><Form.Item name={[field.name,'name']} rules={[{required:true}]} noStyle><Input aria-label="字段名称"/></Form.Item>},
          {title:'类型',width:140,render:(_,field)=><Form.Item name={[field.name,'dataType']} noStyle><Select aria-label="数据类型" options={TYPES.map(value=>({value,label:value}))}/></Form.Item>},
          {title:'长度',width:100,render:(_,field)=><Form.Item name={[field.name,'maxLength']} noStyle><InputNumber aria-label="最大长度" min={1}/></Form.Item>},
          {title:'属性',width:250,render:(_,field)=><Space wrap><Form.Item name={[field.name,'required']} valuePropName="checked" noStyle><Checkbox>必填</Checkbox></Form.Item><Form.Item name={[field.name,'unique']} valuePropName="checked" noStyle><Checkbox>唯一</Checkbox></Form.Item><Form.Item name={[field.name,'searchable']} valuePropName="checked" noStyle><Checkbox>可搜索</Checkbox></Form.Item><Form.Item name={[field.name,'listVisible']} valuePropName="checked" noStyle><Checkbox>列表展示</Checkbox></Form.Item></Space>},
          {title:'类型配置',width:360,render:(_,field)=><Form.Item noStyle shouldUpdate>{({getFieldValue})=>{const type=getFieldValue(['fields',field.name,'dataType']);if(type==='ENUM')return <Form.Item name={[field.name,'enumOptions']} rules={[{required:true,message:'枚举字段至少录入一个选项'}]} noStyle><Select aria-label="枚举选项" mode="tags" tokenSeparators={[',','，']} placeholder="输入选项后按 Enter" options={[]}/></Form.Item>;if(type!=='REFERENCE')return <Typography.Text type="secondary">无额外配置</Typography.Text>;const target=getFieldValue(['fields',field.name,'referenceConfig','targetModelCode']);const options=targetFields(target);return <Space direction="vertical" size={8} style={{width:'100%'}}><Form.Item name={[field.name,'referenceConfig','targetModelCode']} rules={[{required:true,message:'请选择关联模型'}]} noStyle><Select aria-label="关联模型" placeholder="关联模型" showSearch optionFilterProp="label" options={modelOptions}/></Form.Item><Space.Compact block><Form.Item name={[field.name,'referenceConfig','valueFieldCode']} rules={[{required:true,message:'请选择值字段'}]} noStyle><Select aria-label="引用值字段" placeholder="值字段" options={options}/></Form.Item><Form.Item name={[field.name,'referenceConfig','displayFieldCode']} rules={[{required:true,message:'请选择显示字段'}]} noStyle><Select aria-label="引用显示字段" placeholder="显示字段" options={options}/></Form.Item></Space.Compact><Space.Compact block><Form.Item name={[field.name,'referenceConfig','statusFieldCode']} noStyle><Select aria-label="引用状态字段" allowClear placeholder="状态字段（可选）" options={options}/></Form.Item><Form.Item name={[field.name,'referenceConfig','allowedStatuses']} noStyle><Select aria-label="允许的引用状态" mode="tags" placeholder="允许状态" options={[]}/></Form.Item></Space.Compact><Typography.Text type="secondary">保存值字段，表单中显示名称字段。</Typography.Text></Space>}}</Form.Item>},
          {title:'操作',width:70,render:(_,field)=><Button danger type="link" onClick={()=>remove(field.name)}>删除</Button>}
        ]}/>}</Form.List>
      </Card>
      <Card title="UI Schema" style={{marginTop:16}}><Typography.Paragraph type="secondary">表单分区与列表列定义，使用 JSON 保存并随模型版本固化。</Typography.Paragraph><Form.Item name="uiSchemaText" rules={[{required:true}]}><Input.TextArea aria-label="UI Schema" autoSize={{minRows:8,maxRows:18}} style={{fontFamily:'monospace'}}/></Form.Item></Card>
    </Form>
  </AppPageContainer>;
};
