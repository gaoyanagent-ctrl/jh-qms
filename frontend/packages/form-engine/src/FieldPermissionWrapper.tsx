import React from 'react';
import { Form } from 'antd';

export type FieldPermissionType = 'VISIBLE_EDITABLE' | 'VISIBLE_READONLY' | 'HIDDEN' | 'MASKED';

interface FieldPermissionWrapperProps {
  permission: FieldPermissionType;
  label: string;
  name: string | string[];
  required?: boolean;
  rules?: any[];
  maskedValue?: string;
  children: React.ReactElement;
}

export const FieldPermissionWrapper: React.FC<FieldPermissionWrapperProps> = ({
  permission,
  label,
  name,
  required,
  rules,
  maskedValue,
  children
}) => {
  if (permission === 'HIDDEN') return null;

  if (permission === 'VISIBLE_READONLY' || permission === 'MASKED') {
    return (
      <Form.Item label={label} name={name}>
        {permission === 'MASKED' ? (
          <span>{maskedValue ?? '******'}</span>
        ) : (
          <Form.Item noStyle name={name}>
            <ReadOnlyField />
          </Form.Item>
        )}
      </Form.Item>
    );
  }

  return (
    <Form.Item
      label={label}
      name={name}
      rules={rules}
      required={required}
    >
      {children}
    </Form.Item>
  );
};

const ReadOnlyField: React.FC<{ value?: any }> = ({ value }) => {
  return <span className="ant-form-text">{value !== undefined && value !== null ? String(value) : '-'}</span>;
};
