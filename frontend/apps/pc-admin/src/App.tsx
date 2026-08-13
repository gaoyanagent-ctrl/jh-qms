import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { PermissionRoute, PLATFORM_PERMISSIONS, QMS_PERMISSIONS } from '@iaf/permissions';
import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import { MainLayout } from './layouts/MainLayout';
import { LoginPage } from './pages/LoginPage';
import { WorkbenchPage } from './pages/WorkbenchPage';
import { OrgTreePage } from './modules/platform/orgs/OrgTreePage';
import { RoleListPage } from './modules/platform/roles/RoleListPage';
import { UserListPage } from './modules/platform/users/UserListPage';
import { PlatformKanbanPage } from './modules/platform/kanban/PlatformKanbanPage';
import { ApprovalTaskCenterPage } from './modules/platform/approval/ApprovalTaskCenterPage';
import { PlatformAuditLogPage, PlatformDictionaryParameterPage } from './modules/platform/config/PlatformConfigPages';
import { PlatformMenuConsolePage } from './modules/platform/menus/PlatformMenuConsolePage';
import { ProtectedRoute } from './routes/ProtectedRoute';
import { QmsPartListPage } from './modules/qms/engineering/QmsPartListPage';
import { QmsPartDetailPage } from './modules/qms/engineering/QmsPartDetailPage';
import { QmsDrawingReviewWorkbenchPage } from './modules/qms/engineering/QmsDrawingReviewWorkbenchPage';
import { QmsDrawingLegendConfigPage } from './modules/qms/engineering/QmsDrawingLegendConfigPage';
import { QmsInspectionStandardPage } from './modules/qms/engineering/QmsInspectionStandardPage';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      staleTime: 30_000
    }
  }
});

export const App = () => (
  <QueryClientProvider client={queryClient}>
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route element={<ProtectedRoute />}>
          <Route element={<MainLayout />}>
            <Route index element={<WorkbenchPage />} />
            <Route element={<PermissionRoute require={PLATFORM_PERMISSIONS.userView} fallbackPath="/" />}>
              <Route path="/platform/users" element={<UserListPage />} />
            </Route>
            <Route element={<PermissionRoute require={PLATFORM_PERMISSIONS.orgView} fallbackPath="/" />}>
              <Route path="/platform/orgs" element={<OrgTreePage />} />
            </Route>
            <Route element={<PermissionRoute require={PLATFORM_PERMISSIONS.roleView} fallbackPath="/" />}>
              <Route path="/platform/roles" element={<RoleListPage />} />
            </Route>
            <Route path="/platform/kanban" element={<PlatformKanbanPage />} />
            <Route element={<PermissionRoute require={PLATFORM_PERMISSIONS.menuView} fallbackPath="/" />}>
              <Route path="/platform/menus" element={<PlatformMenuConsolePage />} />
            </Route>
            <Route element={<PermissionRoute require={[PLATFORM_PERMISSIONS.dictionaryView, PLATFORM_PERMISSIONS.parameterView]} fallbackPath="/" />}>
              <Route path="/platform/dictionaries" element={<PlatformDictionaryParameterPage />} />
            </Route>
            <Route element={<PermissionRoute require={PLATFORM_PERMISSIONS.auditView} fallbackPath="/" />}>
              <Route path="/platform/audit-logs" element={<PlatformAuditLogPage />} />
            </Route>
            <Route path="/platform/approval/tasks" element={<ApprovalTaskCenterPage />} />
            <Route element={<PermissionRoute require={QMS_PERMISSIONS.partView} fallbackPath="/" />}>
              <Route path="/qms/engineering/parts" element={<QmsPartListPage />} />
              <Route path="/qms/engineering/parts/:partId" element={<QmsPartDetailPage />} />
              <Route path="/qms/engineering/drawing-revisions/:revisionId/review" element={<QmsDrawingReviewWorkbenchPage />} />
              <Route path="/qms/engineering/drawing-revisions/:revisionId/inspection-standard" element={<QmsInspectionStandardPage />} />
            </Route>
            <Route element={<PermissionRoute require={QMS_PERMISSIONS.drawingLegendManage} fallbackPath="/" />}>
              <Route path="/qms/engineering/drawing-legend" element={<QmsDrawingLegendConfigPage />} />
            </Route>
          </Route>
        </Route>
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  </QueryClientProvider>
);
