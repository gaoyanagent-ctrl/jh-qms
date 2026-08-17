import { Alert, Button, Input, Modal, Space, Table, Tag, Typography } from 'antd';
import { useMemo, useState } from 'react';
import { useCreateMdmBatch, useValidateMdmBatch } from './hooks';
import type { MdmDataType, MdmModel, SaveMdmRecord } from './types';

const convert=(value:string,type:MdmDataType):unknown=>{
  if(value==='')return null;
  if(type==='BOOLEAN')return ['true','1','是','yes'].includes(value.toLowerCase());
  if(type==='INTEGER')return Number.parseInt(value,10);
  if(type==='DECIMAL')return Number(value);
  return value;
};

export const parseMdmExcelPaste=(text:string,model:MdmModel):SaveMdmRecord[]=>{
  const lines=text.replace(/\r/g,'').split('\n').filter(line=>line.trim()); if(lines.length<2)throw new Error('请粘贴表头和至少一行数据');
  const headers=lines[0].split('\t').map(value=>value.trim());
  const businessIndex=headers.findIndex(value=>['businessCode','业务编码'].includes(value)); const nameIndex=headers.findIndex(value=>['name','名称'].includes(value));
  if(businessIndex<0||nameIndex<0)throw new Error('表头必须包含“业务编码”和“名称”');
  return lines.slice(1).map(line=>{const cells=line.split('\t').map(value=>value.trim()); const attributes:Record<string,unknown>={};
    model.fields.forEach(field=>{const index=headers.findIndex(value=>value===field.code||value===field.name);if(index>=0&&cells[index]!==undefined)attributes[field.code]=convert(cells[index],field.dataType);});
    return {businessCode:cells[businessIndex]??'',name:cells[nameIndex]??'',lifecycleStatus:'DRAFT',scopeType:'GROUP',scopeIds:[],attributes,changeReason:'Excel 粘贴导入'};
  });
};

export const MdmExcelPasteModal=({open,onClose,model}:{open:boolean;onClose:()=>void;model:MdmModel})=>{
  const [text,setText]=useState(''); const [records,setRecords]=useState<SaveMdmRecord[]>([]); const [parseError,setParseError]=useState<string>();
  const validation=useValidateMdmBatch(model.code); const create=useCreateMdmBatch(model.code,()=>{onClose();setText('');setRecords([]);validation.reset();});
  const example=useMemo(()=>['业务编码','名称',...model.fields.map(field=>field.name)].join('\t'),[model.fields]);
  const preview=()=>{try{const parsed=parseMdmExcelPaste(text,model);setParseError(undefined);setRecords(parsed);validation.mutate(parsed);}catch(error){setParseError(error instanceof Error?error.message:String(error));}};
  return <Modal open={open} onCancel={onClose} title="Excel 粘贴导入" width={900} footer={<Space><Button onClick={onClose}>取消</Button><Button onClick={preview} loading={validation.isPending}>预检查</Button><Button type="primary" disabled={!validation.data?.valid||records.length===0} loading={create.isPending} onClick={()=>create.mutate(records)}>确认导入 {records.length||''}</Button></Space>}>
    <Typography.Paragraph type="secondary">从 Excel 复制包含表头的数据区域后粘贴到下方。必需表头：业务编码、名称；其他列可使用字段中文名或字段编码。</Typography.Paragraph>
    <Typography.Paragraph copyable={{text:example}}>模板表头：<Typography.Text code>{example}</Typography.Text></Typography.Paragraph>
    <Input.TextArea aria-label="Excel 粘贴内容" rows={9} value={text} onChange={event=>{setText(event.target.value);validation.reset();}} placeholder={`${example}\nM-001\t示例物料`} />
    {(parseError||validation.isError||create.isError)&&<Alert style={{marginTop:12}} type="error" showIcon message={parseError??validation.error?.message??create.error?.message}/>} 
    {validation.data&&<Table style={{marginTop:16}} size="small" pagination={{pageSize:8}} rowKey="rowNo" dataSource={validation.data.rows} columns={[{title:'Excel 行',dataIndex:'rowNo',width:90},{title:'业务编码',dataIndex:'businessCode'},{title:'结果',dataIndex:'valid',width:90,render:(valid:boolean)=><Tag color={valid?'success':'error'}>{valid?'通过':'错误'}</Tag>},{title:'问题',dataIndex:'errors',render:(errors:string[])=>errors.length?errors.join('；'):'—'}]}/>} 
  </Modal>;
};
