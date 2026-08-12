import { createContext, useContext, type ReactNode } from 'react';

export interface PageContextValue {
  module: string;
  pageType: 'list' | 'form' | 'detail' | 'operation' | 'designer';
  objectType?: string;
  objectId?: string;
  routePath: string;
}

export interface PageAIContextValue {
  module: string;
  objectType?: string;
  objectId?: string;
  pageType: 'list' | 'form' | 'detail' | 'task' | 'designer' | 'dashboard';
  currentStatus?: Record<string, string>;
  permissions: string[];
  availableActions: string[];
  visibleFields: string[];
  validationErrors?: string[];
  routePath: string;
}

const PageContextRegistry = createContext<{ page: PageContextValue; ai: PageAIContextValue } | null>(null);

export const QmsPageContextProvider = ({
  page,
  ai,
  children
}: {
  page: PageContextValue;
  ai: PageAIContextValue;
  children: ReactNode;
}) => (
  <PageContextRegistry.Provider value={{ page, ai }}>
    <div data-page-module={page.module} data-page-type={page.pageType} style={{ width: '100%' }}>
      {children}
    </div>
  </PageContextRegistry.Provider>
);

export const useQmsPageContext = () => useContext(PageContextRegistry);

export const buildPageAIContext = (context: PageAIContextValue): PageAIContextValue => ({
  ...context,
  permissions: [...context.permissions],
  availableActions: [...context.availableActions],
  visibleFields: [...context.visibleFields]
});
