import React from 'react';
import { Popover, Checkbox, List, Button, Tooltip } from 'antd';
import { ArrowDownOutlined, ArrowUpOutlined, SettingOutlined } from '@ant-design/icons';
import { useTranslation } from 'react-i18next';
import type { ColumnPreference } from './UserListViewPreference';

interface ColumnSettingsProps {
  columns: ColumnPreference[];
  titleKeys: Record<string, string>;
  onChange: (columns: ColumnPreference[]) => void;
}

export const ColumnSettings: React.FC<ColumnSettingsProps> = ({ columns, titleKeys, onChange }) => {
  const { t } = useTranslation();

  const handleToggle = (key: string) => {
    const updated = columns.map((col) =>
      col.key === key ? { ...col, visible: !col.visible } : col
    );
    onChange(updated);
  };

  const handleMove = (index: number, direction: 'up' | 'down') => {
    const targetIndex = direction === 'up' ? index - 1 : index + 1;
    if (targetIndex < 0 || targetIndex >= columns.length) return;

    const updated = [...columns];
    const temp = updated[index];
    updated[index] = updated[targetIndex];
    updated[targetIndex] = temp;
    onChange(updated);
  };

  const content = (
    <div style={{ width: 260, maxHeight: 400, overflowY: 'auto' }}>
      <List
        size="small"
        dataSource={columns}
        renderItem={(col, idx) => (
          <List.Item
            actions={[
              <Button
                key="up"
                type="link"
                size="small"
                aria-label={t('workspace.moveColumnUp')}
                icon={<ArrowUpOutlined />}
                disabled={idx === 0}
                onClick={() => handleMove(idx, 'up')}
              />,
              <Button
                key="down"
                type="link"
                size="small"
                aria-label={t('workspace.moveColumnDown')}
                icon={<ArrowDownOutlined />}
                disabled={idx === columns.length - 1}
                onClick={() => handleMove(idx, 'down')}
              />
            ]}
          >
            <Checkbox checked={col.visible} onChange={() => handleToggle(col.key)}>
              {titleKeys[col.key] ? t(titleKeys[col.key]) : col.key}
            </Checkbox>
          </List.Item>
        )}
      />
    </div>
  );

  return (
    <Popover
      content={content}
      title={t('workspace.columnSettings') || 'Column Settings'}
      trigger="click"
      placement="bottomRight"
    >
      <Tooltip title={t('workspace.columnSettings')}>
        <Button aria-label={t('workspace.columnSettings')} icon={<SettingOutlined />} />
      </Tooltip>
    </Popover>
  );
};
