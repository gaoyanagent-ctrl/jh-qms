alter table mdm_import_task add column error_message varchar(1000);
alter table mdm_import_task add column processing_started_at timestamptz;

create index idx_mdm_import_task_queue
 on mdm_import_task(status,created_at) where status='QUEUED' and deleted=false;
