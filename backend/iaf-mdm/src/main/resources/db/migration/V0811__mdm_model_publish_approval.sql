alter table mdm_model
    add column model_approval_role_id bigint,
    add column publish_approval_status varchar(32) not null default 'NOT_SUBMITTED',
    add column publish_approval_org_id bigint;

alter table mdm_model
    add constraint fk_mdm_model_approval_role
    foreign key (model_approval_role_id) references sys_role(id);

create index idx_mdm_model_publish_approval
    on mdm_model(tenant_id, publish_approval_status)
    where deleted = false;

insert into sys_permission(tenant_id,permission_code,permission_name,resource_type,module_code,action_code,created_by,updated_by)
select t.id,'mdm:model:approve','Approve MDM model publication','API','mdm','approve',0,0
from sys_tenant t where t.deleted=false
on conflict(tenant_id,permission_code) do nothing;

insert into sys_role_permission(tenant_id,role_id,permission_id,created_by,updated_by,deleted,version)
select r.tenant_id,r.id,p.id,0,0,false,0
from sys_role r join sys_permission p on p.tenant_id=r.tenant_id
where r.role_code='platform_admin' and r.deleted=false and p.deleted=false
  and p.permission_code='mdm:model:approve'
on conflict(tenant_id,role_id,permission_id) do nothing;
