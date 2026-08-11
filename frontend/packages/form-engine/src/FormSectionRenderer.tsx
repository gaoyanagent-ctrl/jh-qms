import React from 'react';
import { Card, Row, Col } from 'antd';
import { useTranslation } from 'react-i18next';
import type { FieldDefinition, FormSectionDefinition } from './FormDefinition';
import { FieldPermissionWrapper, FieldPermissionType } from './FieldPermissionWrapper';
import { resolveFieldsByViewMode, ViewMode } from './ViewModeFieldResolver';

interface FormSectionRendererProps {
  section: FormSectionDefinition;
  viewMode: ViewMode;
  fieldPermissions: Record<string, FieldPermissionType>;
  renderFieldInput: (field: FieldDefinition) => React.ReactElement;
  getMaskedValue?: (fieldKey: string, value: any) => string;
  formValues?: Record<string, any>;
}

export const FormSectionRenderer: React.FC<FormSectionRendererProps> = ({
  section,
  viewMode,
  fieldPermissions,
  renderFieldInput,
  getMaskedValue,
  formValues
}) => {
  const { t } = useTranslation();
  const visibleFields = resolveFieldsByViewMode(section.fields, viewMode);

  if (visibleFields.length === 0) return null;

  const content = (
    <Row gutter={16}>
      {visibleFields.map((field) => {
        const permission = fieldPermissions[field.key] || 'VISIBLE_EDITABLE';
        const label = t(field.labelKey);
        const rawValue = formValues ? formValues[field.key] : undefined;
        const maskedVal = permission === 'MASKED'
          ? (field.maskedTemplate ? field.maskedTemplate(rawValue) : (getMaskedValue ? getMaskedValue(field.key, rawValue) : undefined))
          : undefined;

        return (
          <Col span={12} key={field.key}>
            <FieldPermissionWrapper
              permission={permission}
              label={label}
              name={field.key}
              required={field.required}
              rules={field.rules}
              maskedValue={maskedVal}
            >
              {renderFieldInput(field)}
            </FieldPermissionWrapper>
          </Col>
        );
      })}
    </Row>
  );

  if (section.titleKey) {
    return (
      <Card title={t(section.titleKey)} style={{ marginBottom: 16 }} bordered={false}>
        {content}
      </Card>
    );
  }

  return <div style={{ marginBottom: 16 }}>{content}</div>;
};
