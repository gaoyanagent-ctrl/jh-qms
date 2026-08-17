import { describe, expect, it } from 'vitest';
import { buildMdmDynamicColumns, buildMdmVersionDiff } from './MdmRecordWorkspacePage';
import type { MdmField, MdmRecordVersion } from './types';

const field = (code:string, listVisible:boolean):MdmField => ({ id:1, code, name:code, dataType:'STRING', required:false, unique:false, readonly:false, searchable:false, sortable:false, listVisible, length:null, enumOptions:[], helpText:null, sortNo:1 });

describe('buildMdmDynamicColumns', () => {
  it('keeps every model field available in column settings', () => {
    const columns = buildMdmDynamicColumns([field('visible', true), field('hidden', false)]);
    expect(columns.map(column => column.key)).toEqual(['visible', 'hidden']);
    expect(columns[1]?.defaultVisible).toBe(false);
  });
});

describe('buildMdmVersionDiff',()=>{
  const version=(versionNo:number,snapshot:Record<string,unknown>):MdmRecordVersion=>({id:versionNo,recordId:'r1',versionNo,snapshot,changeType:'UPDATE',changeReason:null,effectiveFrom:null,effectiveTo:null,createdBy:1,createdByName:'管理员',createdAt:'2026-08-17T00:00:00Z'});
  it('shows field-level before and after values including dynamic attributes',()=>{
    const changes=buildMdmVersionDiff(version(2,{name:'新名称',attributes:{materialType:'FINISHED'}}),version(1,{name:'旧名称',attributes:{materialType:'RAW'}}));
    expect(changes).toEqual([{key:'name',before:'旧名称',after:'新名称'},{key:'materialType',before:'RAW',after:'FINISHED'}]);
  });
});
