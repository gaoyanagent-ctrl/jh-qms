import { describe, expect, it } from 'vitest';
import { buildMdmGridDraft, updateMdmGridAttribute } from './MdmRecordWorkspacePage';
import type { MdmRecord } from './types';

const record:MdmRecord={id:'1',modelId:7,businessCode:'M-1',name:'旧名称',lifecycleStatus:'DRAFT',currentVersionNo:1,modelVersionNo:1,scopeType:'GROUP',scopeIds:[],effectiveFrom:null,effectiveTo:null,attributes:{materialType:'RAW',safetyPart:false},version:3,createdAt:'',updatedAt:''};

describe('MDM Grid change set',()=>{
  it('preserves the optimistic-lock version when staging a record',()=>{
    const draft=buildMdmGridDraft(record,'Grid edit');
    expect(draft.expectedVersion).toBe(3);
    expect(draft.attributes).not.toBe(record.attributes);
  });

  it('updates one dynamic field without dropping the others',()=>{
    const draft=updateMdmGridAttribute(buildMdmGridDraft(record,'Grid edit'),'safetyPart',true);
    expect(draft.attributes).toEqual({materialType:'RAW',safetyPart:true});
  });
});
