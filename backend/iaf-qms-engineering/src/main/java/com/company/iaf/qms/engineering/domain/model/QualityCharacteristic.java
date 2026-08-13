package com.company.iaf.qms.engineering.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record QualityCharacteristic(Long id, long tenantId, long orgId, long partId,
        long drawingRevisionId, String sourceEntityId, Long evidenceId, String characteristicCode,
        String characteristicType, String name, BigDecimal nominalValue, BigDecimal upperTolerance,
        BigDecimal lowerTolerance, BigDecimal upperLimit, BigDecimal lowerLimit, String unit,
        String specialCharacteristicCode, boolean inspectionDimension, boolean referenceDimension,
        boolean idealDimension, boolean fitDimension, boolean locationDimension, boolean regulatoryFlag,
        boolean mandatoryInspection, BigDecimal confidence, String status, String reviewStatus,
        Long reviewedBy, OffsetDateTime reviewedAt, String reviewComment, int version) { }
