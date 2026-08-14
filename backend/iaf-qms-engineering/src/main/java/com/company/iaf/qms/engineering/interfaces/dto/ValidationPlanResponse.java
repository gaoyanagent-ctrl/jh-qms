package com.company.iaf.qms.engineering.interfaces.dto;
import java.time.*;import java.util.List;
public record ValidationPlanResponse(long id,String planNo,long partId,long inspectionStandardId,long drawingRevisionId,int documentVersion,String status,String approvalStatus,Long supplierId,int version,LocalDateTime updatedAt,Long submittedBy,LocalDateTime submittedAt,Long approvedBy,LocalDateTime approvedAt,Long releasedBy,LocalDateTime releasedAt,List<ApprovalAction> approvalActions,List<Item> items){
 public record ApprovalAction(String action,long actorId,String comment,LocalDateTime actedAt){}
 public record Item(long id,int sequenceNo,String testItem,String standardSource,String methodAcceptanceCriteria,Long laboratoryId,boolean dvRequired,boolean pvRequired,boolean typeRequired,boolean batchRequired,Integer quantity,LocalDate startDate,LocalDate endDate,String equivalentInfo,long sourceInspectionItemId,long sourceCharacteristicId,Long evidenceId,String reviewStatus){}
}
