alter table mdm_model_version add column approval_required boolean not null default false;

update mdm_model_version v
set approval_required=m.approval_required
from mdm_model m
where m.id=v.model_id and m.tenant_id=v.tenant_id;
