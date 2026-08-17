insert into sys_permission(tenant_id,permission_code,permission_name,resource_type,module_code,action_code,created_by,updated_by)
select t.id,'mdm:record:delete','Delete draft MDM records','API','mdm','delete',0,0
from sys_tenant t where t.deleted=false
on conflict(tenant_id,permission_code) do nothing;

insert into sys_role_permission(tenant_id,role_id,permission_id,created_by,updated_by,deleted,version)
select r.tenant_id,r.id,p.id,0,0,false,0
from sys_role r join sys_permission p on p.tenant_id=r.tenant_id
where r.role_code='platform_admin' and r.deleted=false and p.deleted=false
  and p.permission_code='mdm:record:delete'
on conflict(tenant_id,role_id,permission_id) do nothing;
