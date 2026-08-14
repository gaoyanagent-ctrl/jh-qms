import React, { useEffect, useRef, useState } from 'react';
import { useLocation, useNavigate, useOutlet } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { Tabs, Dropdown, Modal, theme } from 'antd';
import { useRouteTabStore, RouteTab } from './RouteTabStore';
import { useDirtyStateStore } from './DirtyStateRegistry';

const getTabLabelKey = (pathname: string): string => {
  if (pathname === '/') return 'menu.workbench';
  if (pathname === '/platform/users') return 'menu.users';
  if (pathname === '/platform/orgs') return 'menu.orgs';
  if (pathname === '/platform/roles') return 'menu.roles';
  if (pathname === '/platform/menus') return 'menu.menus';
  if (pathname === '/platform/dictionaries') return 'menu.dictionaries';
  if (pathname === '/platform/audit-logs') return 'menu.auditLogs';
  if (pathname === '/platform/approval/tasks') return 'menu.approvalTasks';
  if (pathname === '/platform/kanban') return 'menu.kanban';
  if (pathname === '/qms/engineering/parts') return 'menu.qmsParts';
  if (pathname === '/qms/engineering/drawing-legend') return 'menu.qmsDrawingLegend';
  if (/^\/qms\/engineering\/parts\/\d+$/.test(pathname)) return 'menu.qmsPartDetail';
  if (/^\/qms\/engineering\/drawing-revisions\/\d+\/review$/.test(pathname)) return 'menu.qmsDrawingReview';
  if (/^\/qms\/engineering\/drawing-revisions\/\d+\/inspection-standard$/.test(pathname)) return 'menu.qmsInspectionStandard';
  if (/^\/qms\/engineering\/inspection-standards\/\d+\/validation-plan$/.test(pathname)) return 'menu.qmsValidationPlan';
  return 'menu.unknown';
};

