import { describe, expect, it } from 'vitest';
import { getTabLabelKey } from './TabWorkspace';

describe('getTabLabelKey', () => {
  it('translates MDM routes', () => {
    expect(getTabLabelKey('/mdm/models')).toBe('menu.mdmModels');
    expect(getTabLabelKey('/mdm/models/material/design')).toBe('menu.mdmModelDesigner');
    expect(getTabLabelKey('/mdm/models/material/records')).toBe('menu.mdmRecords');
  });
});
