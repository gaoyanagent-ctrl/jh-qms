insert into sys_user_org
    (tenant_id,user_id,org_id,is_primary,scope_weight,created_by,updated_by,deleted,version)
select u.tenant_id,u.id,o.id,true,100,0,0,false,0
  from sys_user u
  join lateral (
      select id from sys_org
       where tenant_id=u.tenant_id and parent_id is null and deleted=false
       order by id limit 1
  ) o on true
 where u.deleted=false
   and not exists (
       select 1 from sys_user_org uo
        where uo.tenant_id=u.tenant_id and uo.user_id=u.id and uo.deleted=false
   );

update sys_user u
   set primary_org_id=(
       select uo.org_id from sys_user_org uo
        where uo.tenant_id=u.tenant_id and uo.user_id=u.id
          and uo.is_primary and not uo.deleted order by uo.id limit 1
   ), updated_at=current_timestamp, version=version+1
 where u.deleted=false and u.primary_org_id is null
   and exists (
       select 1 from sys_user_org uo
        where uo.tenant_id=u.tenant_id and uo.user_id=u.id
          and uo.is_primary and not uo.deleted
   );
