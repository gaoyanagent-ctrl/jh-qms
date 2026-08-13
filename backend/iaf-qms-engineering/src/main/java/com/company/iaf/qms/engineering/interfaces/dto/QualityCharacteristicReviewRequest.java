package com.company.iaf.qms.engineering.interfaces.dto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
public record QualityCharacteristicReviewRequest(@NotNull Integer version,@Size(max=255) String name,
 BigDecimal nominalValue,BigDecimal upperTolerance,BigDecimal lowerTolerance,@Size(max=32) String unit,
 @Pattern(regexp="DIMENSION|APPEARANCE|PERFORMANCE|OTHER") String characteristicType,@Size(max=64) String specialCharacteristicCode,
 Boolean inspectionDimension,Boolean referenceDimension,Boolean idealDimension,Boolean fitDimension,
 Boolean locationDimension,Boolean regulatoryFlag,Boolean mandatoryInspection,
 @Size(max=1000) String comment){}
