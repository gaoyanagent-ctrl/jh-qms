#!/usr/bin/env node
const fs = require('fs');
const path = require('path');

const root = path.resolve(__dirname, '..');
const templatePath = path.join(root, 'ai-coding/templates/platform-foundation/platform-foundation-delivery-package.json');
const permissionsPath = path.join(root, 'frontend/packages/permissions/src/index.tsx');
const migrationsDir = path.join(root, 'backend/iaf-app/src/main/resources/db/migration');

const readJson = (filePath) => JSON.parse(fs.readFileSync(filePath, 'utf8'));
const fail = (message) => {
  console.error(`Platform foundation template check failed: ${message}`);
  process.exit(1);
};

const unique = (values, label) => {
  const seen = new Set();
  values.forEach((value) => {
    if (seen.has(value)) {
      fail(`duplicate ${label}: ${value}`);
    }
    seen.add(value);
  });
  return seen;
};

const template = readJson(templatePath);
const permissionSource = fs.readFileSync(permissionsPath, 'utf8');
const frontendPermissionCodes = [...permissionSource.matchAll(/:\s*'([^']+)'/g)]
  .map((match) => match[1])
  .filter((code) => code.startsWith('platform:'));
const backendSeedPermissionCodes = fs.readdirSync(migrationsDir)
  .filter((fileName) => fileName.endsWith('.sql'))
  .flatMap((fileName) => {
    const migration = fs.readFileSync(path.join(migrationsDir, fileName), 'utf8');
    return [...migration.matchAll(/platform:[a-z0-9:-]+/g)].map((match) => match[0]);
  });

const templatePermissionCodes = template.permissions.map((permission) => permission.code);
const templatePermissionSet = unique(templatePermissionCodes, 'permission code');
const frontendPermissionSet = unique(frontendPermissionCodes, 'frontend permission code');
const backendSeedPermissionSet = new Set(backendSeedPermissionCodes);
const knownPermissionSet = new Set([...frontendPermissionSet, ...backendSeedPermissionSet]);

const frontendMissingInTemplate = [...frontendPermissionSet].filter((code) => !templatePermissionSet.has(code));
const backendSeedMissingInTemplate = [...backendSeedPermissionSet].filter((code) => !templatePermissionSet.has(code));
const unknownTemplatePermissions = [...templatePermissionSet].filter((code) => !knownPermissionSet.has(code));

if (frontendMissingInTemplate.length > 0) {
  fail(`frontend permissions missing from template: ${frontendMissingInTemplate.join(', ')}`);
}

if (backendSeedMissingInTemplate.length > 0) {
  fail(`backend seed permissions missing from template: ${backendSeedMissingInTemplate.join(', ')}`);
}

if (unknownTemplatePermissions.length > 0) {
  fail(`template permissions missing from frontend constants and backend seeds: ${unknownTemplatePermissions.join(', ')}`);
}

const menuCodes = unique(template.menus.map((menu) => menu.menuCode), 'menu code');
const roleCodes = unique(template.roles.map((role) => role.roleCode), 'role code');
const rolesByCode = new Map(template.roles.map((role) => [role.roleCode, role]));
const rolePermissionCoverage = new Set();

template.permissions.forEach((permission) => {
  if (!Array.isArray(permission.recommendedRoles) || permission.recommendedRoles.length === 0) {
    fail(`permission has no recommended roles: ${permission.code}`);
  }
  permission.recommendedRoles.forEach((roleCode) => {
    if (!roleCodes.has(roleCode)) {
      fail(`permission ${permission.code} references unknown role ${roleCode}`);
    }
    const role = rolesByCode.get(roleCode);
    if (!role.permissionCodes.includes('*') && !role.permissionCodes.includes(permission.code)) {
      fail(`permission ${permission.code} recommends role ${roleCode}, but the role template does not grant it`);
    }
  });
  permission.menus.forEach((menuCode) => {
    if (!menuCodes.has(menuCode)) {
      fail(`permission ${permission.code} references unknown menu ${menuCode}`);
    }
  });
});

template.roles.forEach((role) => {
  role.menuCodes.forEach((menuCode) => {
    if (!menuCodes.has(menuCode)) {
      fail(`role ${role.roleCode} references unknown menu ${menuCode}`);
    }
  });

  if (role.permissionCodes.includes('*')) {
    templatePermissionCodes.forEach((code) => rolePermissionCoverage.add(code));
    return;
  }

  role.permissionCodes.forEach((permissionCode) => {
    if (!templatePermissionSet.has(permissionCode)) {
      fail(`role ${role.roleCode} references unknown permission ${permissionCode}`);
    }
    rolePermissionCoverage.add(permissionCode);
  });
});

template.menus.forEach((menu) => {
  if (menu.parentCode && !menuCodes.has(menu.parentCode)) {
    fail(`menu ${menu.menuCode} references unknown parent ${menu.parentCode}`);
  }

  menu.permissionCodes.forEach((permissionCode) => {
    if (!templatePermissionSet.has(permissionCode)) {
      fail(`menu ${menu.menuCode} references unknown permission ${permissionCode}`);
    }
  });

  if (menu.routePath && menu.status === 'production-backed' && menu.permissionCodes.length === 0) {
    fail(`production-backed route menu has no permission guard: ${menu.menuCode}`);
  }
});

const uncoveredPermissions = templatePermissionCodes.filter((code) => !rolePermissionCoverage.has(code));
if (uncoveredPermissions.length > 0) {
  fail(`permissions not assigned to any role template: ${uncoveredPermissions.join(', ')}`);
}

if (!Array.isArray(template.designAcceptanceMatrix) || template.designAcceptanceMatrix.length < 8) {
  fail('design acceptance matrix is missing core platform pages');
}

if (!Array.isArray(template.regressionMatrix) || template.regressionMatrix.length < 10) {
  fail('regression matrix is too small for platform foundation coverage');
}

console.log(`Platform foundation template check passed: ${templatePermissionCodes.length} permissions, ${frontendPermissionSet.size} frontend permissions, ${backendSeedPermissionSet.size} backend seed permissions, ${template.roles.length} roles, ${template.menus.length} menus.`);
