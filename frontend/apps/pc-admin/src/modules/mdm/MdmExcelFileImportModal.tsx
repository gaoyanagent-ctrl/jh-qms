import { DownloadOutlined, InboxOutlined } from '@ant-design/icons';
import { Alert, Button, Divider, Modal, Space, Table, Tag, Typography, Upload } from 'antd';
import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import dayjs from 'dayjs';
import { mdmApi } from './api';
import { useCommitMdmImport, useMdmImportErrors, useMdmImportTasks, usePreviewMdmImport } from './hooks';
import type { MdmModel } from './types';

export const MdmExcelFileImportModal=({open,onClose,model}:{open:boolean;onClose:()=>void;model:MdmModel})=>{
  const {t}=useTranslation(); const [file,setFile]=useState<File>(); const [downloadError,setDownloadError]=useState<string>();
  const [selectedTaskId,setSelectedTaskId]=useState<string>();
  const preview=usePreviewMdmImport(model.code);
  const tasks=useMdmImportTasks(model.code,open); const taskErrors=useMdmImportErrors(model.code,selectedTaskId);
  const clear=()=>{setFile(undefined);setDownloadError(undefined);setSelectedTaskId(undefined);preview.reset();};
  const close=()=>{clear();onClose();};
  const commit=useCommitMdmImport(model.code,task=>task.status==='COMMITTED'?close():setSelectedTaskId(task.id));
  const download=async()=>{try{setDownloadError(undefined);const blob=await mdmApi.importTemplate(model.code);const url=URL.createObjectURL(blob);const link=document.createElement('a');link.href=url;link.download=`${model.code}-import-template.xlsx`;link.click();URL.revokeObjectURL(url);}catch(error){setDownloadError(error instanceof Error?error.message:String(error));}};
  const downloadArtifact=async(taskId:string,fileName:string,type:'source'|'result')=>{try{setDownloadError(undefined);const blob=await (type==='source'?mdmApi.importSource(model.code,taskId):mdmApi.importResult(model.code,taskId));const url=URL.createObjectURL(blob);const link=document.createElement('a');link.href=url;link.download=type==='source'?fileName:`${fileName.replace(/\.[^.]+$/,'')}-校验结果.xlsx`;link.click();URL.revokeObjectURL(url);}catch(error){setDownloadError(error instanceof Error?error.message:String(error));}};
  const validation=selectedTaskId?taskErrors.data:preview.data?.validation;
  return <Modal open={open} onCancel={close} title={t('mdm.import.title')} width={980} destroyOnHidden footer={<Space><Button onClick={close}>{t('common.actions.cancel')}</Button><Button type="primary" disabled={!preview.data?.validation.valid||preview.data.status!=='READY'} loading={commit.isPending} onClick={()=>preview.data&&commit.mutate(preview.data.taskId)}>{t('mdm.import.confirm',{count:preview.data?.validation.total??0})}</Button></Space>}>
    <Space direction="vertical" size={16} style={{width:'100%'}}>
      <Alert type="info" showIcon message={t('mdm.import.guideTitle')} description={t('mdm.import.guideDescription')} action={<Button icon={<DownloadOutlined/>} onClick={download}>{t('mdm.import.downloadTemplate')}</Button>}/>
      <Upload.Dragger accept=".xlsx,.xls" maxCount={1} fileList={file?[{uid:'selected',name:file.name,status:'done'}]:[]} beforeUpload={selected=>{setFile(selected);preview.reset();return false;}} onRemove={()=>{setFile(undefined);preview.reset();}}>
        <p className="ant-upload-drag-icon"><InboxOutlined/></p><p className="ant-upload-text">{t('mdm.import.dropFile')}</p><p className="ant-upload-hint">{t('mdm.import.fileHint')}</p>
      </Upload.Dragger>
      <Button block disabled={!file} loading={preview.isPending} onClick={()=>file&&preview.mutate(file)}>{t('mdm.import.precheck')}</Button>
      {(downloadError||preview.isError||commit.isError)&&<Alert type="error" showIcon message={downloadError??preview.error?.message??commit.error?.message}/>}
      {validation&&<><Alert type={validation.valid?'success':'warning'} showIcon message={t(validation.valid?'mdm.import.precheckPassed':'mdm.import.precheckFailed',{count:validation.total})}/><Table size="small" pagination={{pageSize:8}} rowKey="rowNo" dataSource={validation.rows} columns={[{title:t('mdm.import.excelRow'),dataIndex:'rowNo',width:90},{title:t('mdm.fields.businessCode'),dataIndex:'businessCode'},{title:t('mdm.import.result'),dataIndex:'valid',width:90,render:(valid:boolean)=><Tag color={valid?'success':'error'}>{t(valid?'mdm.import.passed':'mdm.import.failed')}</Tag>},{title:t('mdm.import.problems'),dataIndex:'errors',render:(errors:string[])=><Typography.Text type={errors.length?'danger':undefined}>{errors.length?errors.join('；'):'—'}</Typography.Text>}]}/></>}
      <Divider orientation="left">{t('mdm.import.history')}</Divider>
      <Table loading={tasks.isLoading} size="small" rowKey="id" pagination={{pageSize:5}} dataSource={tasks.data??[]} columns={[{title:t('mdm.import.fileName'),dataIndex:'fileName'},{title:t('common.fields.status'),dataIndex:'status',width:130,render:(status:string)=><Tag color={status==='COMMITTED'?'success':status==='READY'?'processing':status==='PRECHECK_FAILED'?'error':'warning'}>{t(`mdm.import.status.${status}`)}</Tag>},{title:t('mdm.import.rowSummary'),width:110,render:(_,task)=><span>{task.validRows}/{task.totalRows}</span>},{title:t('mdm.import.operator'),dataIndex:'createdByName',width:110},{title:t('mdm.import.createdAt'),dataIndex:'createdAt',width:145,render:(value:string)=>dayjs(value).format('YYYY-MM-DD HH:mm')},{title:t('common.fields.actions'),width:230,render:(_,task)=><Space size={0}><Button type="link" size="small" onClick={()=>setSelectedTaskId(task.id)}>{t('mdm.import.viewResult')}</Button><Button type="link" size="small" disabled={!task.sourceFileAvailable} onClick={()=>downloadArtifact(task.id,task.fileName,'source')}>{t('mdm.import.downloadSource')}</Button><Button type="link" size="small" onClick={()=>downloadArtifact(task.id,task.fileName,'result')}>{t('mdm.import.downloadResult')}</Button></Space>}]}/>
    </Space>
  </Modal>;
};
