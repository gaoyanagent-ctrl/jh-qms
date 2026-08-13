package com.company.iaf.qms.engineering.domain.repository;
import com.company.iaf.qms.engineering.domain.model.DrawingLegendRule;
import java.util.List;
public interface DrawingLegendRuleRepository {
 List<DrawingLegendRule> findAll(long tenantId);
 boolean update(long actorId,long tenantId,long id,int version,String marker,String description,boolean enabled,int priority);
 void reclassifyPending(long actorId,long tenantId,long orgId);
}
