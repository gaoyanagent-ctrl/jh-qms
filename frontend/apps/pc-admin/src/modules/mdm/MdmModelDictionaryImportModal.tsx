import { DownloadOutlined, InboxOutlined } from '@ant-design/icons';
import { Alert, Button, Card, Col, Modal, Row, Space, Statistic, Table, Tag, Typography, Upload } from 'antd';
import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { mdmApi } from './api';
import { usePreviewMdmModelDictionary } from './hooks';
import type { MdmModelDictionaryIssue } from './types';

export const MdmModelDictionaryImportModal=({open,onClose}:{open:boolean;onClose:()=>void})=>{
  const {t}=useTranslation();const[file,setFile]=useState<File>();const[downloadError,setDownloadError]=useState<string>();const preview=usePreviewMdmModelDictionary();
  const close=()=>{setFile(undefined);setDownloadError(undefined);preview.reset();onClose();};
  const download=async()=>{try{setDownloadError(undefined);const blob=await mdmApi.modelDictionaryTemplate();const url=URL.createObjectURL(blob);const link=document.createElement('a');link.href=url;link.download='mdm-model-dictionary-template.xlsx';link.click();URL.revokeObjectURL(url);}catch(error){setDownloadError(error instanceof Error?error.message:String(error));}};
  const result=preview.data;
  const issueColumns=[{title:t('mdm.modelImport.sheet'),dataIndex:'sheet',width:120},{title:t('mdm.modelImport.row'),dataIndex:'rowNo',width:80,render:(value:number)=>value||'-'},{title:t('mdm.modelImport.field'),dataIndex:'field',width:180},{title:t('mdm.modelImport.problem'),dataIndex:'message'}];
  return <Modal title={t('mdm.modelImport.title')} open={open} onCancel={close} footer={<Button onClick={close}>{t('common.actions.cancel')}</Button>} width={1000} destroyOnHidden>
    <Alert type="info" showIcon message={t('mdm.modelImport.guideTitle')} description={t('mdm.modelImport.guideDescription')} action={<Button icon={<DownloadOutlined/>} onClick={download}>{t('mdm.modelImport.downloadTemplate')}</Button>} style={{marginBottom:16}}/>
    <Upload.Dragger accept=".xlsx,.xls" maxCount={1} beforeUpload={selected=>{setFile(selected);preview.reset();return false;}} onRemove={()=>{setFile(undefined);preview.reset();}} disabled={preview.isPending}>
      <p className="ant-upload-drag-icon"><InboxOutlined/></p><p className="ant-upload-text">{t('mdm.modelImport.dropTitle')}</p><p className="ant-upload-hint">{t('mdm.modelImport.dropHelp')}</p>
    </Upload.Dragger>
    <Space style={{marginTop:16}}><Button type="primary" disabled={!file} loading={preview.isPending} onClick={()=>file&&preview.mutate(file)}>{t('mdm.modelImport.precheck')}</Button><Typography.Text type="secondary">{t('mdm.modelImport.noWriteHint')}</Typography.Text></Space>
    {(downloadError||preview.isError)&&<Alert type="error" showIcon message={downloadError??preview.error?.message} style={{marginTop:16}}/>}
    {result&&<Space direction="vertical" size={16} style={{width:'100%',marginTop:16}}>
      <Alert type={result.valid?'success':'error'} showIcon message={t(result.valid?'mdm.modelImport.valid':'mdm.modelImport.invalid')} description={result.valid?t('mdm.modelImport.readyDescription'):t('mdm.modelImport.fixDescription')}/>
      <Row gutter={[16,16]}><Col xs={12} md={6}><Card size="small"><Statistic title={t('mdm.modelImport.modelCount')} value={result.totalModels}/></Card></Col><Col xs={12} md={6}><Card size="small"><Statistic title={t('mdm.modelImport.fieldCount')} value={result.totalFields}/></Card></Col><Col xs={12} md={6}><Card size="small"><Statistic title={t('mdm.modelImport.createCount')} value={result.modelCreates}/></Card></Col><Col xs={12} md={6}><Card size="small"><Statistic title={t('mdm.modelImport.updateCount')} value={result.modelUpdates}/></Card></Col></Row>
      <Table size="small" rowKey="modelCode" pagination={false} dataSource={result.changes} columns={[
        {title:t('mdm.fields.modelCode'),dataIndex:'modelCode'},{title:t('mdm.fields.name'),dataIndex:'modelName'},
        {title:t('mdm.modelImport.changeType'),dataIndex:'changeType',width:110,render:(value:string)=><Tag color={value==='CREATE'?'success':'processing'}>{t(`mdm.modelImport.change.${value}`)}</Tag>},
        {title:t('mdm.modelImport.fieldChanges'),width:260,render:(_,item)=><Space wrap><Tag color="success">{t('mdm.modelImport.added',{count:item.fieldAdds})}</Tag><Tag color="processing">{t('mdm.modelImport.updated',{count:item.fieldUpdates})}</Tag><Tag>{t('mdm.modelImport.unchanged',{count:item.fieldUnchanged})}</Tag></Space>},
        {title:t('mdm.modelImport.issueCount'),width:100,render:(_,item)=>item.issues.length},
      ]}/>
      {result.issues.length>0&&<Card size="small" title={t('mdm.modelImport.issues')}><Table<MdmModelDictionaryIssue> size="small" rowKey={(item,index)=>`${item.sheet}-${item.rowNo}-${item.field}-${index}`} pagination={{pageSize:8}} dataSource={result.issues} columns={issueColumns}/></Card>}
    </Space>}
  </Modal>;
};
