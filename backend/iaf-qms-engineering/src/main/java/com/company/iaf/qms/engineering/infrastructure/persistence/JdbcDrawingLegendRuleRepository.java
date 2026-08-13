package com.company.iaf.qms.engineering.infrastructure.persistence;

import com.company.iaf.qms.engineering.domain.model.DrawingLegendRule;
import com.company.iaf.qms.engineering.domain.repository.DrawingLegendRuleRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class JdbcDrawingLegendRuleRepository implements DrawingLegendRuleRepository {
 private final JdbcTemplate jdbc;
 public JdbcDrawingLegendRuleRepository(JdbcTemplate jdbc){this.jdbc=jdbc;}
 public List<DrawingLegendRule> findAll(long tenant){return jdbc.query("""
  select id,rule_code,marker,description,target_field,target_value,match_mode,priority,enabled,version
  from qms_drawing_legend_rule where tenant_id=? and deleted=false order by priority,id
  """,(rs,n)->new DrawingLegendRule(rs.getLong("id"),rs.getString("rule_code"),rs.getString("marker"),
   rs.getString("description"),rs.getString("target_field"),rs.getString("target_value"),
   rs.getString("match_mode"),rs.getInt("priority"),rs.getBoolean("enabled"),rs.getInt("version")),tenant);}
 public boolean update(long actor,long tenant,long id,int version,String marker,String description,boolean enabled,int priority){return jdbc.update("""
  update qms_drawing_legend_rule set marker=?,description=?,enabled=?,priority=?,updated_by=?,updated_at=current_timestamp,version=version+1
  where tenant_id=? and id=? and version=? and deleted=false
  """,marker,description,enabled,priority,actor,tenant,id,version)==1;}
 public void reclassifyPending(long actor,long tenant,long org){
  var rules=findAll(tenant).stream().filter(DrawingLegendRule::enabled).toList();
  var rows=jdbc.queryForList("select id,name from qms_quality_characteristic where tenant_id=? and org_id=? and evidence_id is not null and review_status='PENDING' and deleted=false",tenant,org);
  for(var row:rows){String text=String.valueOf(row.get("name"));boolean inspection=false,location=false,fit=false,reference=false,regulatory=false;String special=null;
   for(var rule:rules){boolean match="WRAPS_VALUE".equals(rule.matchMode())?isWrapped(text):text.contains(rule.marker());if(!match)continue;
    switch(rule.targetField()){case "INSPECTION_DIMENSION"->inspection=true;case "LOCATION_DIMENSION"->location=true;case "FIT_DIMENSION"->fit=true;case "REFERENCE_DIMENSION"->reference=true;case "REGULATORY_FLAG"->{regulatory=true;special=rule.targetValue();}case "SPECIAL_CODE"->special=rule.targetValue();default->{}}
   }
   if(reference)inspection=false;
   jdbc.update("update qms_quality_characteristic set inspection_dimension=?,reference_dimension=?,fit_dimension=?,location_dimension=?,regulatory_flag=?,special_characteristic_code=?,updated_by=?,updated_at=current_timestamp,version=version+1 where tenant_id=? and org_id=? and id=? and review_status='PENDING'",
    inspection,reference,fit,location,regulatory,special,actor,tenant,org,row.get("id"));
  }
 }
 private static boolean isWrapped(String text){String value=text.replaceAll("^\\s*\\[[A-Za-z]+]\\s*","").trim();return value.startsWith("(")&&value.contains(")");}
}
