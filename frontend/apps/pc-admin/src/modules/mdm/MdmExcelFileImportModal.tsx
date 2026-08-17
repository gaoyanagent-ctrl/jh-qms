import { DownloadOutlined, InboxOutlined } from '@ant-design/icons';
import { Alert, Button, Modal, Space, Table, Tag, Typography, Upload } from 'antd';
import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { mdmApi } from './api';
import { useCreateMdmBatch, usePreviewMdmImport } from './hooks';
import type { MdmModel } from './types';

export const MdmExcelFileImportModal=({open,onClose,model}:{open:boolean;onClose:()=>void;model:MdmModel})=>{
  const {t}=useTranslation(); const [file,setFile]=useState<File>(); const [downloadError,setDownloadError]=useState<string>();
  const preview=usePreviewMdmImport(model.code);
  const clear=()=>{setFile(undefined);setDownloadError(undefined);preview.reset();};
  const close=()=>{clear();onClose();};
  const create=useCreateMdmBatch(model.code,()=>close());
  const download=async()=>{try{setDownloadError(undefined);const blob=await mdmApi.importTemplate(model.code);const url=URL.createObjectURL(blob);const link=document.createElement('a');link.href=url;link.download=`${model.code}-import-template.xlsx`;link.click();URL.revokeObjectURL(url);}catch(error){setDownloadError(error instanceof Error?error.message:String(error));}};
  const validation=preview.data?.validation;
  return <Modal open={open} onCancel={close} title={t('mdm.import.title')} width={920} destroyOnHidden footer={<Space><Button onClick={close}>{t('common.actions.cancel')}</Button><Button type="primary" disabled={!validation?.valid} loading={create.isPending} onClick={()=>create.mutate(preview.data?.records??[])}>{t('mdm.import.confirm',{count:validation?.total??0})}</Button></Space>}>
    <Space direction="vertical" size={16} style={{width:'100%'}}>
      <Alert type="info" showIcon message={t('mdm.import.guideTitle')} description={t('mdm.import.guideDescription')} action={<Button icon={<DownloadOutlined/>} onClick={download}>{t('mdm.import.downloadTemplate')}</Button>}/>
      <Upload.Dragger accept=".xlsx,.xls" maxCount={1} fileList={file?[{uid:'selected',name:file.name,status:'done'}]:[]} beforeUpload={selected=>{setFile(selected);preview.reset();return false;}} onRemove={()=>{setFile(undefined);preview.reset();}}>
        <p className="ant-upload-drag-icon"><InboxOutlined/></p><p className="ant-upload-text">{t('mdm.import.dropFile')}</p><p className="ant-upload-hint">{t('mdm.import.fileHint')}</p>
      </Upload.Dragger>
      <Button block disabled={!file} loading={preview.isPending} onClick={()=>file&&preview.mutate(file)}>{t('mdm.import.precheck')}</Button>
      {(downloadError||preview.isError||create.isError)&&<Alert type="error" showIcon message={downloadError??preview.error?.message??create.error?.message}/>} 
      {validation&&<><Alert type={validation.valid?'success':'warning'} showIcon message={t(validation.valid?'mdm.import.precheckPassed':'mdm.import.precheckFailed',{count:validation.total})}/><Table size="small" pagination={{pageSize:8}} rowKey="rowNo" dataSource={validation.rows} columns={[{title:t('mdm.import.excelRow'),dataIndex:'rowNo',width:90},{title:t('mdm.fields.businessCode'),dataIndex:'businessCode'},{title:t('mdm.import.result'),dataIndex:'valid',width:90,render:(valid:boolean)=><Tag color={valid?'success':'error'}>{t(valid?'mdm.import.passed':'mdm.import.failed')}</Tag>},{title:t('mdm.import.problems'),dataIndex:'errors',render:(errors:string[])=><Typography.Text type={errors.length?'danger':undefined}>{errors.length?errors.join('；'):'—'}</Typography.Text>}]}/></>}
    </Space>
  </Modal>;
};
