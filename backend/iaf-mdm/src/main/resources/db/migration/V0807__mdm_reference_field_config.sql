alter table mdm_model_field add column reference_config jsonb;

update mdm_model_field f set reference_config=jsonb_build_object(
 'targetModelCode','material',
 'valueFieldCode','businessCode',
 'displayFieldCode','name',
 'statusFieldCode','lifecycleStatus',
 'allowedStatuses',jsonb_build_array('ACTIVE')
)
from mdm_model m
where m.id=f.model_id and m.tenant_id=f.tenant_id and m.code='bomItem'
  and f.code in ('parentMaterialCode','componentMaterialCode') and f.deleted=false;

-- Reference metadata now owns these checks; retain the generic quantity expression rule only.
update mdm_validation_rule r set deleted=true,updated_at=now()
from mdm_model m where m.id=r.model_id and m.tenant_id=r.tenant_id and m.code='bomItem'
  and r.code in ('parent-material-active','component-material-active') and r.deleted=false;
