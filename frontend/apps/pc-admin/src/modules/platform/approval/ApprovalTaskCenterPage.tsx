import { AppPageContainer, IafSectionHeader, IafStatusPill, IafSurface, StatusTag } from '@iaf/ui-core';
import { AuditOutlined, CheckOutlined, ClockCircleOutlined, CloseOutlined, RollbackOutlined } from '@ant-design/icons';
import { Button, Descriptions, Drawer, Space, Table, Tabs, Tag, Timeline, Typography, message } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useState } from 'react';
import { useTranslation } from 'react-i18next';

type ApprovalTab = 'todo' | 'done' | 'started';
type ApprovalPriority = 'HIGH' | 'MEDIUM' | 'LOW';

interface ApprovalTask {
  id: string;
  titleKey: string;
  documentNo: string;
  requester: string;
  priority: ApprovalPriority;
  createdAt: string;
  status: 'ACTIVE' | 'DISABLED';
  tab: ApprovalTab;
}

const tasks: ApprovalTask[] = [
  { id: 'task-001', titleKey: 'approval.tasks.purchaseLimit', documentNo: 'PO-202607-001', requester: 'Chen Ops', priority: 'HIGH', createdAt: '2026-07-07 08:30', status: 'ACTIVE', tab: 'todo' },
  { id: 'task-002', titleKey: 'approval.tasks.roleChange', documentNo: 'IAM-202607-003', requester: 'Admin', priority: 'MEDIUM', createdAt: '2026-07-07 09:12', status: 'ACTIVE', tab: 'todo' },
  { id: 'task-003', titleKey: 'approval.tasks.integration', documentNo: 'INT-202607-002', requester: 'System', priority: 'LOW', createdAt: '2026-07-06 17:45', status: 'DISABLED', tab: 'done' },
  { id: 'task-004', titleKey: 'approval.tasks.parameter', documentNo: 'CFG-202607-009', requester: 'Me', priority: 'MEDIUM', createdAt: '2026-07-06 15:20', status: 'ACTIVE', tab: 'started' }
];

const priorityColor = (priority: ApprovalPriority) => {
  if (priority === 'HIGH') return 'error';
  if (priority === 'MEDIUM') return 'warning';
  return 'default';
};

export const ApprovalTaskCenterPage = () => {
  const { t } = useTranslation();
  const [activeTab, setActiveTab] = useState<ApprovalTab>('todo');
  const [selectedTask, setSelectedTask] = useState<ApprovalTask | null>(null);

  const columns: ColumnsType<ApprovalTask> = [
    { title: t('approval.documentNo'), dataIndex: 'documentNo', width: 160 },
    { title: t('approval.taskTitle'), dataIndex: 'titleKey', render: (key) => t(key) },
    { title: t('approval.requester'), dataIndex: 'requester', width: 140 },
    { title: t('approval.priority'), dataIndex: 'priority', width: 120, render: (priority: ApprovalPriority) => <Tag color={priorityColor(priority)}>{t(`kanban.priority.${priority}`)}</Tag> },
    { title: t('common.fields.createdAt'), dataIndex: 'createdAt', width: 180 },
    { title: t('common.fields.status'), dataIndex: 'status', width: 120, render: (status: ApprovalTask['status']) => <StatusTag status={status} label={t(`common.status.${status}`)} /> },
    {
      title: t('common.fields.actions'),
      width: 140,
      fixed: 'right',
      render: (_, record) => (
        <Button type="link" onClick={() => setSelectedTask(record)}>
          {t('common.actions.view')}
        </Button>
      )
    }
  ];

  const activeTasks = tasks.filter((task) => task.tab === activeTab);
  const finishAction = (actionKey: string) => {
    message.success(t(`approval.feedback.${actionKey}`));
    setSelectedTask(null);
  };

  return (
    <AppPageContainer
      title={t('approval.title')}
      extra={
        <Space>
          <Tag icon={<ClockCircleOutlined />} color="processing">
            {t('approval.todoCount', { count: tasks.filter((task) => task.tab === 'todo').length })}
          </Tag>
        </Space>
      }
    >
      <IafSurface
        title={
          <IafSectionHeader
            title={t('approval.listTitle')}
            description={t('approval.listDescription')}
            extra={<IafStatusPill tone="info">{t('platformConfig.mockFirst')}</IafStatusPill>}
          />
        }
      >
        <Tabs
          activeKey={activeTab}
          onChange={(key) => setActiveTab(key as ApprovalTab)}
          items={[
            { key: 'todo', label: t('approval.tabs.todo') },
            { key: 'done', label: t('approval.tabs.done') },
            { key: 'started', label: t('approval.tabs.started') }
          ]}
        />
        <Table rowKey="id" size="small" bordered columns={columns} dataSource={activeTasks} scroll={{ x: 'max-content' }} pagination={false} />
      </IafSurface>

      <Drawer
        open={Boolean(selectedTask)}
        title={selectedTask ? t(selectedTask.titleKey) : undefined}
        width={860}
        styles={{ wrapper: { maxWidth: '90vw' } }}
        onClose={() => setSelectedTask(null)}
        footer={
          selectedTask?.tab === 'todo' ? (
            <Space style={{ justifyContent: 'flex-end', width: '100%' }}>
              <Button icon={<RollbackOutlined />} onClick={() => finishAction('returned')}>
                {t('approval.actions.return')}
              </Button>
              <Button danger icon={<CloseOutlined />} onClick={() => finishAction('rejected')}>
                {t('approval.actions.reject')}
              </Button>
              <Button type="primary" icon={<CheckOutlined />} onClick={() => finishAction('approved')}>
                {t('approval.actions.approve')}
              </Button>
            </Space>
          ) : null
        }
      >
        {selectedTask && (
          <Space direction="vertical" size={16} style={{ width: '100%' }}>
            <Descriptions bordered size="small" column={2}>
              <Descriptions.Item label={t('approval.documentNo')}>{selectedTask.documentNo}</Descriptions.Item>
              <Descriptions.Item label={t('approval.requester')}>{selectedTask.requester}</Descriptions.Item>
              <Descriptions.Item label={t('approval.priority')}>
                <Tag color={priorityColor(selectedTask.priority)}>{t(`kanban.priority.${selectedTask.priority}`)}</Tag>
              </Descriptions.Item>
              <Descriptions.Item label={t('common.fields.status')}>
                <StatusTag status={selectedTask.status} label={t(`common.status.${selectedTask.status}`)} />
              </Descriptions.Item>
            </Descriptions>
            <IafSurface compact title={t('approval.fieldPermission')}>
              <Typography.Paragraph style={{ margin: 0 }}>{t('approval.fieldPermissionDescription')}</Typography.Paragraph>
            </IafSurface>
            <IafSurface compact title={t('approval.timeline')}>
              <Timeline
                items={[
                  { dot: <AuditOutlined />, children: t('approval.timelineItems.started') },
                  { dot: <ClockCircleOutlined />, children: t('approval.timelineItems.waiting') }
                ]}
              />
            </IafSurface>
          </Space>
        )}
      </Drawer>
    </AppPageContainer>
  );
};
