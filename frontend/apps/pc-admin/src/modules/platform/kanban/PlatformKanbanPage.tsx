import { AppPageContainer, StatusTag } from '@iaf/ui-core';
import { useIafTheme } from '@iaf/theme';
import {
  AlertOutlined,
  AuditOutlined,
  CheckCircleOutlined,
  ClockCircleOutlined,
  DragOutlined,
  FilterOutlined,
  UserOutlined
} from '@ant-design/icons';
import { Badge, Button, Card, Drawer, Empty, Input, Segmented, Select, Space, Tag, Typography, theme } from 'antd';
import { useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';

type KanbanColumnKey = 'pending' | 'processing' | 'done';

interface KanbanCard {
  id: string;
  titleKey: string;
  owner: string;
  priority: 'HIGH' | 'MEDIUM' | 'LOW';
  dueText: string;
  status: 'ACTIVE' | 'DISABLED';
  columnKey: KanbanColumnKey;
  source: 'approval' | 'exception' | 'workflow' | 'sync';
}

interface KanbanColumn {
  key: KanbanColumnKey;
  titleKey: string;
  wipLimit: number;
}

const columns: KanbanColumn[] = [
  { key: 'pending', titleKey: 'kanban.columns.pending', wipLimit: 4 },
  { key: 'processing', titleKey: 'kanban.columns.processing', wipLimit: 3 },
  { key: 'done', titleKey: 'kanban.columns.done', wipLimit: 8 }
];

const initialCards: KanbanCard[] = [
  {
    id: 'approval-001',
    titleKey: 'kanban.cards.approval',
    owner: 'Admin',
    priority: 'HIGH',
    dueText: '2h',
    status: 'ACTIVE',
    columnKey: 'pending',
    source: 'approval'
  },
  {
    id: 'exception-001',
    titleKey: 'kanban.cards.exception',
    owner: 'Ops',
    priority: 'MEDIUM',
    dueText: '4h',
    status: 'ACTIVE',
    columnKey: 'pending',
    source: 'exception'
  },
  {
    id: 'workflow-001',
    titleKey: 'kanban.cards.workflow',
    owner: 'Platform',
    priority: 'MEDIUM',
    dueText: '1d',
    status: 'ACTIVE',
    columnKey: 'processing',
    source: 'workflow'
  },
  {
    id: 'sync-001',
    titleKey: 'kanban.cards.sync',
    owner: 'System',
    priority: 'LOW',
    dueText: 'Done',
    status: 'DISABLED',
    columnKey: 'done',
    source: 'sync'
  }
];

const priorityColor = (priority: KanbanCard['priority']) => {
  if (priority === 'HIGH') return 'error';
  if (priority === 'MEDIUM') return 'warning';
  return 'default';
};

const sourceIcon = (source: KanbanCard['source']) => {
  if (source === 'approval') return <AuditOutlined />;
  if (source === 'exception') return <AlertOutlined />;
  if (source === 'workflow') return <ClockCircleOutlined />;
  return <CheckCircleOutlined />;
};

export const PlatformKanbanPage = () => {
  const { t } = useTranslation();
  const { token } = theme.useToken();
  const { designTokens } = useIafTheme();
  const [cards, setCards] = useState<KanbanCard[]>(initialCards);
  const [draggingId, setDraggingId] = useState<string | null>(null);
  const [selectedCard, setSelectedCard] = useState<KanbanCard | null>(null);
  const [priorityFilter, setPriorityFilter] = useState<KanbanCard['priority'] | 'ALL'>('ALL');
  const [keyword, setKeyword] = useState('');

  const filteredCards = useMemo(() => {
    const normalized = keyword.trim().toLowerCase();
    return cards.filter((card) => {
      const matchesPriority = priorityFilter === 'ALL' || card.priority === priorityFilter;
      const matchesKeyword =
        !normalized ||
        card.id.toLowerCase().includes(normalized) ||
        t(card.titleKey).toLowerCase().includes(normalized) ||
        card.owner.toLowerCase().includes(normalized);
      return matchesPriority && matchesKeyword;
    });
  }, [cards, keyword, priorityFilter, t]);

  const moveCard = (cardId: string, nextColumnKey: KanbanColumnKey) => {
    setCards((current) =>
      current.map((card) => (card.id === cardId ? { ...card, columnKey: nextColumnKey, status: nextColumnKey === 'done' ? 'DISABLED' : 'ACTIVE' } : card))
    );
    setDraggingId(null);
  };

  return (
    <AppPageContainer
      title={t('kanban.title')}
      extra={
        <Space wrap>
          <Input
            allowClear
            prefix={<FilterOutlined />}
            aria-label={t('kanban.filterKeyword')}
            placeholder={t('kanban.filterKeyword')}
            value={keyword}
            onChange={(event) => setKeyword(event.target.value)}
            style={{ width: 240 }}
          />
          <Select
            value={priorityFilter}
            style={{ width: 150 }}
            onChange={setPriorityFilter}
            options={[
              { label: t('kanban.priority.ALL'), value: 'ALL' },
              { label: t('kanban.priority.HIGH'), value: 'HIGH' },
              { label: t('kanban.priority.MEDIUM'), value: 'MEDIUM' },
              { label: t('kanban.priority.LOW'), value: 'LOW' }
            ]}
          />
          <Segmented
            value="operations"
            options={[{ label: t('kanban.views.operations'), value: 'operations' }]}
          />
          <Button type="primary">{t('common.actions.refresh')}</Button>
        </Space>
      }
    >
      <div
        style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(3, minmax(280px, 1fr))',
          gap: token.margin,
          overflowX: 'auto',
          paddingBottom: token.paddingSM
        }}
      >
        {columns.map((column) => {
          const columnCards = filteredCards.filter((card) => card.columnKey === column.key);
          const isOverLimit = columnCards.length > column.wipLimit;

          return (
            <Card
              key={column.key}
              title={
                <Space>
                  <Typography.Text strong>{t(column.titleKey)}</Typography.Text>
                  <Tag color={isOverLimit ? 'error' : 'default'}>
                    {columnCards.length}/{column.wipLimit}
                  </Tag>
                </Space>
              }
              extra={<Badge status={isOverLimit ? 'error' : 'processing'} text={isOverLimit ? t('kanban.wipOverLimit') : t('kanban.wipNormal')} />}
              onDragOver={(event) => event.preventDefault()}
              onDrop={() => draggingId && moveCard(draggingId, column.key)}
              styles={{
                body: {
                  minHeight: 440,
                  background: designTokens.kanban.columnBg,
                  padding: token.paddingSM
                }
              }}
              style={{
                borderColor: isOverLimit ? token.colorErrorBorder : token.colorBorderSecondary,
                boxShadow: designTokens.elevation.level1
              }}
            >
              <Space direction="vertical" size={12} style={{ width: '100%' }}>
                {columnCards.length === 0 ? (
                  <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description={t('kanban.emptyColumn')} />
                ) : (
                  columnCards.map((card) => (
                    <Card
                      key={card.id}
                      size="small"
                      hoverable
                      draggable
                      onDragStart={() => setDraggingId(card.id)}
                      onDragEnd={() => setDraggingId(null)}
                      onClick={() => setSelectedCard(card)}
                      styles={{ body: { padding: token.padding } }}
                      style={{
                        cursor: 'grab',
                        background: designTokens.kanban.cardBg,
                        borderColor: draggingId === card.id ? designTokens.kanban.cardDraggingBorder : token.colorBorderSecondary,
                        boxShadow: draggingId === card.id ? designTokens.elevation.level2 : token.boxShadowTertiary
                      }}
                    >
                      <Space direction="vertical" size={8} style={{ width: '100%' }}>
                        <Space align="center" style={{ justifyContent: 'space-between', width: '100%' }}>
                          <Space>
                            <DragOutlined style={{ color: token.colorTextTertiary }} />
                            <Typography.Text strong>{t(card.titleKey)}</Typography.Text>
                          </Space>
                          <StatusTag status={card.status} label={t(`common.status.${card.status}`)} />
                        </Space>
                        <Typography.Text type="secondary">{card.id}</Typography.Text>
                        <Space wrap>
                          <Tag icon={sourceIcon(card.source)}>{t(`kanban.sources.${card.source}`)}</Tag>
                          <Tag color={priorityColor(card.priority)}>{t(`kanban.priority.${card.priority}`)}</Tag>
                          <Tag icon={<UserOutlined />}>{card.owner}</Tag>
                          <Tag icon={<ClockCircleOutlined />}>{card.dueText}</Tag>
                        </Space>
                      </Space>
                    </Card>
                  ))
                )}
              </Space>
            </Card>
          );
        })}
      </div>

      <Drawer
        open={Boolean(selectedCard)}
        title={selectedCard ? t(selectedCard.titleKey) : undefined}
        width={720}
        styles={{ wrapper: { maxWidth: '90vw' } }}
        onClose={() => setSelectedCard(null)}
      >
        {selectedCard && (
          <Space direction="vertical" size={16} style={{ width: '100%' }}>
            <Space wrap>
              <Tag>{selectedCard.id}</Tag>
              <Tag color={priorityColor(selectedCard.priority)}>{t(`kanban.priority.${selectedCard.priority}`)}</Tag>
              <StatusTag status={selectedCard.status} label={t(`common.status.${selectedCard.status}`)} />
            </Space>
            <Card title={t('kanban.detail.owner')} size="small">
              <Space>
                <UserOutlined />
                <Typography.Text>{selectedCard.owner}</Typography.Text>
              </Space>
            </Card>
            <Card title={t('kanban.detail.source')} size="small">
              <Space>
                {sourceIcon(selectedCard.source)}
                <Typography.Text>{t(`kanban.sources.${selectedCard.source}`)}</Typography.Text>
              </Space>
            </Card>
            <Card title={t('kanban.detail.nextAction')} size="small">
              <Typography.Paragraph style={{ margin: 0 }}>
                {t(`kanban.detailActions.${selectedCard.source}`)}
              </Typography.Paragraph>
            </Card>
          </Space>
        )}
      </Drawer>
    </AppPageContainer>
  );
};
