insert into sys_permission (tenant_id, permission_code, permission_name, resource_type, module_code, action_code)
values
    (1, 'platform:data-permission:view', 'View data permission rules', 'API', 'platform', 'data-permission:view'),
    (1, 'platform:data-permission:update', 'Update data permission rules', 'API', 'platform', 'data-permission:update'),
    (1, 'platform:field-permission:view', 'View field permission rules', 'API', 'platform', 'field-permission:view'),
    (1, 'platform:field-permission:update', 'Update field permission rules', 'API', 'platform', 'field-permission:update'),
    (1, 'platform:dictionary:view', 'View dictionaries', 'API', 'platform', 'dictionary:view'),
    (1, 'platform:dictionary:update', 'Update dictionaries', 'API', 'platform', 'dictionary:update'),
    (1, 'platform:parameter:view', 'View system parameters', 'API', 'platform', 'parameter:view'),
    (1, 'platform:parameter:update', 'Update system parameters', 'API', 'platform', 'parameter:update'),
    (1, 'platform:audit:view', 'View audit logs', 'API', 'platform', 'audit:view')
on conflict (tenant_id, permission_code) do nothing;

insert into sys_role_permission (tenant_id, role_id, permission_id)
select 1, r.id, p.id
  from sys_role r
  join sys_permission p on p.tenant_id = r.tenant_id
 where r.tenant_id = 1
   and r.role_code = 'platform_admin'
   and p.permission_code in (
       'platform:data-permission:view',
       'platform:data-permission:update',
       'platform:field-permission:view',
       'platform:field-permission:update',
       'platform:dictionary:view',
       'platform:dictionary:update',
       'platform:parameter:view',
       'platform:parameter:update',
       'platform:audit:view'
   )
on conflict (tenant_id, role_id, permission_id) do nothing;

insert into sys_menu_permission (tenant_id, menu_id, permission_id, created_by, updated_by)
select 1, m.id, p.id, 1, 1
  from (
      values
        ('platform.dictionaries', 'platform:dictionary:view'),
        ('platform.dictionaries', 'platform:parameter:view'),
        ('platform.auditLogs', 'platform:audit:view')
  ) as mp(menu_code, permission_code)
  join sys_menu m on m.tenant_id = 1 and m.menu_code = mp.menu_code
  join sys_permission p on p.tenant_id = 1 and p.permission_code = mp.permission_code
on conflict (tenant_id, menu_id, permission_id) do nothing;
