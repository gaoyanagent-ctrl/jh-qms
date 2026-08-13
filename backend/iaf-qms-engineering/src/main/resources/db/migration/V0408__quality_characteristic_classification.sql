alter table qms_quality_characteristic
    alter column evidence_id drop not null,
    add column inspection_dimension boolean not null default false,
    add column reference_dimension boolean not null default false,
    add column ideal_dimension boolean not null default false,
    add column fit_dimension boolean not null default false,
    add column location_dimension boolean not null default false,
    add column regulatory_flag boolean not null default false,
    add column mandatory_inspection boolean not null default false;

update qms_quality_characteristic
   set inspection_dimension = true
 where characteristic_type = 'DIMENSION'
   and deleted = false;

alter table qms_quality_characteristic
    add constraint ck_qms_characteristic_dimension_flags
        check (not (reference_dimension and ideal_dimension)),
    add constraint ck_qms_characteristic_noninspection_flags
        check (not ((reference_dimension or ideal_dimension) and
                    (inspection_dimension or mandatory_inspection)));

create index idx_qms_characteristic_classification on qms_quality_characteristic
    (tenant_id, org_id, drawing_revision_id, characteristic_type, review_status)
    where deleted = false;
