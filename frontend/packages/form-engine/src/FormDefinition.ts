export interface FieldDefinition {
  key: string;
  labelKey: string;
  type: 'text' | 'password' | 'number' | 'select' | 'textarea';
  required?: boolean;
  rules?: any[];
  options?: { labelKey: string; value: any }[];
  defaultValue?: any;
  placeholderKey?: string;
  maskedTemplate?: (value: any) => string; // Custom formatting for MASKED fields
  isExpertOnly?: boolean; // For simple/expert view switching
}

export interface FormSectionDefinition {
  key: string;
  titleKey?: string;
  fields: FieldDefinition[];
}

export interface FormDefinition {
  id: string;
  sections: FormSectionDefinition[];
}
