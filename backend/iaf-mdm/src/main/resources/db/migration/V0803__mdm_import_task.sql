create table mdm_import_task (
 id uuid primary key,
 tenant_id bigint not null,
 model_id bigint not null references mdm_model(id),
 file_name varchar(255) not null,
 status varchar(32) not null,
 total_rows int not null default 0,
 valid_rows int not null default 0,
 invalid_rows int not null default 0,
 imported_rows int not null default 0,
 record_payload jsonb not null default '[]'::jsonb,
 validation_result jsonb not null default '{}'::jsonb,
 committed_at timestamptz,
 created_by bigint,
 created_at timestamptz not null default now(),
 updated_by bigint,
 updated_at timestamptz not null default now(),
 deleted boolean not null default false,
 version int not null default 0,
 ext_json jsonb
);

create index idx_mdm_import_task_tenant_model_created
 on mdm_import_task(tenant_id,model_id,created_at desc) where deleted=false;
