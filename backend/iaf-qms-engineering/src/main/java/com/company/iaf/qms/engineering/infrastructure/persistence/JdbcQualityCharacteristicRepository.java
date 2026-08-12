package com.company.iaf.qms.engineering.infrastructure.persistence;

import com.company.iaf.qms.engineering.domain.model.QualityCharacteristic;
import com.company.iaf.qms.engineering.domain.repository.QualityCharacteristicRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Repository
public class JdbcQualityCharacteristicRepository implements QualityCharacteristicRepository {
    private static final Pattern DIMENSION = Pattern.compile("(?<![\\d.])(\\d+(?:\\.\\d+)?)\\s*(?:±|\\+/-)\\s*(\\d+(?:\\.\\d+)?)");
    private static final ObjectMapper JSON = new ObjectMapper();
    private final JdbcTemplate jdbc;
    public JdbcQualityCharacteristicRepository(JdbcTemplate jdbc) { this.jdbc=jdbc; }

    @Override public void generateDimensionCandidates(long actorId,long tenantId,long orgId,long revisionId) {
        var rows=jdbc.queryForList("""
            select e.id evidence_id,e.entity_id,e.normalized_text,e.confidence,e.extractor_type,
                   de.entity_type,de.geometry_json,d.part_id
            from qms_source_evidence e join qms_drawing_revision r on r.tenant_id=e.tenant_id and r.id=e.drawing_revision_id
            join qms_drawing d on d.tenant_id=r.tenant_id and d.id=r.drawing_id
            left join qms_drawing_entity de on de.tenant_id=e.tenant_id and de.revision_id=e.drawing_revision_id
                 and de.entity_id=e.entity_id and de.deleted=false
            where e.tenant_id=? and e.org_id=? and e.drawing_revision_id=? and e.deleted=false order by e.id
            """,tenantId,orgId,revisionId);
        for(var row:rows){ String text=(String)row.get("normalized_text"); Matcher matcher=DIMENSION.matcher(text==null?"":text);
            BigDecimal nominal; BigDecimal upper; BigDecimal lower;
            if (matcher.find()) {
                nominal=new BigDecimal(matcher.group(1)); upper=new BigDecimal(matcher.group(2)); lower=upper.negate();
            } else if ("DWG_ENTITY".equals(row.get("extractor_type")) && "DIMENSION".equals(row.get("entity_type"))) {
                Object geometry = row.get("geometry_json");
                try { nominal = new BigDecimal(JSON.readTree(String.valueOf(geometry)).path("act_measurement").asText()); }
                catch (Exception ignored) { continue; }
                upper=null; lower=null;
            } else continue;
            jdbc.update("""
                insert into qms_quality_characteristic
                (tenant_id,org_id,part_id,drawing_revision_id,source_entity_id,evidence_id,characteristic_code,
                 characteristic_type,name,nominal_value,upper_tolerance,lower_tolerance,upper_limit,lower_limit,
                 unit,confidence,created_by,updated_by)
                values (?,?,?,?,?,?,?,'DIMENSION',?,?,?,?,?,?,'mm',?,?,?) on conflict do nothing
                """,tenantId,orgId,row.get("part_id"),revisionId,row.get("entity_id"),row.get("evidence_id"),
                    "DIM-EV-"+row.get("evidence_id"),text,nominal,upper,lower,
                    upper == null ? null : nominal.add(upper),lower == null ? null : nominal.add(lower),
                    row.get("confidence"),actorId,actorId);
        }
    }
    @Override public List<QualityCharacteristic> findByRevision(long tenantId,long orgId,long revisionId){return jdbc.query(sql(""),this::map,tenantId,orgId,revisionId);}
    @Override public Optional<QualityCharacteristic> findById(long tenantId,long orgId,long revisionId,long id){return jdbc.query(sql(" and id=?"),this::map,tenantId,orgId,revisionId,id).stream().findFirst();}
    @Override public boolean review(long actorId,long tenantId,long orgId,long revisionId,long id,int version,String reviewStatus,String name,BigDecimal nominal,BigDecimal upper,BigDecimal lower,String unit,String comment){
        return jdbc.update("""
            update qms_quality_characteristic
            set review_status=?, name=coalesce(?,name), nominal_value=coalesce(?,nominal_value),
                upper_tolerance=coalesce(?,upper_tolerance), lower_tolerance=coalesce(?,lower_tolerance),
                upper_limit=coalesce(?,nominal_value)+coalesce(?,upper_tolerance),
                lower_limit=coalesce(?,nominal_value)+coalesce(?,lower_tolerance),
                unit=coalesce(?,unit), review_comment=?, reviewed_by=?, reviewed_at=current_timestamp,
                updated_by=?, updated_at=current_timestamp, version=version+1
            where tenant_id=? and org_id=? and drawing_revision_id=? and id=? and version=?
              and review_status='PENDING' and deleted=false
            """,
            reviewStatus,name,nominal,upper,lower,nominal,upper,nominal,lower,unit,comment,actorId,actorId,tenantId,orgId,revisionId,id,version)==1;
    }
    private String sql(String suffix) {
        return """
            select id,tenant_id,org_id,part_id,drawing_revision_id,source_entity_id,evidence_id,
                   characteristic_code,characteristic_type,name,nominal_value,upper_tolerance,
                   lower_tolerance,upper_limit,lower_limit,unit,special_characteristic_code,
                   confidence,status,review_status,reviewed_by,reviewed_at,review_comment,version
            from qms_quality_characteristic
            where tenant_id=? and org_id=? and drawing_revision_id=? and deleted=false
            """ + suffix + " order by id";
    }

    private QualityCharacteristic map(ResultSet r, int rowNum) throws SQLException {
        Number reviewedBy = (Number) r.getObject("reviewed_by");
        return new QualityCharacteristic(
            r.getLong("id"), r.getLong("tenant_id"), r.getLong("org_id"), r.getLong("part_id"),
            r.getLong("drawing_revision_id"), r.getString("source_entity_id"), r.getLong("evidence_id"),
            r.getString("characteristic_code"), r.getString("characteristic_type"), r.getString("name"),
            r.getBigDecimal("nominal_value"), r.getBigDecimal("upper_tolerance"),
            r.getBigDecimal("lower_tolerance"), r.getBigDecimal("upper_limit"),
            r.getBigDecimal("lower_limit"), r.getString("unit"),
            r.getString("special_characteristic_code"), r.getBigDecimal("confidence"),
            r.getString("status"), r.getString("review_status"),
            reviewedBy == null ? null : reviewedBy.longValue(),
            JdbcPartRepository.utc(r.getTimestamp("reviewed_at")), r.getString("review_comment"),
            r.getInt("version")
        );
    }
}
