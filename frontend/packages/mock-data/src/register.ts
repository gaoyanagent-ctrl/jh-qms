import { MockApiAdapter } from '@iaf/api-client';
import { registerAuthMocks } from './platform/auth';
import { registerUserMocks } from './platform/users';
import { registerOrgMocks } from './platform/orgs';
import { registerRoleMocks } from './platform/roles';
import { registerMenuMocks } from './platform/menus';
import { registerSystemConfigMocks } from './platform/systemConfig';
import { registerReceiptOrderMocks } from './wms/receiptOrders';
import { registerQmsEngineeringMocks } from './qms/engineering';

export const registerMocks = (adapter: MockApiAdapter) => {
  registerAuthMocks(adapter);
  registerUserMocks(adapter);
  registerOrgMocks(adapter);
  registerRoleMocks(adapter);
  registerMenuMocks(adapter);
  registerSystemConfigMocks(adapter);
  registerReceiptOrderMocks(adapter);
  registerQmsEngineeringMocks(adapter);
};
