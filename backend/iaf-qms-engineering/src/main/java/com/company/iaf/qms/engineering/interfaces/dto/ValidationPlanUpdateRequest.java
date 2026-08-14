package com.company.iaf.qms.engineering.interfaces.dto;
import jakarta.validation.Valid;import jakarta.validation.constraints.*;import java.time.LocalDate;import java.util.List;
public record ValidationPlanUpdateRequest(@NotNull @PositiveOrZero Integer version,Long supplierId,@NotEmpty List<@Valid Item> items){
 public record Item(@Positive long id,@NotBlank @Size(max=1000) String methodAcceptanceCriteria,Long laboratoryId,boolean dvRequired,boolean pvRequired,boolean typeRequired,boolean batchRequired,@Positive Integer quantity,LocalDate startDate,LocalDate endDate,@Size(max=1000) String equivalentInfo){}
}