export const TabWorkspace: React.FC = () => {
  const { t } = useTranslation();
  const { token } = theme.useToken();
  const location = useLocation();
  const navigate = useNavigate();
  const outlet = useOutlet();

  const {
    tabs,
    activeTabKey,
    addTab,
    removeTab,
    removeOtherTabs,
    removeRightTabs,
    setActiveTabKey,
    pinTab
  } = useRouteTabStore();

  const [refreshTick, setRefreshTick] = useState(0);
  const componentCache = useRef<Record<string, React.ReactNode>>({});

  const currentKey = location.pathname + location.search;

  // Auto add tab when routing matches
  useEffect(() => {
    const labelKey = getTabLabelKey(location.pathname);
    addTab({
      key: currentKey,
      label: labelKey,
      closable: location.pathname !== '/'
    });
  }, [currentKey, location.pathname, addTab]);

  // Sync active key to router on first mount only when at root path
  const isFirstMount = useRef(true);
  useEffect(() => {
    if (isFirstMount.current) {
      isFirstMount.current = false;
      if (activeTabKey && activeTabKey !== currentKey && currentKey === '/') {
        navigate(activeTabKey);
      }
    }
  }, [activeTabKey, currentKey, navigate]);

  // Handle component KeepAlive caching
  if (outlet && currentKey) {
    if (!componentCache.current[currentKey] || refreshTick === -1) {
      componentCache.current[currentKey] = outlet;
    }
  }

  const isTabDirty = (key: string) => {
    return useDirtyStateStore.getState().dirtyTabs[key] === true;
  };

  const handleClose = (key: string) => {
    if (isTabDirty(key)) {
      Modal.confirm({
        title: t('workspace.confirmCloseTitle'),
        content: t('workspace.confirmCloseContent'),
        okText: t('common.actions.confirm'),
        cancelText: t('common.actions.cancel'),
        onOk: () => {
          delete componentCache.current[key];
          useDirtyStateStore.getState().clearTabDirty(key);
          removeTab(key);
        }
      });
    } else {
      delete componentCache.current[key];
      removeTab(key);
    }
  };

  const handleMenuClick = (key: string, action: string) => {
    switch (action) {
      case 'refresh':
        delete componentCache.current[key];
        setRefreshTick((tick) => tick + 1);
        break;
      case 'close':
        handleClose(key);
        break;
      case 'closeOthers': {
        const dirtyOthers = tabs.some((t) => t.key !== key && t.closable && isTabDirty(t.key));
        if (dirtyOthers) {
          Modal.confirm({
            title: t('workspace.confirmCloseTitle'),
            content: t('workspace.confirmCloseContent'),
            onOk: () => {
              tabs.forEach((t) => {
                if (t.key !== key && t.closable) {
                  delete componentCache.current[t.key];
                  useDirtyStateStore.getState().clearTabDirty(t.key);
                }
              });
              removeOtherTabs(key);
            }
          });
        } else {
          tabs.forEach((t) => {
            if (t.key !== key && t.closable) {
              delete componentCache.current[t.key];
            }
          });
          removeOtherTabs(key);
        }
        break;
      }
      case 'closeRight': {
        const index = tabs.findIndex((t) => t.key === key);
        if (index === -1) return;
        const rightTabs = tabs.slice(index + 1);
        const dirtyRight = rightTabs.some((t) => t.closable && isTabDirty(t.key));

        if (dirtyRight) {
          Modal.confirm({
            title: t('workspace.confirmCloseTitle'),
            content: t('workspace.confirmCloseContent'),
            onOk: () => {
              rightTabs.forEach((t) => {
                if (t.closable) {
                  delete componentCache.current[t.key];
                  useDirtyStateStore.getState().clearTabDirty(t.key);
                }
              });
              removeRightTabs(key);
            }
          });
        } else {
          rightTabs.forEach((t) => {
            if (t.closable) {
              delete componentCache.current[t.key];
            }
          });
          removeRightTabs(key);
        }
        break;
      }
      case 'pin':
        pinTab(key, true);
        break;
      case 'unpin':
        pinTab(key, false);
        break;
      default:
        break;
    }
  };

  const renderTabTitle = (tab: RouteTab) => {
    const isCurrent = tab.key === currentKey;
    const items = [
      { key: 'refresh', label: t('workspace.refresh') },
      tab.closable ? { key: 'close', label: t('workspace.close') } : null,
      { key: 'closeOthers', label: t('workspace.closeOthers') },
      { key: 'closeRight', label: t('workspace.closeRight') },
      tab.fixed
        ? { key: 'unpin', label: t('workspace.unpin') }
        : { key: 'pin', label: t('workspace.pin') }
    ].filter(Boolean) as any[];

    const displayText = tab.label.includes('.') ? t(tab.label) : tab.label;

    return (
      <Dropdown
        menu={{
          items,
          onClick: ({ key }) => handleMenuClick(tab.key, key)
        }}
        trigger={['contextMenu']}
      >
        <span style={{ cursor: 'pointer', fontWeight: isCurrent ? 'bold' : 'normal' }}>
          {displayText}
        </span>
      </Dropdown>
    );
  };

  const tabItems = tabs.map((tab) => {
    const element = componentCache.current[tab.key] || null;
    return {
      key: tab.key,
      label: renderTabTitle(tab),
      closable: tab.closable,
      children: (
        <div
          key={tab.key}
          style={{ display: tab.key === currentKey ? 'block' : 'none' }}
        >
          {element}
        </div>
      )
    };
  });

  return (
    <Tabs
      activeKey={currentKey}
      onChange={(key) => {
        setActiveTabKey(key);
        navigate(key);
      }}
      type="editable-card"
      hideAdd
      onEdit={(targetKey, action) => {
        if (action === 'remove') {
          handleClose(targetKey as string);
        }
      }}
      items={tabItems}
      style={{
        background: token.colorBgContainer,
        padding: token.padding,
        borderRadius: token.borderRadius,
        minHeight: 'calc(100vh - 120px)'
      }}
    />
  );
};
