package com.company.iaf.platform.workflow.infrastructure.persistence;

import com.company.iaf.platform.workflow.application.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Optional;
import java.util.List;

@Service
public class JdbcApprovalApplicationService implements ApprovalApplicationService {
    private final JdbcTemplate jdbc;
    public JdbcApprovalApplicationService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override @Transactional
    public ApprovalRecord submit(long tenantId,long orgId,String type,long businessId,long actor,String comment) {
        var latest=findLatest(tenantId,orgId,type,businessId);
        if(latest.isPresent() && latest.get().status()==ApprovalStatus.PENDING) throw new IllegalStateException("Approval is already pending");
        Long id=jdbc.queryForObject("insert into platform_approval_instance(tenant_id,org_id,business_type,business_id,approval_status,submitted_by,created_by,updated_by,ext_json) values(?,?,?,?, 'PENDING',?,?,?,'{}') returning id",Long.class,tenantId,orgId,type,businessId,actor,actor,actor);
        action(tenantId,orgId,id,"SUBMIT",actor,comment); return findById(tenantId,orgId,id);
    }
    @Override @Transactional public ApprovalRecord approve(long t,long o,String type,long b,long actor,String comment){return decide(t,o,type,b,actor,comment,ApprovalStatus.APPROVED);}
    @Override @Transactional public ApprovalRecord reject(long t,long o,String type,long b,long actor,String comment){return decide(t,o,type,b,actor,comment,ApprovalStatus.REJECTED);}
    private ApprovalRecord decide(long t,long o,String type,long b,long actor,String comment,ApprovalStatus status){var current=findLatest(t,o,type,b).filter(x->x.status()==ApprovalStatus.PENDING).orElseThrow(()->new IllegalStateException("No pending approval"));if(current.submittedBy()==actor)throw new IllegalStateException("Submitter cannot decide own approval");int changed=jdbc.update("update platform_approval_instance set approval_status=?,decided_by=?,decided_at=current_timestamp,decision_comment=?,updated_by=?,updated_at=current_timestamp,version=version+1 where tenant_id=? and org_id=? and id=? and approval_status='PENDING'",status.name(),actor,comment,actor,t,o,current.id());if(changed!=1)throw new IllegalStateException("Approval was already decided");action(t,o,current.id(),status.name(),actor,comment);return findById(t,o,current.id());}
    @Override public Optional<ApprovalRecord> findLatest(long t,long o,String type,long b){var ids=jdbc.query("select id from platform_approval_instance where tenant_id=? and org_id=? and business_type=? and business_id=? and deleted=false order by id desc limit 1",(rs,n)->rs.getLong(1),t,o,type,b);return ids.isEmpty()?Optional.empty():Optional.of(findById(t,o,ids.getFirst()));}
    @Override public List<ApprovalRecord> findLatestByBusinessType(long t,String type){var refs=jdbc.query("select distinct on (business_id) id,org_id from platform_approval_instance where tenant_id=? and business_type=? and deleted=false order by business_id,id desc",(rs,n)->new ApprovalRef(rs.getLong("id"),rs.getLong("org_id")),t,type);return refs.stream().map(ref->findById(t,ref.orgId(),ref.id())).toList();}
    private ApprovalRecord findById(long t,long o,long id){return jdbc.queryForObject("select * from platform_approval_instance where tenant_id=? and org_id=? and id=? and deleted=false",(rs,n)->new ApprovalRecord(rs.getLong("id"),rs.getString("business_type"),rs.getLong("business_id"),ApprovalStatus.valueOf(rs.getString("approval_status")),rs.getLong("submitted_by"),rs.getTimestamp("submitted_at").toLocalDateTime(),(Long)rs.getObject("decided_by"),rs.getTimestamp("decided_at")==null?null:rs.getTimestamp("decided_at").toLocalDateTime(),rs.getString("decision_comment"),jdbc.query("select action_code,actor_id,comment,acted_at from platform_approval_action where tenant_id=? and org_id=? and approval_instance_id=? and deleted=false order by id",(ar,i)->new ApprovalRecord.Action(ar.getString(1),ar.getLong(2),ar.getString(3),ar.getTimestamp(4).toLocalDateTime()),t,o,id)),t,o,id);}
    private void action(long t,long o,long id,String code,long actor,String comment){jdbc.update("insert into platform_approval_action(tenant_id,org_id,approval_instance_id,action_code,actor_id,comment,created_by,updated_by,ext_json) values(?,?,?,?,?,?,?,?,'{}')",t,o,id,code,actor,comment,actor,actor);}
    private record ApprovalRef(long id,long orgId){}
}
