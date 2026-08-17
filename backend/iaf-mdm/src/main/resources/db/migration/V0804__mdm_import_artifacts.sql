alter table mdm_import_task add column source_object_key varchar(500);
alter table mdm_import_task add column source_media_type varchar(128);
alter table mdm_import_task add column source_size bigint;

create index idx_mdm_import_task_source_object
 on mdm_import_task(tenant_id,source_object_key) where source_object_key is not null and deleted=false;
