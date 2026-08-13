package com.company.iaf.qms.engineering.interfaces.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

public record DrawingLegendRuleUpdateRequest(@NotEmpty @Size(max=20) List<@Valid Rule> rules) {
 public record Rule(@Positive long id,@NotNull @PositiveOrZero Integer version,
   @NotBlank @Size(max=32) String marker,@NotBlank @Size(max=128) String description,
   boolean enabled,@Min(0) @Max(9999) int priority) { }
}
