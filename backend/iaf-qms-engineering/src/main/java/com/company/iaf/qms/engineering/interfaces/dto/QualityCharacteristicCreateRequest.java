package com.company.iaf.qms.engineering.interfaces.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record QualityCharacteristicCreateRequest(
        @NotBlank @Pattern(regexp="DIMENSION|APPEARANCE|PERFORMANCE|OTHER") String characteristicType,
        @NotBlank @Size(max=255) String name,
        BigDecimal nominalValue, BigDecimal upperTolerance, BigDecimal lowerTolerance,
        @Size(max=32) String unit, @Size(max=64) String specialCharacteristicCode,
        boolean inspectionDimension, boolean referenceDimension, boolean idealDimension,
        boolean fitDimension, boolean locationDimension, boolean regulatoryFlag,
        boolean mandatoryInspection, @Size(max=1000) String comment) { }
