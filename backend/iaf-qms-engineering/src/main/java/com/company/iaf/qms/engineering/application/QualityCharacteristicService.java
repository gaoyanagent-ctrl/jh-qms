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
  var current=repository.findById(tenant,org,revision,id).orElseThrow(()->new BusinessException(QmsEngineeringErrorCode.CHARACTERISTIC_REVIEW_CONFLICT));
  validate(request.referenceDimension()!=null?request.referenceDimension():current.referenceDimension(),
      request.idealDimension()!=null?request.idealDimension():current.idealDimension(),
      request.inspectionDimension()!=null?request.inspectionDimension():current.inspectionDimension(),
      request.mandatoryInspection()!=null?request.mandatoryInspection():current.mandatoryInspection());
  long actor=SecurityContext.getUserId().orElse(0L); boolean changed=repository.review(actor,tenant,org,revision,id,request.version(),decision,request.name(),request.nominalValue(),request.upperTolerance(),request.lowerTolerance(),request.unit(),request.characteristicType(),request.specialCharacteristicCode(),request.inspectionDimension(),request.referenceDimension(),request.idealDimension(),request.fitDimension(),request.locationDimension(),request.regulatoryFlag(),request.mandatoryInspection(),request.comment());
  if(!changed)throw new BusinessException(QmsEngineeringErrorCode.CHARACTERISTIC_REVIEW_CONFLICT);
  var result=repository.findById(tenant,org,revision,id).orElseThrow(); audit.record(tenant,actor,"QUALITY_CHARACTERISTIC_"+decision,"QualityCharacteristic",id,QualityCharacteristicResponse.from(result)); return QualityCharacteristicResponse.from(result);
 }
 @RequiresPermission("qms:quality-characteristic:review") @Transactional
 public QualityCharacteristicResponse create(long tenant,long org,long revision,QualityCharacteristicCreateRequest request){
  requireRevision(tenant,org,revision); validate(request.referenceDimension(),request.idealDimension(),request.inspectionDimension(),request.mandatoryInspection());
  long actor=SecurityContext.getUserId().orElse(0L);
  var result=repository.createManual(actor,tenant,org,revision,request.characteristicType(),request.name(),request.nominalValue(),request.upperTolerance(),request.lowerTolerance(),request.unit(),request.specialCharacteristicCode(),request.inspectionDimension(),request.referenceDimension(),request.idealDimension(),request.fitDimension(),request.locationDimension(),request.regulatoryFlag(),request.mandatoryInspection(),request.comment());
  var response=QualityCharacteristicResponse.from(result); audit.record(tenant,actor,"QUALITY_CHARACTERISTIC_CREATED","QualityCharacteristic",result.id(),response); return response;
 }
 @RequiresPermission("qms:quality-characteristic:review") @Transactional
 public List<QualityCharacteristicResponse> bulkReview(long tenant,long org,long revision,QualityCharacteristicBulkReviewRequest request){
  requireRevision(tenant,org,revision); long actor=SecurityContext.getUserId().orElse(0L);
  for(var target:request.targets()){
   var current=repository.findById(tenant,org,revision,target.id()).orElseThrow(()->new BusinessException(QmsEngineeringErrorCode.CHARACTERISTIC_REVIEW_CONFLICT));
   boolean changed=repository.review(actor,tenant,org,revision,target.id(),target.version(),request.decision().name(),null,null,null,null,null,current.characteristicType(),current.specialCharacteristicCode(),null,null,null,null,null,null,null,request.comment());
   if(!changed)throw new BusinessException(QmsEngineeringErrorCode.CHARACTERISTIC_REVIEW_CONFLICT);
  }
  var results=request.targets().stream().map(target->repository.findById(tenant,org,revision,target.id()).orElseThrow()).map(QualityCharacteristicResponse::from).toList();
  results.forEach(result->audit.record(tenant,actor,"QUALITY_CHARACTERISTIC_"+request.decision(),"QualityCharacteristic",result.id(),result)); return results;
 }
 private void validate(boolean reference,boolean ideal,boolean inspection,boolean mandatory){if((reference&&ideal)||((reference||ideal)&&(inspection||mandatory)))throw new BusinessException(QmsEngineeringErrorCode.CHARACTERISTIC_CLASSIFICATION_INVALID);}
 private void requireRevision(long tenant,long org,long revision){if(revisions.findById(tenant,org,revision).isEmpty())throw new BusinessException(QmsEngineeringErrorCode.REVISION_NOT_FOUND);}
}
