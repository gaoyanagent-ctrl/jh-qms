package com.company.iaf.qms.engineering.interfaces.dto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
public record QualityCharacteristicReviewRequest(@NotNull Integer version,@Size(max=255) String name,
 BigDecimal nominalValue,BigDecimal upperTolerance,BigDecimal lowerTolerance,@Size(max=32) String unit,
 @Size(max=1000) String comment){}
