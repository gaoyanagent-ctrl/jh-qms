import type { QmsDrawingLegendRule } from '@iaf/domain-types';
import { AppPageContainer } from '@iaf/ui-core';
import { Alert, App, Button, Card, Input, InputNumber, Space, Switch, Table, Tag, Typography } from 'antd';
import { useEffect, useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useQmsDrawingLegendQuery, useUpdateQmsDrawingLegendMutation } from './hooks';

const targetLabels: Record<QmsDrawingLegendRule['targetField'], string> = {
 INSPECTION_DIMENSION:'检验尺寸',LOCATION_DIMENSION:'定位尺寸',FIT_DIMENSION:'配合尺寸',
 REFERENCE_DIMENSION:'参考尺寸',SPECIAL_CODE:'特殊特性',REGULATORY_FLAG:'法规特性'
};

export const QmsDrawingLegendConfigPage=()=>{
 const {t}=useTranslation();const {message}=App.useApp();const query=useQmsDrawingLegendQuery();
 const [rows,setRows]=useState<QmsDrawingLegendRule[]>([]);useEffect(()=>{if(query.data)setRows(query.data)},[query.data]);
 const mutation=useUpdateQmsDrawingLegendMutation(()=>message.success(t('qmsLegend.saved')));
 const update=(id:number,patch:Partial<QmsDrawingLegendRule>)=>setRows(current=>current.map(row=>row.id===id?{...row,...patch}:row));
 const dirty=useMemo(()=>JSON.stringify(rows)!==JSON.stringify(query.data??[]),[rows,query.data]);
 const save=()=>mutation.mutate({rules:rows.map(({id,version,marker,description,enabled,priority})=>({id,version,marker,description,enabled,priority}))});
 return <AppPageContainer title={<Space direction="vertical" size={0}><span>{t('qmsLegend.title')}</span><Typography.Text type="secondary">{t('qmsLegend.subtitle')}</Typography.Text></Space>}
  extra={<Space><Button disabled={!dirty} onClick={()=>setRows(query.data??[])}>{t('common.actions.reset')}</Button><Button type="primary" loading={mutation.isPending} disabled={!dirty} onClick={save}>{t('common.actions.save')}</Button></Space>}>
  <Alert showIcon type="info" message={t('qmsLegend.effectTitle')} description={t('qmsLegend.effectDescription')} style={{marginBottom:16}} />
  <Card title={t('qmsLegend.rules')}>
   <Table rowKey="id" loading={query.isLoading} pagination={false} dataSource={rows} scroll={{x:820}} columns={[
    {title:t('qmsLegend.enabled'),dataIndex:'enabled',width:88,render:(value:boolean,row)=><Switch checked={value} aria-label={`${row.description} ${t('qmsLegend.enabled')}`} onChange={enabled=>update(row.id,{enabled})}/>},
    {title:t('qmsLegend.marker'),dataIndex:'marker',width:130,render:(value:string,row)=><Input value={value} maxLength={32} aria-label={`${row.description} ${t('qmsLegend.marker')}`} onChange={e=>update(row.id,{marker:e.target.value})}/>},
    {title:t('qmsLegend.meaning'),dataIndex:'description',render:(value:string,row)=><Input value={value} maxLength={128} aria-label={`${row.ruleCode} ${t('qmsLegend.meaning')}`} onChange={e=>update(row.id,{description:e.target.value})}/>},
    {title:t('qmsLegend.classification'),dataIndex:'targetField',width:150,render:(value:QmsDrawingLegendRule['targetField'],row)=><Space><Tag>{targetLabels[value]}</Tag>{row.targetValue&&<Typography.Text code>{row.targetValue}</Typography.Text>}</Space>},
    {title:t('qmsLegend.matchMode'),dataIndex:'matchMode',width:120,render:(value:string)=><Typography.Text type="secondary">{value==='WRAPS_VALUE'?t('qmsLegend.wraps'):t('qmsLegend.contains')}</Typography.Text>},
    {title:t('qmsLegend.priority'),dataIndex:'priority',width:110,render:(value:number,row)=><InputNumber min={0} max={9999} value={value} aria-label={`${row.description} ${t('qmsLegend.priority')}`} onChange={priority=>update(row.id,{priority:priority??0})}/>}
   ]}/>
  </Card>
  <Card title={t('qmsLegend.example')} style={{marginTop:16}}><Typography.Text code>[B]6.5±0.3◆▲</Typography.Text><Space wrap style={{marginInlineStart:16}}><Tag color="red">B · {t('qmsLegend.important')}</Tag><Tag color="blue">{t('qmsReview.flags.inspection')}</Tag><Tag color="purple">{t('qmsReview.flags.locationDimension')}</Tag></Space></Card>
 </AppPageContainer>;
};
