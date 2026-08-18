import { Button, Card, Checkbox, Form, Input, Modal, Popconfirm, Select, Space, Table, Tag, Typography, message } from 'antd';
import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import type { MdmModel, MdmValidationRule } from './types';
import { useMdmValidationRules, useSaveMdmValidationRules } from './hooks';

type Condition={targetField:string;sourceField?:string;value?:string;sourceType:'FIELD'|'CONSTANT'};
type RuleForm={name:string;fieldCode:string;targetModel:string;triggerPoint:'SAVE'|'BLUR';severity:'BLOCK'|'WARNING';message:string;enabled:boolean;conditions:Condition[]};
const commonFields=[{code:'businessCode',name:'业务编码'},{code:'name',name:'名称'},{code:'lifecycleStatus',name:'状态'}];

export const toRuleForm=(rule:MdmValidationRule):RuleForm=>({name:rule.name,fieldCode:rule.fieldCode,targetModel:rule.assertion.targetModel,triggerPoint:rule.triggerPoint,severity:rule.severity,message:rule.message,enabled:rule.enabled,conditions:rule.assertion.conditions.map(item=>({targetField:item.targetField,sourceField:item.sourceField,value:item.value==null?undefined:String(item.value),sourceType:item.sourceField?'FIELD':'CONSTANT'}))});
export const toValidationRule=(value:RuleForm,index:number,existing?:MdmValidationRule):MdmValidationRule=>({id:existing?.id,code:existing?.code??`rule_${Date.now()}_${index}`,name:value.name,fieldCode:value.fieldCode,triggerPoint:value.triggerPoint,ruleType:'REFERENCE_EXISTS',severity:value.severity,message:value.message,condition:{},assertion:{targetModel:value.targetModel,conditions:value.conditions.map(({sourceType,targetField,sourceField,value})=>sourceType==='FIELD'?{targetField,sourceField}:{targetField,value})},enabled:value.enabled,sortNo:(index+1)*10});

