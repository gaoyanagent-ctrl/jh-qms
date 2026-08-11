-- V0002__seed_platform_management_permissions.sql
-- Seed permission codes for the user / org / role management APIs added in
-- TASK-0101, and bind them to the existing `platform_admin` role.
-- Idempotent: safe to re-run on databases that already contain partial seed data.
--
-- Reference: docs/operations/TASK-0101_next_agent_execution_plan.md Section 5.

insert into sys_permission (id, tenant_id, permission_code, permission_name, resource_type, module_code, action_code)
values
    (2,  1, 'platform:user:view',             'View platform users',           'API', 'platform', 'view'),
    (3,  1, 'platform:user:create',           'Create platform user',          'API', 'platform', 'create'),
    (4,  1, 'platform:user:update',           'Update platform user',          'API', 'platform', 'update'),
    (5,  1, 'platform:user:disable',          'Disable platform user',         'API', 'platform', 'disable'),
    (6,  1, 'platform:user:reset-password',   'Reset platform user password',  'API', 'platform', 'reset-password'),
    (7,  1, 'platform:org:view',              'View platform organizations',   'API', 'platform', 'view'),
    (8,  1, 'platform:org:create',            'Create platform organization',  'API', 'platform', 'create'),
    (9,  1, 'platform:org:update',            'Update platform organization',  'API', 'platform', 'update'),
    (10, 1, 'platform:role:view',             'View platform roles',           'API', 'platform', 'view'),
    (11, 1, 'platform:role:create',           'Create platform role',          'API', 'platform', 'create'),
    (12, 1, 'platform:role:update',           'Update platform role',          'API', 'platform', 'update'),
    (13, 1, 'platform:role:assign-permission','Assign permissions to role',    'API', 'platform', 'assign-permission')
on conflict (id) do nothing;

-- Bind every newly seeded permission to the seeded `platform_admin` role (id=1)
-- if the binding does not already exist. Idempotent against re-runs.
insert into sys_role_permission (tenant_id, role_id, permission_id)
select 1, 1, p.id
from sys_permission p
where p.tenant_id = 1
  and p.permission_code in (
      'platform:user:view',
      'platform:user:create',
      'platform:user:update',
      'platform:user:disable',
      'platform:user:reset-password',
      'platform:org:view',
      'platform:org:create',
      'platform:org:update',
      'platform:role:view',
      'platform:role:create',
      'platform:role:update',
      'platform:role:assign-permission'
  )
on conflict (tenant_id, role_id, permission_id) do nothing;
