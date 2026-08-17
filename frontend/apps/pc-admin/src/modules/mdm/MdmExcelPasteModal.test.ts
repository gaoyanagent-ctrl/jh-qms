import { describe,expect,it } from 'vitest';
import { parseMdmExcelPaste } from './MdmExcelPasteModal';
import type { MdmModel } from './types';

const model:MdmModel={id:1,domainCode:'manufacturing',code:'material',name:'物料',recordType:'MASTER',versionEnabled:true,effectiveDateEnabled:true,organizationScopeEnabled:true,approvalRequired:false,status:'PUBLISHED',currentModelVersion:1,uiSchema:{},fields:[{id:1,code:'materialType',name:'物料类型',dataType:'ENUM',required:true,unique:false,readonly:false,searchable:true,sortable:true,listVisible:true,length:32,enumOptions:['RAW'],helpText:null,sortNo:10},{id:2,code:'safetyPart',name:'是否安全件',dataType:'BOOLEAN',required:false,unique:false,readonly:false,searchable:true,sortable:true,listVisible:true,length:null,enumOptions:[],helpText:null,sortNo:20}]};

describe('parseMdmExcelPaste',()=>{
  it('maps Chinese Excel headers and converts dynamic values',()=>{
    const records=parseMdmExcelPaste('业务编码\t名称\t物料类型\t是否安全件\nM-1\t测试物料\tRAW\t是',model);
    expect(records[0]).toMatchObject({businessCode:'M-1',name:'测试物料',attributes:{materialType:'RAW',safetyPart:true}});
  });
  it('requires header and data rows',()=>expect(()=>parseMdmExcelPaste('业务编码\t名称',model)).toThrow());
});