export const MdmValidationRuleEditor=({model,models,editable}:{model:MdmModel;models:MdmModel[];editable:boolean})=>{
  const {t}=useTranslation();const query=useMdmValidationRules(model.code);const [rules,setRules]=useState<MdmValidationRule[]>([]);const [editingIndex,setEditingIndex]=useState<number>();const [form]=Form.useForm<RuleForm>();
  useEffect(()=>{if(query.data)setRules(query.data);},[query.data]);
  const sourceFields=[...commonFields,...model.fields].map(field=>({value:field.code,label:`${field.name} (${field.code})`}));
  const targetModel=Form.useWatch('targetModel',form);const target=models.find(item=>item.code===targetModel);const targetFields=[...commonFields,...(target?.fields??[])].map(field=>({value:field.code,label:`${field.name} (${field.code})`}));
  const save=useSaveMdmValidationRules(model.code,()=>message.success(t('mdm.validation.saved')));
  const open=(index?:number)=>{const existing=index===undefined?undefined:rules[index];form.setFieldsValue(existing?toRuleForm(existing):{triggerPoint:'BLUR',severity:'BLOCK',enabled:true,conditions:[{sourceType:'FIELD',sourceField:'',targetField:''}],name:'',fieldCode:'',targetModel:'',message:''});setEditingIndex(index??rules.length);};
  const confirm=async()=>{const value=await form.validateFields();setRules(current=>{const next=[...current];next[editingIndex!]=toValidationRule(value,editingIndex!,current[editingIndex!]);return next;});setEditingIndex(undefined);form.resetFields();};
  return <Card title={t('mdm.validation.title')} style={{marginTop:16}} extra={editable&&<Space><Button onClick={()=>open()}>{t('mdm.validation.add')}</Button><Button type="primary" loading={save.isPending} onClick={()=>save.mutate(rules)}>{t('mdm.validation.save')}</Button></Space>}>
    <Typography.Paragraph type="secondary">{t('mdm.validation.help')}</Typography.Paragraph>
    <Table loading={query.isLoading} pagination={false} rowKey={rule=>rule.code} dataSource={rules} columns={[
      {title:t('mdm.validation.ruleName'),dataIndex:'name'},
      {title:t('mdm.validation.currentField'),dataIndex:'fieldCode',render:(code:string)=>sourceFields.find(item=>item.value===code)?.label??code},
      {title:t('mdm.validation.targetModel'),render:(_:unknown,rule:MdmValidationRule)=>models.find(item=>item.code===rule.assertion.targetModel)?.name??rule.assertion.targetModel},
      {title:t('mdm.validation.timing'),dataIndex:'triggerPoint',render:(value:string)=><Tag>{t(`mdm.validation.trigger.${value}`)}</Tag>},
      {title:t('mdm.validation.strategy'),dataIndex:'severity',render:(value:string)=><Tag color={value==='WARNING'?'warning':'error'}>{t(`mdm.validation.severity.${value}`)}</Tag>},
      {title:t('common.fields.actions'),width:150,render:(_:unknown,_rule:MdmValidationRule,index:number)=>editable?<Space><Button type="link" onClick={()=>open(index)}>{t('common.actions.edit')}</Button><Popconfirm title={t('mdm.validation.deleteConfirm')} onConfirm={()=>setRules(current=>current.filter((_,itemIndex)=>itemIndex!==index))}><Button danger type="link">{t('common.actions.delete')}</Button></Popconfirm></Space>:null}
    ]}/>
    <Modal open={editingIndex!==undefined} title={t('mdm.validation.dialogTitle')} width={760} onCancel={()=>setEditingIndex(undefined)} onOk={confirm} destroyOnHidden>
      <Form form={form} layout="vertical" preserve={false}>
        <Space align="start" style={{width:'100%'}}><Form.Item name="name" label={t('mdm.validation.ruleName')} rules={[{required:true}]} style={{width:260}}><Input/></Form.Item><Form.Item name="enabled" valuePropName="checked" label=" "><Checkbox>{t('mdm.validation.enabled')}</Checkbox></Form.Item></Space>
        <Space align="start" wrap><Form.Item name="fieldCode" label={t('mdm.validation.currentField')} rules={[{required:true}]} style={{width:260}}><Select showSearch optionFilterProp="label" options={sourceFields}/></Form.Item><Form.Item name="targetModel" label={t('mdm.validation.targetModel')} rules={[{required:true}]} style={{width:300}}><Select showSearch optionFilterProp="label" options={models.map(item=>({value:item.code,label:`${item.name} (${item.code})`}))}/></Form.Item></Space>
        <Space align="start" wrap><Form.Item name="triggerPoint" label={t('mdm.validation.timing')} rules={[{required:true}]} style={{width:260}}><Select options={['SAVE','BLUR'].map(value=>({value,label:t(`mdm.validation.trigger.${value}`)}))}/></Form.Item><Form.Item name="severity" label={t('mdm.validation.strategy')} rules={[{required:true}]} style={{width:300}}><Select options={['BLOCK','WARNING'].map(value=>({value,label:t(`mdm.validation.severity.${value}`)}))}/></Form.Item></Space>
        <Form.Item name="message" label={t('mdm.validation.message')} rules={[{required:true}]}><Input placeholder={t('mdm.validation.messagePlaceholder')}/></Form.Item>
        <Typography.Title level={5}>{t('mdm.validation.conditions')}</Typography.Title><Typography.Paragraph type="secondary">{t('mdm.validation.conditionsHelp')}</Typography.Paragraph>
        <Form.List name="conditions">{(fields,{add,remove})=><Space direction="vertical" style={{width:'100%'}}>{fields.map(field=><Space key={field.key} align="start" wrap><Form.Item name={[field.name,'sourceType']} rules={[{required:true}]}><Select style={{width:120}} options={[{value:'FIELD',label:t('mdm.validation.sourceField')},{value:'CONSTANT',label:t('mdm.validation.constant')}]}/></Form.Item><Form.Item noStyle shouldUpdate>{({getFieldValue})=>getFieldValue(['conditions',field.name,'sourceType'])==='FIELD'?<Form.Item name={[field.name,'sourceField']} rules={[{required:true}]}><Select showSearch optionFilterProp="label" style={{width:220}} placeholder={t('mdm.validation.currentField')} options={sourceFields}/></Form.Item>:<Form.Item name={[field.name,'value']} rules={[{required:true}]}><Input style={{width:220}} placeholder={t('mdm.validation.constant')}/></Form.Item>}</Form.Item><Typography.Text>=</Typography.Text><Form.Item name={[field.name,'targetField']} rules={[{required:true}]}><Select showSearch optionFilterProp="label" style={{width:220}} placeholder={t('mdm.validation.targetField')} options={targetFields}/></Form.Item><Button danger onClick={()=>remove(field.name)}>{t('common.actions.delete')}</Button></Space>)}<Button onClick={()=>add({sourceType:'FIELD'})}>{t('mdm.validation.addCondition')}</Button></Space>}</Form.List>
      </Form>
    </Modal>
  </Card>;
};
