import { describe, expect, it } from 'vitest';
import { toRuleForm, toValidationRule } from './MdmValidationRuleEditor';

describe('MDM visual validation rule mapping',()=>{
  it('maps field and constant conditions without exposing JSON editing',()=>{
    const rule=toValidationRule({name:'组件必须生效',fieldCode:'componentCode',targetModel:'material',triggerPoint:'BLUR',severity:'BLOCK',message:'组件物料不存在',enabled:true,conditions:[{sourceType:'FIELD',sourceField:'componentCode',targetField:'businessCode'},{sourceType:'CONSTANT',value:'ACTIVE',targetField:'lifecycleStatus'}]},0);
    expect(rule.assertion.conditions).toEqual([{targetField:'businessCode',sourceField:'componentCode'},{targetField:'lifecycleStatus',value:'ACTIVE'}]);
    expect(toRuleForm(rule).conditions).toHaveLength(2);
  });
});
