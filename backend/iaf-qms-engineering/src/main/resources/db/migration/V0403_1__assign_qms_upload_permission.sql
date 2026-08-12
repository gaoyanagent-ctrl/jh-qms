insert into sys_role_permission
    (tenant_id, role_id, permission_id, created_by, updated_by, deleted, version)
select r.tenant_id, r.id, p.id, 1, 1, false, 0
from sys_role r
join sys_permission p on p.tenant_id = r.tenant_id
where r.tenant_id = 1
  and r.role_code = 'platform_admin'
  and p.permission_code = 'qms:drawing-revision:upload'
  and not exists (
      select 1
      from sys_role_permission rp
      where rp.tenant_id = r.tenant_id
        and rp.role_id = r.id
        and rp.permission_id = p.id
        and rp.deleted = false
  );
