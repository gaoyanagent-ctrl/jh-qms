package com.company.iaf.qms.engineering.application;
import com.company.iaf.platform.core.security.RequiresPermission;
import com.company.iaf.qms.engineering.domain.repository.*;
import com.company.iaf.qms.engineering.interfaces.dto.*;
import com.company.iaf.shared.exception.BusinessException;
import com.company.iaf.shared.security.SecurityContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
@Service public class QualityCharacteristicService {
 private final QualityCharacteristicRepository repository; private final DrawingRevisionRepository revisions; private final QmsAuditTrail audit;
 public QualityCharacteristicService(QualityCharacteristicRepository repository,DrawingRevisionRepository revisions,QmsAuditTrail audit){this.repository=repository;this.revisions=revisions;this.audit=audit;}
 @RequiresPermission("qms:drawing-revision:view") @Transactional(readOnly=true)
 public List<QualityCharacteristicResponse> list(long tenant,long org,long revision){requireRevision(tenant,org,revision);return repository.findByRevision(tenant,org,revision).stream().map(QualityCharacteristicResponse::from).toList();}
 @RequiresPermission("qms:quality-characteristic:review") @Transactional
 public QualityCharacteristicResponse review(long tenant,long org,long revision,long id,String decision,QualityCharacteristicReviewRequest request){
  requireRevision(tenant,org,revision); if(!decision.equals("CONFIRMED")&&!decision.equals("REJECTED"))throw new BusinessException(QmsEngineeringErrorCode.CHARACTERISTIC_REVIEW_INVALID);
  long actor=SecurityContext.getUserId().orElse(0L); boolean changed=repository.review(actor,tenant,org,revision,id,request.version(),decision,request.name(),request.nominalValue(),request.upperTolerance(),request.lowerTolerance(),request.unit(),request.comment());
  if(!changed)throw new BusinessException(QmsEngineeringErrorCode.CHARACTERISTIC_REVIEW_CONFLICT);
  var result=repository.findById(tenant,org,revision,id).orElseThrow(); audit.record(tenant,actor,"QUALITY_CHARACTERISTIC_"+decision,"QualityCharacteristic",id,QualityCharacteristicResponse.from(result)); return QualityCharacteristicResponse.from(result);
 }
 private void requireRevision(long tenant,long org,long revision){if(revisions.findById(tenant,org,revision).isEmpty())throw new BusinessException(QmsEngineeringErrorCode.REVISION_NOT_FOUND);}
}
