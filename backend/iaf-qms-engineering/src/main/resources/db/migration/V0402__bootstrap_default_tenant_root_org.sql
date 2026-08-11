insert into sys_org (
    tenant_id, parent_id, org_code, org_name, org_type, status,
    sort_no, created_by, updated_by
)
select t.tenant_id, null, 'ROOT', t.tenant_name, 'COMPANY', 'ENABLED',
       0, 1, 1
  from sys_tenant t
 where t.tenant_id = 1
   and t.tenant_code = 'default'
   and t.deleted = false
on conflict (tenant_id, org_code) do nothing;

update sys_user u
   set primary_org_id = o.id,
       updated_by = 1,
       updated_at = current_timestamp,
       version = u.version + 1
  from sys_org o
 where u.tenant_id = 1
   and u.username = 'admin'
   and u.deleted = false
   and u.primary_org_id is null
   and o.tenant_id = u.tenant_id
   and o.org_code = 'ROOT'
   and o.deleted = false;

insert into sys_user_org (
    tenant_id, user_id, org_id, is_primary, scope_weight,
    created_by, updated_by
)
select u.tenant_id, u.id, o.id, true, 100, 1, 1
  from sys_user u
  join sys_org o
    on o.tenant_id = u.tenant_id
   and o.org_code = 'ROOT'
   and o.deleted = false
 where u.tenant_id = 1
   and u.username = 'admin'
   and u.deleted = false
on conflict (tenant_id, user_id, org_id) where deleted = false do nothing;
