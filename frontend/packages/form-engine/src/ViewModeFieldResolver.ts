import type { FieldDefinition } from './FormDefinition';

export type ViewMode = 'simple' | 'expert';

export const resolveFieldsByViewMode = (fields: FieldDefinition[], mode: ViewMode): FieldDefinition[] => {
  if (mode === 'simple') {
    return fields.filter((f) => !f.isExpertOnly);
  }
  return fields;
};
