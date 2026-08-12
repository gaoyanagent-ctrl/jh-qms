insert into sys_menu (
    tenant_id, parent_id, menu_code, menu_type, title_key, route_path,
    component_key, icon, sort_no, visible, enabled, created_by, updated_by
)
select t.id, null, 'qms', 'GROUP', 'menu.qms', null,
       null, 'AppstoreOutlined', 400, true, true, 0, 0
  from sys_tenant t
on conflict (tenant_id, menu_code) do nothing;

insert into sys_menu (
    tenant_id, parent_id, menu_code, menu_type, title_key, route_path,
    component_key, icon, sort_no, visible, enabled, created_by, updated_by
)
select t.id, parent.id, 'qms.engineering.parts', 'MENU', 'menu.qmsParts',
       '/qms/engineering/parts', 'qms/engineering/QmsPartListPage',
       'FileSearchOutlined', 410, true, true, 0, 0
  from sys_tenant t
  join sys_menu parent
    on parent.tenant_id = t.id
   and parent.menu_code = 'qms'
   and parent.deleted = false
on conflict (tenant_id, menu_code) do nothing;

insert into sys_menu_permission (
    tenant_id, menu_id, permission_id, created_by, updated_by
)
select menu.tenant_id, menu.id, permission.id, 0, 0
  from sys_menu menu
  join sys_permission permission
    on permission.tenant_id = menu.tenant_id
   and permission.permission_code = 'qms:part:view'
   and permission.deleted = false
 where menu.menu_code = 'qms.engineering.parts'
   and menu.deleted = false
on conflict (tenant_id, menu_id, permission_id) do nothing;

insert into sys_role_menu (
    tenant_id, role_id, menu_id, created_by, updated_by
)
select role.tenant_id, role.id, menu.id, 0, 0
  from sys_role role
  join sys_menu menu
    on menu.tenant_id = role.tenant_id
   and menu.menu_code in ('qms', 'qms.engineering.parts')
   and menu.deleted = false
 where role.role_code = 'platform_admin'
   and role.deleted = false
on conflict (tenant_id, role_id, menu_id) do nothing;
