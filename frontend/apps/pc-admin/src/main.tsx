import { initIafI18n } from '@iaf/i18n';
import { IafThemeProvider } from '@iaf/theme';
import { App as AntApp } from 'antd';
import React from 'react';
import ReactDOM from 'react-dom/client';
import { App } from './App';
import { loadIafPcAdminBrandConfig } from './config/brandConfig';
import './global.css';

initIafI18n().then(async () => {
  const brandConfig = await loadIafPcAdminBrandConfig();

  ReactDOM.createRoot(document.getElementById('root') as HTMLElement).render(
    <React.StrictMode>
      <IafThemeProvider brandConfig={brandConfig}>
        <AntApp>
          <App />
        </AntApp>
      </IafThemeProvider>
    </React.StrictMode>
  );
});
