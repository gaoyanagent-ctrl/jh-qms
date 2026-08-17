package com.company.iaf.mdm.infrastructure.persistence;

import com.company.iaf.mdm.domain.model.MdmModels;
import com.company.iaf.mdm.domain.repository.MdmRepository;
import com.company.iaf.mdm.interfaces.dto.MdmDtos;
import com.company.iaf.mdm.application.MdmErrorCode;
import com.company.iaf.shared.exception.BusinessException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneOffset;
import java.util.*;

@Repository
@ConditionalOnProperty(name = "iaf.mdm.enabled", havingValue = "true", matchIfMissing = true)
public class JdbcMdmRepository implements MdmRepository {
    private final JdbcTemplate jdbc; private final ObjectMapper json;
    public JdbcMdmRepository(JdbcTemplate jdbc, ObjectMapper json) { this.jdbc = jdbc; this.json = json; }

    public List<MdmModels.Model> findModels(long tenantId) {
        return jdbc.query("select m.*, d.code domain_code from mdm_model m join mdm_data_domain d on d.id=m.domain_id where m.tenant_id=? and m.deleted=false order by m.name", (rs,n)->mapModel(rs, fields(tenantId, rs.getLong("id"))), tenantId);
    }
    public Optional<MdmModels.Model> findModel(long tenantId, String code) {
        var list=jdbc.query("select m.*, d.code domain_code from mdm_model m join mdm_data_domain d on d.id=m.domain_id where m.tenant_id=? and m.code=? and m.deleted=false", (rs,n)->mapModel(rs, fields(tenantId, rs.getLong("id"))), tenantId, code); return list.stream().findFirst();
    }
    public MdmModels.Model insertModel(long tenantId,long actorId,MdmDtos.CreateModelRequest r) {
        List<Long> domains=jdbc.query("select id from mdm_data_domain where tenant_id=? and code=? and status='ENABLED' and deleted=false",(rs,n)->rs.getLong(1),tenantId,r.domainCode().trim());
        if(domains.isEmpty()) throw new BusinessException(MdmErrorCode.DOMAIN_NOT_FOUND);
        jdbc.update("insert into mdm_model(tenant_id,domain_id,code,name,record_type,version_enabled,effective_date_enabled,organization_scope_enabled,approval_required,status,current_model_version,ui_schema,created_by,updated_by) values (?,?,?,?,?,?,?,?,?,'DRAFT',0,'{}'::jsonb,?,?)",tenantId,domains.get(0),r.code().trim(),r.name().trim(),r.recordType()==null?"MASTER":r.recordType(),r.versionEnabled(),r.effectiveDateEnabled(),r.organizationScopeEnabled(),r.approvalRequired(),actorId,actorId);
        return findModel(tenantId,r.code().trim()).orElseThrow();
    }
    public void replaceDraft(long tenantId,long actorId,long modelId,List<MdmDtos.FieldDraft> fields,Map<String,Object> uiSchema) {
        jdbc.update("delete from mdm_model_field where tenant_id=? and model_id=?",tenantId,modelId);
        for(var f:fields) jdbc.update("insert into mdm_model_field(tenant_id,model_id,code,name,data_type,required,unique_value,readonly,searchable,sortable,list_visible,max_length,enum_options,help_text,sort_no,created_by,updated_by) values (?,?,?,?,?,?,?,?,?,?,?,?,?::jsonb,?,?,?,?)",tenantId,modelId,f.code(),f.name(),f.dataType(),f.required(),f.unique(),f.readonly(),f.searchable(),f.sortable(),f.listVisible(),f.maxLength(),write(f.enumOptions()==null?List.of():f.enumOptions()),f.helpText(),f.sortNo(),actorId,actorId);
        jdbc.update("update mdm_model set ui_schema=?::jsonb,status='DRAFT',updated_by=?,updated_at=now(),version=version+1 where tenant_id=? and id=? and status in ('DRAFT','PUBLISHED')",write(uiSchema),actorId,tenantId,modelId);
    }
    public void publishModel(long tenantId,long actorId,MdmModels.Model model) {
        int next=model.currentModelVersion()+1;
        jdbc.update("insert into mdm_model_version(tenant_id,model_id,version_no,status,schema_snapshot,ui_schema_snapshot,published_by,created_by,updated_by) values (?,?,?,'PUBLISHED',?::jsonb,?::jsonb,?,?,?)",tenantId,model.id(),next,write(model.fields()),write(model.uiSchema()),actorId,actorId,actorId);
        jdbc.update("update mdm_model set status='PUBLISHED',current_model_version=?,updated_by=?,updated_at=now(),version=version+1 where tenant_id=? and id=?",next,actorId,tenantId,model.id());
    }
    private List<MdmModels.Field> fields(long tenantId,long modelId) { return jdbc.query("select * from mdm_model_field where tenant_id=? and model_id=? and deleted=false and deprecated=false order by sort_no,id", this::mapField, tenantId,modelId); }
    public List<MdmModels.Record> findRecords(long tenantId,long modelId,String keyword,long offset,int size) {
        String base="select * from mdm_master_record where tenant_id=? and model_id=? and deleted=false";
        if(keyword==null)return jdbc.query(base+" order by updated_at desc,id limit ? offset ?",this::mapRecord,tenantId,modelId,size,offset);
        String p="%"+keyword+"%";return jdbc.query(base+" and (business_code ilike ? or name ilike ?) order by updated_at desc,id limit ? offset ?",this::mapRecord,tenantId,modelId,p,p,size,offset);
    }
    public long countRecords(long t,long m,String q){ Long n=q==null?jdbc.queryForObject("select count(*) from mdm_master_record where tenant_id=? and model_id=? and deleted=false",Long.class,t,m):jdbc.queryForObject("select count(*) from mdm_master_record where tenant_id=? and model_id=? and deleted=false and (business_code ilike ? or name ilike ?)",Long.class,t,m,"%"+q+"%","%"+q+"%");return n==null?0:n;}
    public boolean businessCodeExists(long t,long m,String c,UUID x){Integer n=x==null?jdbc.queryForObject("select count(*) from mdm_master_record where tenant_id=? and model_id=? and business_code=? and deleted=false",Integer.class,t,m,c):jdbc.queryForObject("select count(*) from mdm_master_record where tenant_id=? and model_id=? and business_code=? and id<>? and deleted=false",Integer.class,t,m,c,x);return n!=null&&n>0;}
    public MdmModels.Record insertRecord(long t,long a,MdmModels.Model m,String c,String n,String s,String st,List<Long> ids,java.time.LocalDate from,java.time.LocalDate to,Map<String,Object> attrs){ UUID id=UUID.randomUUID();jdbc.update("insert into mdm_master_record(id,tenant_id,model_id,business_code,name,lifecycle_status,current_version_no,model_version_no,scope_type,scope_ids,effective_from,effective_to,attributes,created_by,updated_by,deleted,version) values (?,?,?,?,?,?,1,?,?,?::jsonb,?,?,?::jsonb,?,?,false,0)",id,t,m.id(),c,n,s,m.currentModelVersion(),st,write(ids==null?List.of():ids),from,to,write(attrs),a,a);return findRecord(t,m.id(),id).orElseThrow();}
    public Optional<MdmModels.Record> findRecord(long t,long m,UUID id){return jdbc.query("select * from mdm_master_record where tenant_id=? and model_id=? and id=? and deleted=false",this::mapRecord,t,m,id).stream().findFirst();}
    public boolean updateRecord(long t,long a,MdmModels.Record r,int expected){return jdbc.update("update mdm_master_record set business_code=?,name=?,lifecycle_status=?,current_version_no=?,model_version_no=?,scope_type=?,scope_ids=?::jsonb,effective_from=?,effective_to=?,attributes=?::jsonb,updated_by=?,updated_at=now(),version=version+1 where tenant_id=? and model_id=? and id=? and version=? and deleted=false",r.businessCode(),r.name(),r.lifecycleStatus(),r.currentVersionNo(),r.modelVersionNo(),r.scopeType(),write(r.scopeIds()==null?List.of():r.scopeIds()),r.effectiveFrom(),r.effectiveTo(),write(r.attributes()),a,t,r.modelId(),r.id(),expected)==1;}
    public void insertVersion(long t,long a,MdmModels.Record r,String type,String reason){jdbc.update("insert into mdm_master_record_version(tenant_id,record_id,version_no,snapshot,change_type,change_reason,effective_from,effective_to,created_by,updated_by,deleted,version) values (?,?,?,?::jsonb,?,?,?,?,?,?,false,0)",t,r.id(),r.currentVersionNo(),write(r),type,reason,r.effectiveFrom(),r.effectiveTo(),a,a);}
    public List<MdmModels.RecordVersion> findRecordVersions(long t,long m,UUID id){return jdbc.query("select v.* from mdm_master_record_version v join mdm_master_record r on r.id=v.record_id and r.tenant_id=v.tenant_id where v.tenant_id=? and r.model_id=? and v.record_id=? and v.deleted=false and r.deleted=false order by v.version_no desc",(rs,n)->new MdmModels.RecordVersion(rs.getLong("id"),(UUID)rs.getObject("record_id"),rs.getInt("version_no"),readMap(rs.getString("snapshot")),rs.getString("change_type"),rs.getString("change_reason"),rs.getObject("effective_from",java.time.LocalDate.class),rs.getObject("effective_to",java.time.LocalDate.class),rs.getLong("created_by"),rs.getTimestamp("created_at").toInstant().atOffset(ZoneOffset.UTC)),t,m,id);}
    private MdmModels.Model mapModel(ResultSet r,List<MdmModels.Field> f)throws SQLException{return new MdmModels.Model(r.getLong("id"),r.getString("domain_code"),r.getString("code"),r.getString("name"),r.getString("record_type"),r.getBoolean("version_enabled"),r.getBoolean("effective_date_enabled"),r.getBoolean("organization_scope_enabled"),r.getBoolean("approval_required"),r.getString("status"),r.getInt("current_model_version"),readMap(r.getString("ui_schema")),f);}
    private MdmModels.Field mapField(ResultSet r,int n)throws SQLException{return new MdmModels.Field(r.getLong("id"),r.getString("code"),r.getString("name"),r.getString("data_type"),r.getBoolean("required"),r.getBoolean("unique_value"),r.getBoolean("readonly"),r.getBoolean("searchable"),r.getBoolean("sortable"),r.getBoolean("list_visible"),(Integer)r.getObject("max_length"),readList(r.getString("enum_options")),r.getString("help_text"),r.getInt("sort_no"));}
    private MdmModels.Record mapRecord(ResultSet r,int n)throws SQLException{return new MdmModels.Record((UUID)r.getObject("id"),r.getLong("model_id"),null,r.getString("business_code"),r.getString("name"),r.getString("lifecycle_status"),r.getInt("current_version_no"),r.getInt("model_version_no"),r.getString("scope_type"),readLongs(r.getString("scope_ids")),r.getObject("effective_from",java.time.LocalDate.class),r.getObject("effective_to",java.time.LocalDate.class),readMap(r.getString("attributes")),r.getInt("version"),r.getTimestamp("created_at").toInstant().atOffset(ZoneOffset.UTC),r.getTimestamp("updated_at").toInstant().atOffset(ZoneOffset.UTC));}
    private String write(Object o){try{return json.writeValueAsString(o);}catch(Exception e){throw new IllegalArgumentException(e);}}
    private Map<String,Object> readMap(String s){try{return s==null?Map.of():json.readValue(s,new TypeReference<>(){});}catch(Exception e){throw new IllegalArgumentException(e);}}
    private List<String> readList(String s){try{return s==null?List.of():json.readValue(s,new TypeReference<>(){});}catch(Exception e){throw new IllegalArgumentException(e);}}
    private List<Long> readLongs(String s){try{return s==null?List.of():json.readValue(s,new TypeReference<>(){});}catch(Exception e){throw new IllegalArgumentException(e);}}
}
