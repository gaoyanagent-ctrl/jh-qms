package com.company.iaf.qms.engineering.application;
import com.company.iaf.platform.core.security.RequiresPermission;
import com.company.iaf.qms.engineering.domain.model.DrawingLegendRule;
import com.company.iaf.qms.engineering.domain.repository.*;
import com.company.iaf.qms.engineering.interfaces.dto.DrawingLegendRuleUpdateRequest;
import com.company.iaf.shared.exception.BusinessException;
import com.company.iaf.shared.security.SecurityContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
@Service public class DrawingLegendRuleService {
 private final DrawingLegendRuleRepository repository;private final QmsAuditTrail audit;
 public DrawingLegendRuleService(DrawingLegendRuleRepository repository,QmsAuditTrail audit){this.repository=repository;this.audit=audit;}
 @RequiresPermission("qms:drawing-legend:manage") @Transactional(readOnly=true)
 public List<DrawingLegendRule> list(long tenant){return repository.findAll(tenant);}
 @RequiresPermission("qms:drawing-legend:manage") @Transactional
 public List<DrawingLegendRule> update(long tenant,long org,DrawingLegendRuleUpdateRequest request){
  if(request.rules().stream().map(r->r.marker().trim()).distinct().count()!=request.rules().size())throw new BusinessException(QmsEngineeringErrorCode.CHARACTERISTIC_CLASSIFICATION_INVALID,"Legend markers must be unique");
  long actor=SecurityContext.getUserId().orElse(0L);for(var rule:request.rules())if(!repository.update(actor,tenant,rule.id(),rule.version(),rule.marker().trim(),rule.description().trim(),rule.enabled(),rule.priority()))throw new BusinessException(QmsEngineeringErrorCode.CHARACTERISTIC_REVIEW_CONFLICT);
  repository.reclassifyPending(actor,tenant,org);var result=repository.findAll(tenant);audit.record(tenant,actor,"DRAWING_LEGEND_UPDATED","DrawingLegend",tenant,result);return result;
 }
}
