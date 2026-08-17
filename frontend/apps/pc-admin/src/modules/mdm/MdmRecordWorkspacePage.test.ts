import { describe, expect, it } from 'vitest';
import { buildMdmDynamicColumns } from './MdmRecordWorkspacePage';
import type { MdmField } from './types';

const field = (code:string, listVisible:boolean):MdmField => ({ id:1, code, name:code, dataType:'STRING', required:false, unique:false, readonly:false, searchable:false, sortable:false, listVisible, length:null, enumOptions:[], helpText:null, sortNo:1 });

describe('buildMdmDynamicColumns', () => {
  it('keeps every model field available in column settings', () => {
    const columns = buildMdmDynamicColumns([field('visible', true), field('hidden', false)]);
    expect(columns.map(column => column.key)).toEqual(['visible', 'hidden']);
    expect(columns[1]?.defaultVisible).toBe(false);
  });
});
