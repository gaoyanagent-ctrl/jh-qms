package com.company.iaf.qms.engineering.interfaces.dto;
import com.company.iaf.qms.engineering.domain.model.QualityCharacteristic;
import java.math.BigDecimal;
public record QualityCharacteristicResponse(long id,long partId,long drawingRevisionId,String sourceEntityId,Long evidenceId,
 String characteristicCode,String characteristicType,String name,BigDecimal nominalValue,BigDecimal upperTolerance,
 BigDecimal lowerTolerance,BigDecimal upperLimit,BigDecimal lowerLimit,String unit,String specialCharacteristicCode,
 boolean inspectionDimension,boolean referenceDimension,boolean idealDimension,boolean fitDimension,
 boolean locationDimension,boolean regulatoryFlag,boolean mandatoryInspection,
 BigDecimal confidence,String status,String reviewStatus,Long reviewedBy,java.time.OffsetDateTime reviewedAt,String reviewComment,int version){
 public static QualityCharacteristicResponse from(QualityCharacteristic v){return new QualityCharacteristicResponse(v.id(),v.partId(),v.drawingRevisionId(),v.sourceEntityId(),v.evidenceId(),v.characteristicCode(),v.characteristicType(),v.name(),v.nominalValue(),v.upperTolerance(),v.lowerTolerance(),v.upperLimit(),v.lowerLimit(),v.unit(),v.specialCharacteristicCode(),v.inspectionDimension(),v.referenceDimension(),v.idealDimension(),v.fitDimension(),v.locationDimension(),v.regulatoryFlag(),v.mandatoryInspection(),v.confidence(),v.status(),v.reviewStatus(),v.reviewedBy(),v.reviewedAt(),v.reviewComment(),v.version());}}
