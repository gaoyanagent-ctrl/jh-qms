package com.company.iaf.qms.engineering.interfaces.dto;
import jakarta.validation.Valid;import jakarta.validation.constraints.*;import java.util.List;
public record InspectionStandardUpdateRequest(@NotNull @PositiveOrZero Integer version,@Size(max=2000) String reactionPlan,@NotEmpty List<@Valid Item> items){public record Item(@Positive long id,@NotBlank @Size(max=1000) String requirement,@Size(max=255) String supplierBatchSampling,@Size(max=255) String supplierBatchMethod,@Size(max=255) String supplierAnnualSampling,@Size(max=255) String supplierAnnualMethod,@Size(max=1000) String remark){}}
