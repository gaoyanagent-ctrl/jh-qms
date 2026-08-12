package com.company.iaf.qms.engineering.interfaces.dto;
import com.company.iaf.qms.engineering.domain.model.QualityCharacteristic;
import java.math.BigDecimal;
public record QualityCharacteristicResponse(long id,long partId,long drawingRevisionId,String sourceEntityId,long evidenceId,
 String characteristicCode,String characteristicType,String name,BigDecimal nominalValue,BigDecimal upperTolerance,
 BigDecimal lowerTolerance,BigDecimal upperLimit,BigDecimal lowerLimit,String unit,String specialCharacteristicCode,
 BigDecimal confidence,String status,String reviewStatus,Long reviewedBy,java.time.OffsetDateTime reviewedAt,String reviewComment,int version){
 public static QualityCharacteristicResponse from(QualityCharacteristic v){return new QualityCharacteristicResponse(v.id(),v.partId(),v.drawingRevisionId(),v.sourceEntityId(),v.evidenceId(),v.characteristicCode(),v.characteristicType(),v.name(),v.nominalValue(),v.upperTolerance(),v.lowerTolerance(),v.upperLimit(),v.lowerLimit(),v.unit(),v.specialCharacteristicCode(),v.confidence(),v.status(),v.reviewStatus(),v.reviewedBy(),v.reviewedAt(),v.reviewComment(),v.version());}}
