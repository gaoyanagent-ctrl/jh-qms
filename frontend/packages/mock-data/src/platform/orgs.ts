import { mockSuccess, MockApiAdapter, MockRequest } from '@iaf/api-client';
import type { PlatformOrg } from '@iaf/domain-types';

let orgTree: PlatformOrg[] = [
  {
    id: 1,
    parentId: null,
    orgCode: 'CORP',
    orgName: 'IAF Industrial Corp',
    orgType: 'COMPANY',
    status: 'ACTIVE',
    sortNo: 1,
    children: [
      {
        id: 2,
        parentId: 1,
        orgCode: 'WMS_DEPT',
        orgName: 'Warehouse Department',
        orgType: 'DEPARTMENT',
        status: 'ACTIVE',
        sortNo: 1,
        children: []
      },
      {
        id: 3,
        parentId: 1,
        orgCode: 'PROD_DEPT',
        orgName: 'Production Department',
        orgType: 'DEPARTMENT',
        status: 'ACTIVE',
        sortNo: 2,
        children: []
      }
    ]
  }
];

// Helper to find and add org
const findAndAddOrg = (nodes: PlatformOrg[], parentId: number, newOrg: PlatformOrg): boolean => {
  for (const node of nodes) {
    if (node.id === parentId) {
      node.children = node.children || [];
      node.children.push(newOrg);
      return true;
    }
    if (node.children && findAndAddOrg(node.children, parentId, newOrg)) {
      return true;
    }
  }
  return false;
};

// Helper to find and update org
const findAndUpdateOrg = (nodes: PlatformOrg[], id: number, updated: Partial<PlatformOrg>): boolean => {
  for (const node of nodes) {
    if (node.id === id) {
      Object.assign(node, updated);
      return true;
    }
    if (node.children && findAndUpdateOrg(node.children, id, updated)) {
      return true;
    }
  }
  return false;
};

// Helper to calculate next ID
const getMaxId = (nodes: PlatformOrg[]): number => {
  let max = 0;
  for (const node of nodes) {
    if (node.id > max) max = node.id;
    if (node.children) {
      const childMax = getMaxId(node.children);
      if (childMax > max) max = childMax;
    }
  }
  return max;
};

const flattenOrgNode = (items: PlatformOrg[]): PlatformOrg[] =>
  items.flatMap((item) => [item, ...flattenOrgNode(item.children ?? [])]);

export const registerOrgMocks = (adapter: MockApiAdapter) => {
  adapter.register('GET', '/api/platform/orgs/tree', () => {
    return mockSuccess(orgTree);
  });

  adapter.register('POST', '/api/platform/orgs', (req: MockRequest) => {
    const body = req.body;
    const maxId = getMaxId(orgTree);
    const newOrg: PlatformOrg = {
      id: maxId + 1,
      parentId: body.parentId || null,
      orgCode: body.orgCode,
      orgName: body.orgName,
      orgType: body.orgType,
      status: body.status || 'ACTIVE',
      sortNo: body.sortNo || 0,
      children: []
    };

    if (newOrg.parentId === null) {
      orgTree.push(newOrg);
    } else {
      findAndAddOrg(orgTree, newOrg.parentId, newOrg);
    }

    return mockSuccess(newOrg);
  });

  adapter.register('PUT', '/api/platform/orgs/:id', (req: MockRequest) => {
    const id = parseInt(req.pathParams.id, 10);
    const body = req.body;

    findAndUpdateOrg(orgTree, id, {
      parentId: body.parentId,
      orgCode: body.orgCode,
      orgName: body.orgName,
      orgType: body.orgType,
      status: body.status,
      sortNo: body.sortNo
    });

    const flat = flattenOrgNode(orgTree);
    const updatedNode = flat.find(n => n.id === id);

    return mockSuccess(updatedNode);
  });
};
