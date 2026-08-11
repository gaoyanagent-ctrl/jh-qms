import React, { useState } from 'react';
import { Form, Input, InputNumber, Select, Button, Space, Switch, theme } from 'antd';
import { useTranslation } from 'react-i18next';
import type { FormDefinition, FieldDefinition } from './FormDefinition';
import { FormSectionRenderer } from './FormSectionRenderer';
import type { FieldPermissionType } from './FieldPermissionWrapper';
import type { ViewMode } from './ViewModeFieldResolver';

interface ConfigurableFormPageProps<T> {
  definition: FormDefinition;
  initialValues?: Partial<T>;
  fieldPermissions?: Record<string, FieldPermissionType>;
  loading?: boolean;
  onFinish: (values: T) => void;
  onCancel: () => void;
  title: string;
}

export function ConfigurableFormPage<T extends Record<string, any>>({
  definition,
  initialValues,
  fieldPermissions = {},
  loading = false,
  onFinish,
  onCancel,
  title
}: ConfigurableFormPageProps<T>) {
  const { t } = useTranslation();
  const { token } = theme.useToken();
  const [form] = Form.useForm();
  const [viewMode, setViewMode] = useState<ViewMode>('simple');
  const [formValues, setFormValues] = useState<Record<string, any>>(initialValues || {});

  const renderFieldInput = (field: FieldDefinition): React.ReactElement => {
    const placeholder = field.placeholderKey ? t(field.placeholderKey) : '';

    if (field.type === 'number') {
      return <InputNumber placeholder={placeholder} style={{ width: '100%' }} />;
    }
    if (field.type === 'select') {
      return (
        <Select placeholder={placeholder} allowClear style={{ width: '100%' }}>
          {field.options?.map((opt) => (
            <Select.Option key={String(opt.value)} value={opt.value}>
              {t(opt.labelKey)}
            </Select.Option>
          ))}
        </Select>
      );
    }
    if (field.type === 'textarea') {
      return <Input.TextArea placeholder={placeholder} rows={4} />;
    }
    if (field.type === 'password') {
      return <Input.Password placeholder={placeholder} />;
    }
    return <Input placeholder={placeholder} />;
  };

  const handleValuesChange = (_: any, allValues: any) => {
    setFormValues(allValues);
  };

  return (
    <div style={{ padding: token.paddingLG, background: token.colorBgLayout, minHeight: '100vh' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: token.margin }}>
        <h2>{title}</h2>
        <Space>
          <span>{t('workspace.expertMode') || 'Expert Mode'}</span>
          <Switch
            checked={viewMode === 'expert'}
            onChange={(checked) => setViewMode(checked ? 'expert' : 'simple')}
          />
        </Space>
      </div>

      <Form
        form={form}
        layout="vertical"
        initialValues={initialValues}
        onValuesChange={handleValuesChange}
        onFinish={onFinish}
      >
        {definition.sections.map((section) => (
          <FormSectionRenderer
            key={section.key}
            section={section}
            viewMode={viewMode}
            fieldPermissions={fieldPermissions}
            renderFieldInput={renderFieldInput}
            formValues={formValues}
          />
        ))}

        <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: 16 }}>
          <Space>
            <Button onClick={onCancel}>{t('common.actions.cancel')}</Button>
            <Button type="primary" htmlType="submit" loading={loading}>
              {t('common.actions.save')}
            </Button>
          </Space>
        </div>
      </Form>
    </div>
  );
}
