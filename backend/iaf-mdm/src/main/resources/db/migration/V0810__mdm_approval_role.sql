update mdm_model m
set ui_schema=jsonb_set(m.ui_schema,'{approval}',jsonb_build_object('roleId',r.id),true),
    updated_at=now()
from sys_role r
where m.tenant_id=r.tenant_id and m.approval_required=true and m.deleted=false
  and r.role_code='platform_admin' and r.deleted=false
  and not coalesce(m.ui_schema #> '{approval}' ? 'roleId',false);
