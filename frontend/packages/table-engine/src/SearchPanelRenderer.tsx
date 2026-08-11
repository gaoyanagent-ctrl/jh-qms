import React from 'react';
import { Button, Form, Input, InputNumber, Select, Space, theme } from 'antd';
import { useTranslation } from 'react-i18next';
import type { SearchFieldDefinition } from './ListViewDefinition';

interface SearchPanelRendererProps {
  fields: SearchFieldDefinition[];
  onSearch: (values: Record<string, any>) => void;
  onReset: () => void;
}

export const SearchPanelRenderer: React.FC<SearchPanelRendererProps> = ({ fields, onSearch, onReset }) => {
  const { t } = useTranslation();
  const { token } = theme.useToken();
  const [form] = Form.useForm();

  const handleFinish = (values: any) => {
    const clean: Record<string, any> = {};
    Object.entries(values).forEach(([k, v]) => {
      if (v !== undefined && v !== '' && v !== null) {
        clean[k] = v;
      }
    });
    onSearch(clean);
  };

  const handleReset = () => {
    form.resetFields();
    onReset();
  };

  return (
    <Form
      form={form}
      layout="inline"
      onFinish={handleFinish}
      style={{
        padding: token.padding,
        marginBottom: token.margin,
        background: token.colorFillAlter,
        border: `1px solid ${token.colorBorderSecondary}`,
        borderRadius: token.borderRadiusLG
      }}
    >
      {fields.map((field) => (
        <Form.Item key={field.key} name={field.key} label={t(field.labelKey)} style={{ marginBottom: 8 }}>
          {field.type === 'number' ? (
            <InputNumber placeholder={field.placeholderKey ? t(field.placeholderKey) : ''} style={{ width: 160 }} />
          ) : field.type === 'select' ? (
            <Select placeholder={field.placeholderKey ? t(field.placeholderKey) : ''} style={{ width: 168 }} allowClear>
              {field.options?.map((opt) => (
                <Select.Option key={String(opt.value)} value={opt.value}>
                  {t(opt.labelKey)}
                </Select.Option>
              ))}
            </Select>
          ) : (
            <Input placeholder={field.placeholderKey ? t(field.placeholderKey) : ''} style={{ width: 220 }} allowClear />
          )}
        </Form.Item>
      ))}
      <Form.Item style={{ marginBottom: 8 }}>
        <Space>
          <Button type="primary" htmlType="submit">
            {t('common.actions.search')}
          </Button>
          <Button onClick={handleReset}>
            {t('common.actions.reset')}
          </Button>
        </Space>
      </Form.Item>
    </Form>
  );
};
